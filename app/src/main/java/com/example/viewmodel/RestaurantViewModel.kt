package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DatabaseService
import com.example.data.DashboardStats
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLEncoder

class RestaurantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseService.getInstance(application.applicationContext)

    // User State
    val userName = db.currentUserName
    val userPhone = db.currentUserPhone
    val userAddress = db.currentUserAddress
    val userLoyaltyPoints = db.currentUserLoyaltyPoints

    // FCM Integration States & Simulators
    val fcmToken = db.fcmToken
    val fcmPayloads = db.fcmPayloads

    fun simulateInboundFCMMessage(context: android.content.Context, title: String, body: String, channelId: String) {
        db.simulateIncomingFcmMessage(context, title, body, channelId)
    }

    fun clearFcmLogs() {
        db.clearFcmLogs()
    }

    fun updateProfile(name: String, phone: String, address: String) {
        db.updateProfile(name, phone, address)
    }

    // Menu Categories
    val categories = db.categories

    // Navigation and UI States
    private val _currentTab = MutableStateFlow<String>("menu")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String>("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Shopping Cart State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // Active order for checkout tracking
    private val _lastPlacedOrder = MutableStateFlow<Order?>(null)
    val lastPlacedOrder: StateFlow<Order?> = _lastPlacedOrder.asStateFlow()

    // Selected receipt screenshot URI string for current checkout
    private val _selectedScreenshotUri = MutableStateFlow<String?>(null)
    val selectedScreenshotUri: StateFlow<String?> = _selectedScreenshotUri.asStateFlow()

    // Filtered menu items
    val allMenuItems = db.menuItems

    val menuItems: StateFlow<List<MenuItem>> = combine(
        db.menuItems,
        _selectedCategory,
        _searchQuery
    ) { items, cat, query ->
        items.filter {
            (cat == "All" || it.category == cat) &&
            (query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All orders for admin / history
    val orders = db.orders
    val feedbacks = db.feedbacks
    val notifications = db.notifications

    // Customer specific order history
    val customerOrderHistory: StateFlow<List<Order>> = db.orders.map { list ->
        list.filter { it.customerPhone == userPhone.value }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Calculation
    val cartTotal: StateFlow<Double> = _cart.map { items ->
        items.sumOf { it.menuItem.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartCount: StateFlow<Int> = _cart.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Admin Panel Dashboard Stats
    private val _adminStats = MutableStateFlow(db.getDashboardStats())
    val adminStats: StateFlow<DashboardStats> = _adminStats.asStateFlow()

    // Checkout Form States
    val customerNameInput = MutableStateFlow("")
    val customerPhoneInput = MutableStateFlow("")
    val deliveryAddressInput = MutableStateFlow("")
    val orderTypeChoice = MutableStateFlow(OrderType.DELIVERY)
    val paymentMethodChoice = MutableStateFlow(PaymentMethod.JAZZCASH)

    // Loyalty Point Redemption States
    val redeemPointsChecked = MutableStateFlow(false)

    val maxRedeemablePoints: StateFlow<Int> = combine(
        userLoyaltyPoints,
        cartTotal
    ) { points, total ->
        minOf(points, total.toInt())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val actualRedeemedPoints: StateFlow<Int> = combine(
        redeemPointsChecked,
        maxRedeemablePoints
    ) { checked, maxPoints ->
        if (checked) maxPoints else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pointsDiscountAmount: StateFlow<Double> = actualRedeemedPoints.map { points ->
        points.toDouble()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val finalCheckoutTotal: StateFlow<Double> = combine(
        cartTotal,
        pointsDiscountAmount
    ) { total, discount ->
        (total - discount).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Voice Command State
    private val _voiceStatus = MutableStateFlow<String>("")
    val voiceStatus: StateFlow<String> = _voiceStatus.asStateFlow()

    init {
        // Sync checkout form state with initial default values
        viewModelScope.launch {
            userName.collect { customerNameInput.value = it }
        }
        viewModelScope.launch {
            userPhone.collect { customerPhoneInput.value = it }
        }
        viewModelScope.launch {
            userAddress.collect { deliveryAddressInput.value = it }
        }
        // Watch for order list updates to refresh dashboard stats dynamically
        viewModelScope.launch {
            orders.collect {
                _adminStats.value = db.getDashboardStats()
            }
        }
    }

    fun setTab(tabName: String) {
        _currentTab.value = tabName
    }

    fun selectCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(itemId: String) {
        db.toggleFavorite(itemId)
    }

    // Shopping Cart Methods
    fun addToCart(item: MenuItem, change: Int = 1) {
        val currentList = _cart.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.menuItem.id == item.id }

        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            val newQty = existing.quantity + change
            if (newQty > 0) {
                currentList[existingIndex] = existing.copy(quantity = newQty)
            } else {
                currentList.removeAt(existingIndex)
            }
        } else if (change > 0) {
            currentList.add(CartItem(item, change))
        }
        _cart.value = currentList
    }

    fun setScreenshot(uri: String?) {
        _selectedScreenshotUri.value = uri
    }

    fun clearCart() {
        _cart.value = emptyList()
        _selectedScreenshotUri.value = null
        redeemPointsChecked.value = false
    }

    // Submit Checkout
    fun checkout() {
        if (_cart.value.isEmpty()) return

        val order = db.placeOrder(
            customerName = customerNameInput.value.ifEmpty { "Guest" },
            customerPhone = customerPhoneInput.value.ifEmpty { "00000000000" },
            deliveryAddress = deliveryAddressInput.value.ifEmpty { "Store Pick-up" },
            type = orderTypeChoice.value,
            paymentMethod = paymentMethodChoice.value,
            items = _cart.value,
            totalAmount = finalCheckoutTotal.value,
            screenshotUri = _selectedScreenshotUri.value,
            pointsRedeemed = actualRedeemedPoints.value,
            discountAmount = pointsDiscountAmount.value
        )

        // Save last order
        _lastPlacedOrder.value = order
        
        // Clean cart after placing order
        clearCart()
        _currentTab.value = "confirmation"
    }

    // Customer actions
    fun repeatOrder(order: Order) {
        _cart.value = order.items
        _currentTab.value = "cart"
        db.pushNotification("Reloaded items from order ${order.id} into your cart!")
    }

    fun submitFeedback(text: String, rating: Int) {
        db.submitFeedback(customerNameInput.value.ifEmpty { "Guest" }, text, rating)
    }

    fun submitProductReview(itemId: String, reviewText: String, rating: Int) {
        db.addReviewToItem(itemId, reviewText, rating)
    }

    // Admin Dashboard operations
    fun acceptOrder(orderId: String) {
        db.updateOrderStatus(orderId, OrderStatus.ACCEPTED)
    }

    fun rejectOrder(orderId: String) {
        db.updateOrderStatus(orderId, OrderStatus.REJECTED)
    }

    fun markPreparing(orderId: String) {
        db.updateOrderStatus(orderId, OrderStatus.PREPARING)
    }

    fun markDelivered(orderId: String) {
        db.updateOrderStatus(orderId, OrderStatus.DELIVERED)
    }

    fun togglePaymentVerification(orderId: String, currentStatus: Boolean) {
        db.verifyPayment(orderId, !currentStatus)
    }

    // Dynamic Menu CRUD wrappers for administrator interface
    fun addMenuItem(item: MenuItem) {
        db.addMenuItem(item)
    }

    fun deleteMenuItem(itemId: String) {
        db.deleteMenuItem(itemId)
    }

    fun updateMenuItem(updatedItem: MenuItem) {
        db.updateMenuItem(updatedItem)
    }

    fun deleteNotification() {
        db.clearNotifications()
    }

    // WhatsApp Cloud API prep structure
    fun buildWhatsAppShareMessage(order: Order): String {
        val itemsString = order.items.joinToString("\n") { "• ${it.menuItem.name} x${it.quantity} (PKR ${it.menuItem.price * it.quantity})" }
        val rawMessage = """
            🔔 *RAHMAN FAST FOOD ORDER NOTICE* 🔔
            ───────────────────
            *Order ID:* ${order.id}
            *Customer:* ${order.customerName}
            *Phone:* ${order.customerPhone}
            *Method:* ${order.type.displayName}
            *Payment:* ${order.paymentMethod.displayName} (${if (order.paymentVerified) "Verified" else "Awaiting Verification"})
            ───────────────────
            *Items Ordered:*
            $itemsString
            ───────────────────
            *Total Amount:* PKR ${order.totalAmount}
            *Delivery Address:* ${order.deliveryAddress}
            
            Thank you for choosing Rahman Fast Food! Your satisfaction is our gold standard.
        """.trimIndent()
        
        return try {
            "https://api.whatsapp.com/send?phone=923001234567&text=" + URLEncoder.encode(rawMessage, "UTF-8")
        } catch (e: Exception) {
            "https://api.whatsapp.com/send?text=OrderPlaced"
        }
    }

    // VOICE ORDERING structure and mock execution
    fun executeVoiceCommand(command: String) {
        val lower = command.lowercase()
        _voiceStatus.value = "Processing: \"$command\""
        
        viewModelScope.launch {
            // Find matched items
            val allItems = db.menuItems.value
            var matchedAny = false
            var addedItemsList = mutableListOf<String>()

            for (item in allItems) {
                if (lower.contains(item.name.lowercase())) {
                    // Check if a multiplier exists
                    var qty = 1
                    if (lower.contains("2 ") || lower.contains("two ")) qty = 2
                    if (lower.contains("3 ") || lower.contains("three ")) qty = 3
                    if (lower.contains("4 ") || lower.contains("four ")) qty = 4

                    addToCart(item, qty)
                    addedItemsList.add("$qty x ${item.name}")
                    matchedAny = true
                }
            }

            if (matchedAny) {
                _voiceStatus.value = "Success! Added: " + addedItemsList.joinToString(", ")
                db.pushNotification("Voice Command added: " + addedItemsList.joinToString(", "))
                _currentTab.value = "cart"
            } else {
                _voiceStatus.value = "Could not find any matching food items."
            }
        }
    }
}
