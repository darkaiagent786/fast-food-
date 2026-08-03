package com.example.data

import android.content.Context
import android.net.Uri
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class DatabaseService(private val context: Context) {

    // 16 Standard Categories from specs
    val categories = listOf(
        "Burgers", "Zinger Burgers", "Double Egg Burgers", "Shawarma", "Zinger Shawarma",
        "Pizza", "Loaded Fries", "Fries", "Nuggets", "Chicken Samosa", "Cream Samosa",
        "Kebabs", "Biryani", "Potato Snacks", "Ice Cream", "Drinks"
    )

    // Dynamic State lists for local persistence/sandbox operations
    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _feedbacks = MutableStateFlow<List<Feedback>>(emptyList())
    val feedbacks: StateFlow<List<Feedback>> = _feedbacks.asStateFlow()

    private val _currentUserPhone = MutableStateFlow<String>("03217654321")
    val currentUserPhone: StateFlow<String> = _currentUserPhone.asStateFlow()

    private val _currentUserName = MutableStateFlow<String>("Hamza Rahman")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserAddress = MutableStateFlow<String>("House 45, Street G-10, Islamabad")
    val currentUserAddress: StateFlow<String> = _currentUserAddress.asStateFlow()

    private val _currentUserLoyaltyPoints = MutableStateFlow<Int>(350)
    val currentUserLoyaltyPoints: StateFlow<Int> = _currentUserLoyaltyPoints.asStateFlow()

    // Notification queue for simulation
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    // FCM Integration States & Logs
    private val _fcmToken = MutableStateFlow<String>("fcm_sandbox_token_rahman_fast_food_786")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    private val _fcmPayloads = MutableStateFlow<List<FcmPayload>>(emptyList())
    val fcmPayloads: StateFlow<List<FcmPayload>> = _fcmPayloads.asStateFlow()

    fun updateFcmToken(newToken: String) {
        _fcmToken.value = newToken
        pushNotification("FCM registration token updated: ${newToken.take(15)}...")
    }

    fun receiveFcmPayload(payload: FcmPayload) {
        val current = _fcmPayloads.value.toMutableList()
        current.add(0, payload)
        _fcmPayloads.value = current
        pushNotification("FCM Message [${payload.type}] Received: ${payload.title}")
    }

    fun clearFcmLogs() {
        _fcmPayloads.value = emptyList()
        pushNotification("FCM transaction history logs cleared.")
    }

    fun simulateIncomingFcmMessage(context: Context, title: String, body: String, channelId: String) {
        val standardizedType = if (channelId == "channel_order_status") "Order Update" else "Exclusive Promo"
        val payload = FcmPayload(
            title = title,
            body = body,
            type = standardizedType,
            timestamp = System.currentTimeMillis()
        )
        receiveFcmPayload(payload)
        com.example.fcm.NotificationHelper.showNotification(context, title, body, channelId)
    }

    init {
        initialize50MenuItems()
        initializeMockOrders()
        initializeMockFeedback()
    }

    fun pushNotification(message: String) {
        val currentList = _notifications.value.toMutableList()
        currentList.add(0, "[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}] $message")
        _notifications.value = currentList
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    fun updateProfile(name: String, phone: String, address: String) {
        _currentUserName.value = name
        _currentUserPhone.value = phone
        _currentUserAddress.value = address
    }

    fun earnLoyaltyPoints(points: Int) {
        if (points <= 0) return
        _currentUserLoyaltyPoints.value += points
        pushNotification("Earned $points Loyalty Points! New balance: ${_currentUserLoyaltyPoints.value}")
    }

    fun deductLoyaltyPoints(points: Int) {
        if (points <= 0) return
        _currentUserLoyaltyPoints.value = (_currentUserLoyaltyPoints.value - points).coerceAtLeast(0)
        pushNotification("Redeemed $points Loyalty Points as discount. New balance: ${_currentUserLoyaltyPoints.value}")
    }

    fun refundLoyaltyPoints(points: Int) {
        if (points <= 0) return
        _currentUserLoyaltyPoints.value += points
        pushNotification("Refunded $points Loyalty Points due to status change. New balance: ${_currentUserLoyaltyPoints.value} points")
    }

    // Toggle favorite state
    fun toggleFavorite(itemId: String) {
        _menuItems.value = _menuItems.value.map {
            if (it.id == itemId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    // Dynamic Administrator Menu CRUD operations
    fun addMenuItem(item: MenuItem) {
        val currentList = _menuItems.value.toMutableList()
        currentList.add(item)
        _menuItems.value = currentList
    }

    fun deleteMenuItem(itemId: String) {
        val currentList = _menuItems.value.toMutableList()
        currentList.removeAll { it.id == itemId }
        _menuItems.value = currentList
    }

    fun updateMenuItem(updatedItem: MenuItem) {
        _menuItems.value = _menuItems.value.map {
            if (it.id == updatedItem.id) updatedItem else it
        }
    }

    // Add rating and review to item
    fun addReviewToItem(itemId: String, reviewText: String, rating: Int) {
        _menuItems.value = _menuItems.value.map {
            if (it.id == itemId) {
                val newCount = it.reviewsCount + 1
                val newRating = ((it.rating * it.reviewsCount) + rating) / newCount
                it.copy(rating = newRating, reviewsCount = newCount)
            } else it
        }
        
        // Update corresponding order item if needed
        val timeLabel = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        pushNotification("New Review received for food ID $itemId: $rating Stars!")
    }

    // Place new order
    fun placeOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        type: OrderType,
        paymentMethod: PaymentMethod,
        items: List<CartItem>,
        totalAmount: Double,
        screenshotUri: String? = null,
        pointsRedeemed: Int = 0,
        discountAmount: Double = 0.0
    ): Order {
        val ptsEarned = (totalAmount * 0.05).toInt()
        val newOrder = Order(
            id = "RF-" + (100000 + Random().nextInt(900000)),
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = if (type == OrderType.DELIVERY) deliveryAddress else "Dine-In Table No-5",
            type = type,
            status = OrderStatus.PENDING,
            paymentMethod = paymentMethod,
            paymentVerified = false,
            paymentScreenshotUri = screenshotUri,
            items = items,
            totalAmount = totalAmount,
            timestamp = System.currentTimeMillis(),
            pointsEarned = ptsEarned,
            pointsRedeemed = pointsRedeemed,
            discountAmount = discountAmount
        )

        if (pointsRedeemed > 0) {
            deductLoyaltyPoints(pointsRedeemed)
        }

        val currentList = _orders.value.toMutableList()
        currentList.add(0, newOrder)
        _orders.value = currentList

        pushNotification("New Order Placed: ${newOrder.id} by ${newOrder.customerName} (PKR ${newOrder.totalAmount}). Will earn ${newOrder.pointsEarned} loyalty points upon delivery.")
        if (screenshotUri != null) {
            pushNotification("New Payment Uploaded for Order: ${newOrder.id} (${paymentMethod.displayName})")
        }

        /* 
           Production Firebase Path (Commented for integration reference):
           val firestore = FirebaseFirestore.getInstance()
           firestore.collection("orders").document(newOrder.id).set(newOrder)
           firestore.collection("users").document(customerPhone).collection("history").document(newOrder.id).set(newOrder)
        */

        return newOrder
    }

    // Update order status
    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) {
                val updated = it.copy(status = status)
                pushNotification("Order ${it.id} status updated to ${status.displayName}")
                
                // Trigger real-time status update push notification (FCM style)
                com.example.fcm.NotificationHelper.showNotification(
                    context = context,
                    title = "Order status updated: ${it.id}",
                    message = "Your order status is now: ${status.displayName}. Enjoy the best gold-standard fast food! 🎯",
                    channelId = com.example.fcm.NotificationHelper.CHANNEL_ORDERS
                )

                if (status == OrderStatus.DELIVERED && it.status != OrderStatus.DELIVERED) {
                    earnLoyaltyPoints(it.pointsEarned)
                } else if (status == OrderStatus.REJECTED && it.status != OrderStatus.REJECTED) {
                    if (it.pointsRedeemed > 0) {
                        refundLoyaltyPoints(it.pointsRedeemed)
                    }
                }
                
                updated
            } else it
        }

        /*
           Production Firebase Path:
           FirebaseFirestore.getInstance().collection("orders").document(orderId).update("status", status.name)
        */
    }

    // Verify order payment
    fun verifyPayment(orderId: String, verified: Boolean) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) {
                val updated = it.copy(paymentVerified = verified)
                if (verified) {
                    pushNotification("Payment for Order ${it.id} VERIFIED successfully!")
                } else {
                    pushNotification("Payment for Order ${it.id} marked UNVERIFIED.")
                }
                updated
            } else it
        }

        /*
           Production Firebase Path:
           FirebaseFirestore.getInstance().collection("orders").document(orderId).update("paymentVerified", verified)
        */
    }

    // Submit Feedback
    fun submitFeedback(customerName: String, text: String, rating: Int) {
        val feedback = Feedback(
            id = UUID.randomUUID().toString(),
            customerName = customerName,
            text = text,
            rating = rating,
            timestamp = System.currentTimeMillis()
        )
        val current = _feedbacks.value.toMutableList()
        current.add(0, feedback)
        _feedbacks.value = current

        pushNotification("Customer Feedback: '$text' by $customerName (${rating}★)")

        /*
           Production Firebase Path:
           FirebaseFirestore.getInstance().collection("feedback").document(feedback.id).set(feedback)
        */
    }

    // Generate Admin Dashboard Analytics
    fun getDashboardStats(): DashboardStats {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val weekStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.timeInMillis

        val monthStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }.timeInMillis

        val currentOrders = _orders.value

        val todayOrdersList = currentOrders.filter { it.timestamp >= todayStart }
        val weekOrdersList = currentOrders.filter { it.timestamp >= weekStart }
        val monthOrdersList = currentOrders.filter { it.timestamp >= monthStart }

        val todayRevenue = todayOrdersList.filter { it.status == OrderStatus.DELIVERED && it.paymentVerified }.sumOf { it.totalAmount }
        val weeklyRevenue = weekOrdersList.filter { it.status == OrderStatus.DELIVERED && it.paymentVerified }.sumOf { it.totalAmount }
        val monthlyRevenue = monthOrdersList.filter { it.status == OrderStatus.DELIVERED && it.paymentVerified }.sumOf { it.totalAmount }

        val pending = currentOrders.count { it.status == OrderStatus.PENDING }
        val accepted = currentOrders.count { it.status == OrderStatus.ACCEPTED }
        val preparing = currentOrders.count { it.status == OrderStatus.PREPARING }
        val delivered = currentOrders.count { it.status == OrderStatus.DELIVERED }
        val cancelled = currentOrders.count { it.status == OrderStatus.REJECTED }

        return DashboardStats(
            todayOrders = todayOrdersList.size,
            todayRevenue = todayRevenue,
            weeklyRevenue = weeklyRevenue,
            monthlyRevenue = monthlyRevenue,
            pendingOrders = pending,
            acceptedOrders = accepted + preparing, // Accepted + Preparing count as active accepted orders range
            deliveredOrders = delivered,
            cancelledOrders = cancelled
        )
    }

    private fun initializeMockFeedback() {
        _feedbacks.value = listOf(
            Feedback(UUID.randomUUID().toString(), "Ayesha Khan", "The Royal Zinger is incredibly juicy and piping hot! Gold standard fast food.", 5),
            Feedback(UUID.randomUUID().toString(), "Zainab Ali", "Loaded fries lava portion was massive. Perfect for a cheat meal.", 4),
            Feedback(UUID.randomUUID().toString(), "Bilal Ahmed", "Quick delivery. The Mint Margarita was very refreshing. Highly recommended!", 5)
        )
    }

    private fun initializeMockOrders() {
        // Initial set of dummy orders for dashboard visualization
        val pizza = _menuItems.value.firstOrNull { it.category == "Pizza" } ?: MenuItem("m13", "RF Supreme Pizza", "Gourmet gold pizza crust", 990.0, "Pizza")
        val zinger = _menuItems.value.firstOrNull { it.category == "Zinger Burgers" } ?: MenuItem("m5", "RF Supreme Zinger", "Crunchy royal zinger patty", 450.0, "Zinger Burgers")
        val fries = _menuItems.value.firstOrNull { it.category == "Loaded Fries" } ?: MenuItem("m21", "Lava Loaded Fries", "Cheesy golden french fries", 480.0, "Loaded Fries")

        _orders.value = listOf(
            Order(
                id = "RF-518293",
                customerName = "Ali Raza",
                customerPhone = "03001234567",
                deliveryAddress = "Sector F-11, Street 4, Flat 12B, Islamabad",
                type = OrderType.DELIVERY,
                status = OrderStatus.DELIVERED,
                paymentMethod = PaymentMethod.EASYPAISA,
                paymentVerified = true,
                items = listOf(CartItem(pizza, 1), CartItem(fries, 1)),
                totalAmount = 1470.0,
                timestamp = System.currentTimeMillis() - 1200000 // 20 mins ago
            ),
            Order(
                id = "RF-982103",
                customerName = "Sara Shah",
                customerPhone = "03219876543",
                deliveryAddress = "Dine-In Table No-5",
                type = OrderType.DINE_IN,
                status = OrderStatus.ACCEPTED,
                paymentMethod = PaymentMethod.JAZZCASH,
                paymentVerified = true,
                items = listOf(CartItem(zinger, 2)),
                totalAmount = 900.0,
                timestamp = System.currentTimeMillis() - 600000 // 10 mins ago
            ),
            Order(
                id = "RF-230198",
                customerName = "Usman Sheikh",
                customerPhone = "03125554321",
                deliveryAddress = "G-9 Phase 2, Islamabad",
                type = OrderType.DELIVERY,
                status = OrderStatus.PENDING,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                paymentVerified = false,
                items = listOf(CartItem(zinger, 1), CartItem(fries, 2)),
                totalAmount = 1410.0,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun initialize50MenuItems() {
        val list = mutableListOf<MenuItem>()

        // 1. Burgers (4 items)
        list.add(MenuItem("burger_1", "RF Gold Beef Burger", "Premium hand-smashed double beef patty smothered with signature gold glaze, secret sauce, and sharp cheddar.", 650.0, "Burgers"))
        list.add(MenuItem("burger_2", "Premium Cheese Burger", "Classic fire-grilled chicken patty topped with melted premium cheddar cheese slice, fresh lettuce, and special pickles.", 390.0, "Burgers"))
        list.add(MenuItem("burger_3", "Smoky BBQ Burger", "Flame-grilled tender smoky chicken fillet drenched in rich golden hickory BBQ sauce with crispy caramelized onions.", 440.0, "Burgers"))
        list.add(MenuItem("burger_4", "Crispy Chicken Burger", "Crisp hand-breaded golden chicken breast on a toasted artisanal brioche bun with creamy golden mayo.", 380.0, "Burgers"))

        // 2. Zinger Burgers (4 items)
        list.add(MenuItem("zinger_1", "RF Supreme Zinger", "The King! Super-crispy golden double breast fillet, extra crunchy coating, fresh iceberg lettuce, and premium gold garlic sauce.", 490.0, "Zinger Burgers"))
        list.add(MenuItem("zinger_2", "Gold Chef Zinger with Cheese", "Signature zinger chicken topped with a molten cheddar cheese ring, jalapeños, and firecracker hot gold sauce.", 540.0, "Zinger Burgers"))
        list.add(MenuItem("zinger_3", "Firecracker Hot Zinger", "Explosive hot crispy dipped zinger breast fillet with spicy fire-paste crust, spicy lettuce, and hot chili-mayo.", 520.0, "Zinger Burgers"))
        list.add(MenuItem("zinger_4", "Double Decker Zinger", "Gargantuan stack of two succulent crispy breast zingers, layered with double cheese, and gold pepper dressing.", 750.0, "Zinger Burgers"))

        // 3. Double Egg Burgers (4 items)
        list.add(MenuItem("egg_1", "Classic Egg Burger", "Local hero! Spiced hand-smashed lentil-chicken patty topped with a fluffy double-folded pan egg and tangy mint sauce.", 190.0, "Double Egg Burgers"))
        list.add(MenuItem("egg_2", "Double Egg Special", "Double the goodness. Two signature pan-fried spiced eggs on a golden butter-toasted round bun with sweet garlic mayo.", 230.0, "Double Egg Burgers"))
        list.add(MenuItem("egg_3", "Royal Egg Cheese Burger", "Premium chicken kebab patty layered with fluffy seasoned double-pan-run eggs, a gold cheddar cheese slice, and sweet green chutney.", 290.0, "Double Egg Burgers"))
        list.add(MenuItem("egg_4", "Spicy Shami Egg Burger", "Authentic Shami kebab stuffed with secret spices, topped with double golden scrambled eggs and layered with crisp lettuce.", 220.0, "Double Egg Burgers"))

        // 4. Shawarma (4 items)
        list.add(MenuItem("shawarma_1", "Classic Chicken Shawarma", "Succulent slow-roasted spiced chicken shavings, crispy pickled cucumbers, wrapped in fresh toasted flatbread with classic garlic sauce.", 180.0, "Shawarma"))
        list.add(MenuItem("shawarma_2", "Golden Cheese Shawarma", "Our traditional spice chicken shawarma roll packed with extra premium grated mozzarella and golden liquid cheddar blend.", 240.0, "Shawarma"))
        list.add(MenuItem("shawarma_3", "Spicy Garlic Shawarma", "Sizzling wrap filled with spicy shredded tandoori chicken chunks, onion rings, hot pickles, and fiery red chili-garlic sauce.", 200.0, "Shawarma"))
        list.add(MenuItem("shawarma_4", "Creamy Mayo Shawarma", "Melt-in-mouth chicken chunks wrapped in light pita bread, dripping with rich, freshly emulsified black pepper mayo sauce.", 190.0, "Shawarma"))

        // 5. Zinger Shawarma (3 items)
        list.add(MenuItem("zshawarma_1", "Supreme Zinger Shawarma", "Crunchy zinger chicken strips chopped and rolled inside a warm toasted flatbread, finished with garlic-herb mayonnaise.", 260.0, "Zinger Shawarma"))
        list.add(MenuItem("zshawarma_2", "Zinger Cheese Shawarma", "Gold crispy zinger chunks wrapped with molten mozzarella cheese shreds, premium garlic cream, and seasoned potato chips.", 320.0, "Zinger Shawarma"))
        list.add(MenuItem("zshawarma_3", "Jalapeno Zinger Shawarma", "Hot zinger chicken strips mixed with spicy pickled jalapenos, hot sauce, and gold cheese sauce in flatbread.", 290.0, "Zinger Shawarma"))

        // 6. Pizza (5 items)
        list.add(MenuItem("pizza_1", "RF Supreme Gold Pizza", "House signature! Rich tomato marinara, chunks of chicken tikka, beef pepperoni, smoked onions, green peppers, golden cheddar crust.", 990.0, "Pizza"))
        list.add(MenuItem("pizza_2", "Chicken Tikka Classic", "Authentic charcoal-grilled tandoori chicken tikka cubes, red onions, loaded cheese blend, and green mint drizzle.", 890.0, "Pizza"))
        list.add(MenuItem("pizza_3", "Royal Gold Fajita Pizza", "Sizzling fajita-spiced chicken slices, yellow and red sweet bell peppers, caramelized onions, topped with a rich gold blend of cheese.", 940.0, "Pizza"))
        list.add(MenuItem("pizza_4", "Veggie Gold Sensation", "Gourmet mix of sweet corn, chopped tomatoes, black olives, sliced mushrooms, green bell peppers, and premium mozzarella cheese.", 790.0, "Pizza"))
        list.add(MenuItem("pizza_5", "Cheesy Cheese Gold Crust", "Double-thick cheese pull action! Premium classic mozzarella, gold cheddar, and parmesan melted on a butter-garlic hand-tossed crust.", 840.0, "Pizza"))

        // 7. Loaded Fries (4 items)
        list.add(MenuItem("lfries_1", "Lava Loaded Fries", "Golden bucket fries completely buried under liquid warm cheddar cheese, chopped grilled chicken chunks, and high-heat jalapeños.", 480.0, "Loaded Fries"))
        list.add(MenuItem("lfries_2", "Garlic Mayo Loaded Fries", "Hefty portion of crispy fries tossed in gold garlic rub spices, dripping with garlic mayo, chives, and mozzarella sprinkle.", 390.0, "Loaded Fries"))
        list.add(MenuItem("lfries_3", "BBQ Crispy Chicken Fries", "Crispy golden shoestring fries loaded with crispy fried zinger bits, smoky hickory BBQ syrup, and premium cheese layers.", 450.0, "Loaded Fries"))
        list.add(MenuItem("lfries_4", "RF Special Golden Fries", "Gourmet waffle-cut fries topped with beef bacon crumbles, cheese sauce, gold signature sauce, and spring parsley.", 490.0, "Loaded Fries"))

        // 8. Fries (4 items)
        list.add(MenuItem("fries_1", "Classic Salted Fries", "Lightly salted golden crisp potatoes sliced fresh daily, served with signature red ketchup sauce.", 180.0, "Fries"))
        list.add(MenuItem("fries_2", "Masala Gold Fries", "Our absolute best-seller! Crispy hot fries heavily seasoned with a spicy aromatic local peri-crusted masala mix.", 200.0, "Fries"))
        list.add(MenuItem("fries_3", "Garlic Mayo Dipped Fries", "Chilled piping hot fries seasoned and served with an extra-large dipping sauce bowl of fresh creamy golden garlic mayo.", 220.0, "Fries"))
        list.add(MenuItem("fries_4", "Curly Golden Fries", "Pristine twisted curly potato fries with a seasoned breaded coating, fried to a beautiful rich deep-golden mahogany.", 260.0, "Fries"))

        // 9. Nuggets (3 items)
        list.add(MenuItem("nugget_1", "Crispy Chicken Nuggets", "Premium quality all-breast tender chicken bites wrapped in a crisp, golden-puffed, flaky tempura batter (6 Pieces).", 280.0, "Nuggets"))
        list.add(MenuItem("nugget_2", "Royal Gold Nuggets", "Super-sized batch of crispy chicken nuggets seasoned with garlic powder, onion salt, served in a gold box (10 Pieces).", 420.0, "Nuggets"))
        list.add(MenuItem("nugget_3", "Spicy Fire Nuggets", "Tempura nuggets injected with local green chili paste, coated in crunchy red-hot dust, served with dynamic sweet-sour dip.", 320.0, "Nuggets"))

        // 10. Chicken Samosa (2 items)
        list.add(MenuItem("csamosa_1", "Crunchy Samosa Double", "Two ultra-flaky crispy triangular pastries stuffed with roasted shredded chicken, spring peas, and fragrant cumin spices.", 140.0, "Chicken Samosa"))
        list.add(MenuItem("csamosa_2", "Spicy Mint Samosa Wrap", "Delicious crispy street samosas crushed and layered with spicy green mint yogurt sauce and crisp red cabbage shreds.", 160.0, "Chicken Samosa"))

        // 11. Cream Samosa (2 items)
        list.add(MenuItem("creams_1", "Sweet Cream Samosa", "Delicate flaky crispy sweet pastries filled with a rich sweetened cardamom-infused local fresh thick cream.", 180.0, "Cream Samosa"))
        list.add(MenuItem("creams_2", "Cheesy Samosa Delight", "Warm flaky samosas loaded inside with dynamic melting sweet cream-cheese paste, golden brown and crisp.", 200.0, "Cream Samosa"))

        // 12. Kebabs (3 items)
        list.add(MenuItem("kebab_1", "Shami Gold Kebab Pair", "Classic pair of golden lintel-chicken pan patties spiced with cloves and cardamom, crisp-fried in a delicate egg wash.", 190.0, "Kebabs"))
        list.add(MenuItem("kebab_2", "Seekh Gold Kebab Trio", "Three oven-baked skewers of hand-minced tender beef infused with chili flakes, cilantro leaves, and coriander.", 380.0, "Kebabs"))
        list.add(MenuItem("kebab_3", "Chapli Kebab RF Deluxe", "Signature giant flat Pashtun-style ground beef patty mixed with pomegranate seeds, tomatoes, onions, fried in beef fat.", 340.0, "Kebabs"))

        // 13. Biryani (3 items)
        list.add(MenuItem("biryani_1", "RF Gold Chicken Biryani", "Aromatic long-grained basmati rice layered with juicy masala-stewed chicken, saffron infusion, hard-boiled egg, and mint.", 390.0, "Biryani"))
        list.add(MenuItem("biryani_2", "Special Masala Biryani", "Extra hot and spicy basmati rice cooked on slow steam with spiced chicken roast, green chilies, and tangy dried plums.", 420.0, "Biryani"))
        list.add(MenuItem("biryani_3", "Shahi Beef Dum Biryani", "Imperial recipe! Melt-in-your-mouth slow pressure-stewed beef cubes layered with premium basmati rice under tight sealed steam.", 480.0, "Biryani"))

        // 14. Potato Snacks (3 items)
        list.add(MenuItem("psnack_1", "Crispy Potato Wedges", "Chunky skin-on Idaho potato wedges lightly battered, salted, and seasoned with wild rosemary leaves, golden-crisp.", 220.0, "Potato Snacks"))
        list.add(MenuItem("psnack_2", "Creamy Potato Croquettes", "Four fluffy mashed potato rolls loaded with cream cheese, coated in breadcrumbs, fried to a golden mahogany shell.", 245.0, "Potato Snacks"))
        list.add(MenuItem("psnack_3", "Crunchy Potato Smileys", "Six cute face-stamped crunch potatoes, crisp on the outside, fluffy and buttery inside. Served with ketchup.", 180.0, "Potato Snacks"))

        // 15. Ice Cream (3 items)
        list.add(MenuItem("ice_1", "Golden Kulfa Delight", "Rich local condensed milk-cream bar containing almonds, pistachios, saffron threads, topped with a golden honey glaze.", 180.0, "Ice Cream"))
        list.add(MenuItem("ice_2", "Chocolate Gold Lava Cup", "Warm chocolate mud cake core leaking molten chocolate, topped with a scoop of premium vanilla bean ice cream.", 280.0, "Ice Cream"))
        list.add(MenuItem("ice_3", "Royal Pistachio Scoop", "Two creamy scoops of premium local green farm pistachio nut ice cream, layered with real crushed almonds.", 220.0, "Ice Cream"))

        // 16. Drinks (4 items)
        list.add(MenuItem("drink_1", "Mint Margarita Splash", "Signature cooling summer cooler. Blended ice, fresh spearmint leaves, lemon juice, mineral water, and premium black salt.", 220.0, "Drinks"))
        list.add(MenuItem("drink_2", "Golden Lime Splash", "Freshly squeezed sweet lime juice, cold carbonated white soda water, mixed with crushed ice and organic brown cane syrup.", 190.0, "Drinks"))
        list.add(MenuItem("drink_3", "Mango Lassi Delight", "Rich thick, traditional summer drink made by blending thick yogurt, milk, local sweet ripe honey-mango pulp, and cardamoms.", 240.0, "Drinks"))
        list.add(MenuItem("drink_4", "Premium Carbonated Soda", "Chilled 330ml can of your favorite refreshing fizzy carbonated cola, lemon-lime, or sweet orange refreshment.", 120.0, "Drinks"))

        // Ensure exactly 50+ items
        // Let's check size: 4+4+4+4+3+5+4+4+3+2+2+3+3+3+3+4 = 51 items! That's perfect and satisfies the 50 menu items requirement completely.
        _menuItems.value = list
    }

    companion object {
        @Volatile
        private var INSTANCE: DatabaseService? = null

        fun getInstance(context: Context): DatabaseService {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

data class FcmPayload(
    val title: String,
    val body: String,
    val type: String, // "Order Update" or "Promo"
    val timestamp: Long = System.currentTimeMillis()
)

data class DashboardStats(
    val todayOrders: Int,
    val todayRevenue: Double,
    val weeklyRevenue: Double,
    val monthlyRevenue: Double,
    val pendingOrders: Int,
    val acceptedOrders: Int,
    val deliveredOrders: Int,
    val cancelledOrders: Int
)
