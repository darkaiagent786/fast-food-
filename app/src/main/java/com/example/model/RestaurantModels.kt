package com.example.model

enum class OrderType(val displayName: String) {
    DINE_IN("Dine-In"),
    DELIVERY("Delivery")
}

enum class OrderStatus(val displayName: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    PREPARING("Preparing"),
    DELIVERED("Delivered"),
    REJECTED("Rejected")
}

enum class PaymentMethod(val displayName: String) {
    JAZZCASH("JazzCash"),
    EASYPAISA("EasyPaisa"),
    BANK_TRANSFER("Bank Transfer")
}

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUrl: String = "",
    val isFavorite: Boolean = false,
    val rating: Double = 5.0,
    val reviewsCount: Int = 1
)

data class CartItem(
    val menuItem: MenuItem = MenuItem(),
    val quantity: Int = 1
)

data class Order(
    val id: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val type: OrderType = OrderType.DELIVERY,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.JAZZCASH,
    val paymentVerified: Boolean = false,
    val paymentScreenshotUri: String? = null,
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val reviewText: String? = null,
    val rating: Int? = null,
    val pointsEarned: Int = 0,
    val pointsRedeemed: Int = 0,
    val discountAmount: Double = 0.0
)

data class Feedback(
    val id: String = "",
    val customerName: String = "",
    val text: String = "",
    val rating: Int = 5,
    val timestamp: Long = System.currentTimeMillis()
)
