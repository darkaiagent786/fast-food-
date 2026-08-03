package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.net.Uri
import android.os.Build
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Random
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import coil.compose.AsyncImage
import com.example.R
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.RestaurantViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: RestaurantViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    
    // Auth-Admin Switch (Sandbox Ease of access)
    var isAdminMode by remember { mutableStateOf(false) }
    var showAdminAuthDialog by remember { mutableStateOf(false) }
    
    // Dialog state for item detail
    var selectedDetailItem by remember { mutableStateOf<MenuItem?>(null) }
    // Voice Command state
    var showVoiceAssistant by remember { mutableStateOf(false) }
    // Notifications Drawer state
    var showNotificationList by remember { mutableStateOf(false) }

    // Admin Auth Dialog
    if (showAdminAuthDialog) {
        var pinInput by remember { mutableStateOf("") }
        var isPinError by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showAdminAuthDialog = false },
            title = {
                Text(
                    text = "🔒 Admin Security Authorization",
                    color = PrimaryGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Authorized Personnel Only. Please enter the 4-digit Master Security Key to unlock the admin console.",
                        color = CreamWhite,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { 
                            if (it.length <= 4) pinInput = it
                            isPinError = false
                        },
                        label = { Text("Passcode PIN", color = PrimaryGold) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CreamWhite,
                            unfocusedTextColor = CreamWhite,
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = DeepGray,
                            focusedLabelColor = PrimaryGold,
                            unfocusedLabelColor = LightGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("admin_pin_input")
                    )
                    
                    if (isPinError) {
                        Text(
                            text = "Access Denied: Invalid Security Signature",
                            color = AlertRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "(Hint: Master Admin Passcode is 8888)",
                        color = LightGray,
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput == "8888") {
                            isAdminMode = true
                            viewModel.setTab("admin_dashboard")
                            showAdminAuthDialog = false
                            Toast.makeText(context, "Welcome, Head Restaurant Administrator", Toast.LENGTH_SHORT).show()
                        } else {
                            isPinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                    modifier = Modifier.testTag("admin_auth_confirm")
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminAuthDialog = false }) {
                    Text("Cancel", color = LightGray)
                }
            },
            containerColor = SoftObsidian,
            tonalElevation = 6.dp
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                // Top Identity Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // RF Luxury Logo
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, PrimaryGold, RoundedCornerShape(8.dp))
                                .background(RichBlack)
                        ) {
                            // Render generated image, fallback to icon if not found
                            Image(
                                painter = painterResource(id = R.drawable.img_rf_logo_1781756186415),
                                contentDescription = "RF Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "RAHMAN",
                                color = PrimaryGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "FAST FOOD",
                                color = CreamWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Live Notification Bell
                        IconButton(
                            onClick = { showNotificationList = !showNotificationList },
                            modifier = Modifier.testTag("notifications_bell")
                        ) {
                            Box {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notifications",
                                    tint = PrimaryGold
                                )
                                if (notifications.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(AlertRed, CircleShape)
                                            .align(Alignment.TopEnd)
                                      )
                                }
                            }
                        }

                        // Portal Switch Tab (Customer vs Admin Manager)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DeepGray)
                                .clickable {
                                    if (isAdminMode) {
                                        // Logging out is immediate
                                        isAdminMode = false
                                        viewModel.setTab("menu")
                                        Toast.makeText(context, "Entered Customer Storefront", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Access requires restricted credentials verification
                                        showAdminAuthDialog = true
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("admin_portal_toggle")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAdminMode) Icons.Filled.Storefront else Icons.Filled.Person,
                                    contentDescription = "User Mode",
                                    tint = PrimaryGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAdminMode) "Admin" else "Customer",
                                    color = PrimaryGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Dynamic Notifications Banner Overlay
                AnimatedVisibility(visible = showNotificationList) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DeepGray)
                            .border(BottomBorder(1.dp, WarmGold))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "System Real-Time Feeds",
                                    color = PrimaryGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { viewModel.deleteNotification() }) {
                                    Text("Clear All", color = AlertRed, fontSize = 11.sp)
                                }
                            }
                            if (notifications.isEmpty()) {
                                Text(
                                    text = "No new order or receipt notifications.",
                                    color = LightGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                notifications.take(4).forEach { note ->
                                    Text(
                                        text = note,
                                        color = CreamWhite,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!isAdminMode) {
                // Customer Mode Navigation Bottom Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == "menu" || currentTab == "confirmation",
                        onClick = { viewModel.setTab("menu") },
                        label = { Text("Menu", fontSize = 11.sp) },
                        icon = { Icon(Icons.Filled.RestaurantMenu, contentDescription = "Menu") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RichBlack,
                            selectedTextColor = PrimaryGold,
                            indicatorColor = PrimaryGold,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray
                        ),
                        modifier = Modifier.testTag("nav_menu_tab")
                    )
                    NavigationBarItem(
                        selected = currentTab == "cart",
                        onClick = { viewModel.setTab("cart") },
                        label = { Text("Cart (${cartCount})", fontSize = 11.sp) },
                        icon = { 
                            Box {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                                if (cartCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 6.dp, y = (-6).dp)
                                            .background(AlertRed, CircleShape)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = cartCount.toString(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RichBlack,
                            selectedTextColor = PrimaryGold,
                            indicatorColor = PrimaryGold,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray
                        ),
                        modifier = Modifier.testTag("nav_cart_tab")
                    )
                    NavigationBarItem(
                        selected = currentTab == "history",
                        onClick = { viewModel.setTab("history") },
                        label = { Text("My History", fontSize = 11.sp) },
                        icon = { Icon(Icons.Filled.History, contentDescription = "History") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RichBlack,
                            selectedTextColor = PrimaryGold,
                            indicatorColor = PrimaryGold,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray
                        ),
                        modifier = Modifier.testTag("nav_history_tab")
                    )
                    NavigationBarItem(
                        selected = currentTab == "feedback",
                        onClick = { viewModel.setTab("feedback") },
                        label = { Text("Feedback", fontSize = 11.sp) },
                        icon = { Icon(Icons.Filled.RateReview, contentDescription = "Feedback") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RichBlack,
                            selectedTextColor = PrimaryGold,
                            indicatorColor = PrimaryGold,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray
                        ),
                        modifier = Modifier.testTag("nav_feedback_tab")
                    )
                    NavigationBarItem(
                        selected = currentTab == "profile",
                        onClick = { viewModel.setTab("profile") },
                        label = { Text("Profile", fontSize = 11.sp) },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RichBlack,
                            selectedTextColor = PrimaryGold,
                            indicatorColor = PrimaryGold,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray
                        ),
                        modifier = Modifier.testTag("nav_profile_tab")
                    )
                }
            } else {
                // Merchant Admin Mode Bottom Navigation
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == "admin_dashboard",
                        onClick = { viewModel.setTab("admin_dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp) },
                        icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RichBlack,
                            selectedTextColor = PrimaryGold,
                            indicatorColor = PrimaryGold,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray
                        ),
                        modifier = Modifier.testTag("nav_admin_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentTab == "admin_orders",
                        onClick = { viewModel.setTab("admin_orders") },
                        label = { Text("Order Center", fontSize = 11.sp) },
                        icon = { Icon(Icons.Filled.ReceiptLong, contentDescription = "Orders") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RichBlack,
                            selectedTextColor = PrimaryGold,
                            indicatorColor = PrimaryGold,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray
                        ),
                        modifier = Modifier.testTag("nav_admin_orders")
                    )
                }
            }
        },
        floatingActionButton = {
            if (!isAdminMode) {
                // Floating assistant for simulated Voice inputs
                FloatingActionButton(
                    onClick = { showVoiceAssistant = !showVoiceAssistant },
                    containerColor = PrimaryGold,
                    contentColor = RichBlack,
                    modifier = Modifier.testTag("voice_ordering_fab")
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = "Voice Order Helper")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(RichBlack, SoftObsidian)
                    )
                )
        ) {
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                label = "screen_fade"
            ) { targetTab ->
                when (targetTab) {
                    "menu" -> MenuScreen(
                        viewModel = viewModel,
                        onItemClick = { selectedDetailItem = it }
                    )
                    "cart" -> CartScreen(viewModel = viewModel)
                    "history" -> CustomerHistoryScreen(viewModel = viewModel)
                    "feedback" -> CustomerFeedbackScreen(viewModel = viewModel)
                    "profile" -> CustomerProfileScreen(viewModel = viewModel)
                    "confirmation" -> OrderConfirmationScreen(viewModel = viewModel)
                    "admin_dashboard" -> AdminDashboardScreen(viewModel = viewModel)
                    "admin_orders" -> AdminOrdersScreen(viewModel = viewModel)
                }
            }

            // Voice Ordering Drawer Overlay
            if (showVoiceAssistant) {
                VoiceAssistantDialog(
                    viewModel = viewModel,
                    onDismiss = { showVoiceAssistant = false }
                )
            }

            // Food Detail View details & reviews
            selectedDetailItem?.let { item ->
                ProductDetailDialog(
                    item = item,
                    viewModel = viewModel,
                    onDismiss = { selectedDetailItem = null }
                )
            }
        }
    }
}

// ==================== 1. MENU SCREEN ====================
@Composable
fun MenuScreen(viewModel: RestaurantViewModel, onItemClick: (MenuItem) -> Unit) {
    val categories = viewModel.categories
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Hero Image Banner at top of product lists
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner_1781756209473),
                contentDescription = "Gourmet Fast Food Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, RichBlack.copy(alpha = 0.9f))
                        )
                    )
            )

            val loyaltyPoints by viewModel.userLoyaltyPoints.collectAsState()
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(SuccessGreen.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .border(1.dp, WarmGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.setTab("cart") }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Club Balance",
                        tint = PrimaryGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$loyaltyPoints PTS",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Welcome to Rahman Fast Food",
                    color = PrimaryGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "High-Quality, Golden, Crispy Flavors. Est 2026",
                    color = LightGray,
                    fontSize = 11.sp
                )
            }
        }

        // Search Bar Search menu
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_bar"),
            placeholder = { Text("Search delicious burgers, pizza, shawarma...", color = LightGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryGold) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CreamWhite,
                unfocusedTextColor = CreamWhite,
                focusedBorderColor = PrimaryGold,
                unfocusedBorderColor = DeepGray,
                focusedContainerColor = SoftObsidian,
                unfocusedContainerColor = SoftObsidian
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Categories Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryChip(
                    name = "All",
                    isSelected = selectedCategory == "All",
                    onClick = { viewModel.selectCategory("All") }
                )
            }
            items(categories) { cat ->
                CategoryChip(
                    name = cat,
                    isSelected = selectedCategory == cat,
                    onClick = { viewModel.selectCategory(cat) }
                )
            }
        }

        // Product Listings (Grid with 2 columns styled beautifully)
        if (menuItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Empty",
                        tint = PrimaryGold.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No delicious matches found.", color = LightGray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Chunk the list to simulate structured product grids
                val rows = menuItems.chunked(2)
                items(rows) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MenuItemCard(
                            item = pair[0],
                            onAdd = { viewModel.addToCart(pair[0], 1) },
                            onClick = { onItemClick(pair[0]) },
                            modifier = Modifier.weight(1f)
                        )
                        if (pair.size > 1) {
                            MenuItemCard(
                                item = pair[1],
                                onAdd = { viewModel.addToCart(pair[1], 1) },
                                onClick = { onItemClick(pair[1]) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PrimaryGold else SoftObsidian)
            .border(
                1.dp,
                if (isSelected) PrimaryGold else DeepGray,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            color = if (isSelected) RichBlack else CreamWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getFoodImageResId(category: String, imageUrl: String = ""): Int? {
    val term = if (imageUrl.isNotEmpty()) imageUrl else category
    return when {
        term.contains("Burger", ignoreCase = true) || term == "burger" -> R.drawable.img_gold_burger_1781758956471
        term.contains("Pizza", ignoreCase = true) || term == "pizza" -> R.drawable.img_gold_pizza_1781758971343
        term.contains("Fries", ignoreCase = true) || term == "fries" -> R.drawable.img_gold_fries_1781758990103
        term.contains("Drinks", ignoreCase = true) || term.contains("Drink", ignoreCase = true) || term.contains("Ice Cream", ignoreCase = true) || term == "drink" -> R.drawable.img_gold_drink_1781759008644
        else -> null
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onAdd: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Active spring-driven physical entrance animations mimicking Framer Motion spring physics
    var visible by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(item.id) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "item_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "item_scale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else 50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "item_translate"
    )

    // Generate beautiful placeholder icons for our app to load fast
    val context = LocalContext.current
    Card(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = translationY
            }
            .border(1.dp, DeepGray, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SoftObsidian),
        shape = RoundedCornerShape(12.dp)
    ) {
        val visualCategoryIconName = when {
            item.category.contains("Burger") -> "🍔"
            item.category.contains("Shawarma") -> "🌯"
            item.category.contains("Pizza") -> "🍕"
            item.category.contains("Fries") -> "🍟"
            item.category.contains("Samosa") -> "🥟"
            item.category.contains("Kebab") -> "🍢"
            item.category.contains("Biryani") -> "🍛"
            item.category.contains("Drinks") -> "🥤"
            item.category.contains("Ice Cream") -> "🍨"
            else -> "🍗"
        }

        Column {
            // Visual Image Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
                    .background(DeepGray)
            ) {
                // High contrast icon fallback or premium image
                val visualImageResId = getFoodImageResId(item.category, item.imageUrl)
                if (visualImageResId != null) {
                    Image(
                        painter = painterResource(id = visualImageResId),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // High quality dark-to-translucent gradient mask for rich black harmony
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, RichBlack.copy(alpha = 0.6f))
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = visualCategoryIconName, fontSize = 48.sp)
                    }
                }

                // Small rating flag
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(RichBlack.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = PrimaryGold,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", item.rating),
                        color = CreamWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.name,
                    color = PrimaryGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    color = LightGray,
                    fontSize = 11.sp,
                    maxLines = 2,
                    lineHeight = 13.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PKR ${item.price.toInt()}",
                            color = CreamWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = {
                            onAdd()
                            Toast.makeText(context, "${item.name} added to cart!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .background(PrimaryGold, CircleShape)
                            .testTag("add_to_cart_${item.id}"),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = RichBlack)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Item",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


// ==================== 2. CART SCREEN ====================
@Composable
fun CartScreen(viewModel: RestaurantViewModel) {
    val cart by viewModel.cart.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val customerName by viewModel.customerNameInput.collectAsState()
    val customerPhone by viewModel.customerPhoneInput.collectAsState()
    val deliveryAddress by viewModel.deliveryAddressInput.collectAsState()
    val selectedType by viewModel.orderTypeChoice.collectAsState()
    val selectedPayment by viewModel.paymentMethodChoice.collectAsState()
    val uploadScreenshotUri by viewModel.selectedScreenshotUri.collectAsState()
    val context = LocalContext.current

    if (cart.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Empty",
                    tint = PrimaryGold.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your gold food tray is currently empty.", color = LightGray, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.setTab("menu") },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack)
                ) {
                    Text("Browse Delicious Menu", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Customer Food Tray",
                    color = PrimaryGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Products list
            items(cart) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftObsidian, RoundedCornerShape(10.dp))
                        .border(1.dp, DeepGray, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pair.menuItem.name,
                            color = CreamWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PKR ${pair.menuItem.price.toInt()} each",
                            color = PrimaryGold,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.addToCart(pair.menuItem, -1) }) {
                            Icon(Icons.Default.Remove, "Remove", tint = PrimaryGold)
                        }
                        Text(
                            text = pair.quantity.toString(),
                            color = CreamWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { viewModel.addToCart(pair.menuItem, 1) }) {
                            Icon(Icons.Default.Add, "Add", tint = PrimaryGold)
                        }
                    }
                }
            }

            // Options Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                    border = BorderStroke(1.dp, DeepGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Delivery Preference", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.orderTypeChoice.value = OrderType.DELIVERY }) {
                                RadioButton(
                                    selected = selectedType == OrderType.DELIVERY,
                                    onClick = { viewModel.orderTypeChoice.value = OrderType.DELIVERY },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold)
                                )
                                Text("Home Delivery", color = CreamWhite)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.orderTypeChoice.value = OrderType.DINE_IN }) {
                                RadioButton(
                                    selected = selectedType == OrderType.DINE_IN,
                                    onClick = { viewModel.orderTypeChoice.value = OrderType.DINE_IN },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryGold)
                                )
                                Text("Dine-In", color = CreamWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Customer Contact Details", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { viewModel.customerNameInput.value = it },
                            label = { Text("Customer Name", color = LightGray) },
                            textStyle = TextStyle(color = CreamWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedBorderColor = DeepGray),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("cart_input_name")
                        )

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { viewModel.customerPhoneInput.value = it },
                            label = { Text("Phone Number", color = LightGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            textStyle = TextStyle(color = CreamWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedBorderColor = DeepGray),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("cart_input_phone")
                        )

                        if (selectedType == OrderType.DELIVERY) {
                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = { viewModel.deliveryAddressInput.value = it },
                                label = { Text("Delivery Address", color = LightGray) },
                                textStyle = TextStyle(color = CreamWhite),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedBorderColor = DeepGray),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("cart_input_address")
                            )
                        }
                    }
                }
            }

            // Payment Platform Features
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                    border = BorderStroke(1.dp, DeepGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Payment Gateways (Gold Std)", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaymentGateButton(
                                name = "JazzCash",
                                isSelected = selectedPayment == PaymentMethod.JAZZCASH,
                                onClick = { viewModel.paymentMethodChoice.value = PaymentMethod.JAZZCASH }
                            )
                            PaymentGateButton(
                                name = "EasyPaisa",
                                isSelected = selectedPayment == PaymentMethod.EASYPAISA,
                                onClick = { viewModel.paymentMethodChoice.value = PaymentMethod.EASYPAISA }
                            )
                            PaymentGateButton(
                                name = "Bank Transfer",
                                isSelected = selectedPayment == PaymentMethod.BANK_TRANSFER,
                                onClick = { viewModel.paymentMethodChoice.value = PaymentMethod.BANK_TRANSFER }
                            )
                        }

                        // Gateway visual instruction block
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DeepGray, RoundedCornerShape(8.dp))
                                .border(1.dp, WarmGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (selectedPayment == PaymentMethod.BANK_TRANSFER) "Bank Al-Habib Ltd" else selectedPayment.displayName,
                                    color = PrimaryGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = when (selectedPayment) {
                                        PaymentMethod.JAZZCASH -> "Till ID: 981273 (Rahman Fast Foods)\nAccount Title: Hamza Rahman\nPhone: 0321-7654321"
                                        PaymentMethod.EASYPAISA -> "Till ID: 41208 (Rahman Fast Foods)\nAccount Title: Hamza Rahman\nPhone: 0321-7654321"
                                        PaymentMethod.BANK_TRANSFER -> "IBAN: PK82ALHB00213098127391\nAccount: 01229081273\nAccount Title: Rahman Fast Foods"
                                    },
                                    color = CreamWhite,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                
                                // Simulated QR Code image container
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCode2, "QR", tint = PrimaryGold, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scan the QR generator or Till ID above to send PKR ${cartTotal.toInt()}",
                                        color = LightGray,
                                        fontSize = 10.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Screenshot upload interface
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Add Payment Proof (Receipt Screenshot)", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    // Simulated receipt upload - sets a unique transaction URI value to satisfy database proof requirement
                                    viewModel.setScreenshot("content://media/external/images/payments/tx_proof_${Random().nextInt(9999)}.png")
                                    Toast.makeText(context, "Payment receipt screenshot attached successfully!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepGray, contentColor = PrimaryGold),
                                border = BorderStroke(1.dp, WarmGold)
                            ) {
                                Icon(Icons.Default.UploadFile, "Upload")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Grab Receipt", fontSize = 11.sp)
                            }

                            if (uploadScreenshotUri != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, "Uploaded", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Proof Attached", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(onClick = { viewModel.setScreenshot(null) }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Cancel, "Cancel", tint = AlertRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else {
                                Text("No proof screenshot uploaded", color = AlertRed, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Loyalty Points Redemption
            item {
                val availablePoints by viewModel.userLoyaltyPoints.collectAsState()
                val redeemChecked by viewModel.redeemPointsChecked.collectAsState()
                val maxRedeemable by viewModel.maxRedeemablePoints.collectAsState()
                val potentialPointsEarned = (cartTotal * 0.05).toInt()

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("loyalty_points_card"),
                    colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = "Loyalty Points",
                                    tint = PrimaryGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Rahman Loyalty Club",
                                        color = PrimaryGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Available Balance: $availablePoints PTS",
                                        color = CreamWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Switch(
                                checked = redeemChecked,
                                onCheckedChange = { viewModel.redeemPointsChecked.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RichBlack,
                                    checkedTrackColor = PrimaryGold,
                                    uncheckedThumbColor = LightGray,
                                    uncheckedTrackColor = DeepGray
                                ),
                                enabled = availablePoints > 0
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = if (redeemChecked) {
                                "✓ Redeeming $maxRedeemable points (Saved PKR $maxRedeemable.00 on this order!)."
                            } else if (availablePoints > 0) {
                                "Turn on switch to redeem your available points for an immediate discount (1 Point = 1 PKR)."
                            } else {
                                "Place order to start earning points. No points available for redemption yet."
                            },
                            color = if (redeemChecked) SuccessGreen else LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Divider(color = DeepGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Loyalty Points to Earn upon delivery (5%):",
                                color = LightGray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "+$potentialPointsEarned PTS",
                                color = PrimaryGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Total and Checkout Action
            item {
                val discountAmount by viewModel.pointsDiscountAmount.collectAsState()
                val finalTotal by viewModel.finalCheckoutTotal.collectAsState()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftObsidian, RoundedCornerShape(12.dp))
                        .border(1.dp, DeepGray, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Order Subtotal:", color = LightGray, fontSize = 11.sp)
                        Text("PKR ${cartTotal.toInt()}", color = CreamWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (discountAmount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Loyalty Club Discount:", color = SuccessGreen, fontSize = 11.sp)
                            Text("-PKR ${discountAmount.toInt()}", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Divider(color = DeepGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Final Total", color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("PKR ${finalTotal.toInt()}", color = CreamWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.checkout() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("checkout_submit_button")
                        ) {
                            Text("Dispatch Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Check, "Go")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentGateButton(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PrimaryGold else DeepGray)
            .border(1.dp, if (isSelected) PrimaryGold else GrayBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(name, color = if (isSelected) RichBlack else CreamWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ==================== 3. ORDER CONFIRMATION SCREEN ====================
@Composable
fun OrderConfirmationScreen(viewModel: RestaurantViewModel) {
    val order by viewModel.lastPlacedOrder.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        order?.let { activeOrder ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(90.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ORDER DISPATCHED SUCCESS!",
                    color = PrimaryGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Order Serial ID: ${activeOrder.id}",
                    color = CreamWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Awaiting store merchant verification of ${activeOrder.paymentMethod.displayName}",
                    color = LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                // Active status tracker visual
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftObsidian, RoundedCornerShape(10.dp))
                        .border(1.dp, DeepGray, RoundedCornerShape(10.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mode: ${activeOrder.type.displayName}", color = CreamWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Total: PKR ${activeOrder.totalAmount.toInt()}", color = PrimaryGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = DeepGray, modifier = Modifier.padding(vertical = 10.dp))
                        
                        // Vertical tracker step indicators
                        StepTracker(currentStatus = activeOrder.status)
                    }
                }

                if (activeOrder.pointsRedeemed > 0 || activeOrder.pointsEarned > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftObsidian, RoundedCornerShape(10.dp))
                            .border(1.dp, WarmGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "Loyalty Club Summary",
                                color = PrimaryGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (activeOrder.pointsRedeemed > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Points Redeemed:", color = LightGray, fontSize = 11.sp)
                                    Text("-${activeOrder.pointsRedeemed} PTS (Saved PKR ${activeOrder.discountAmount.toInt()})", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (activeOrder.pointsEarned > 0) {
                                if (activeOrder.pointsRedeemed > 0) Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Points to Earn (upon Delivery):", color = LightGray, fontSize = 11.sp)
                                    Text("+${activeOrder.pointsEarned} PTS", color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // WhatsApp Cloud API Mock Trigger Integration Helper
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val url = viewModel.buildWhatsAppShareMessage(activeOrder)
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp integration link prepared! (Structure saved for Production)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(painterResource(id = android.R.drawable.stat_sys_phone_call), "WhatsApp", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger WhatsApp Cloud API Dispatch", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = { viewModel.setTab("menu") },
                    modifier = Modifier.testTag("dismiss_confirmation")
                ) {
                    Text("Back to Browse Foods", color = PrimaryGold)
                }
            }
        } ?: run {
            Text("No order checkout loaded.", color = LightGray)
        }
    }
}

@Composable
fun StepTracker(currentStatus: OrderStatus) {
    val steps = listOf(OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.DELIVERED)
    val curIndex = steps.indexOf(currentStatus)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { index, status ->
            val isActive = index <= curIndex
            val color = if (isActive) {
                if (status == OrderStatus.DELIVERED) SuccessGreen else PrimaryGold
            } else LightGray.copy(alpha = 0.4f)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when(status) {
                        OrderStatus.PENDING -> "Order Placed & Awaiting Verification"
                        OrderStatus.ACCEPTED -> "Order Approved by Kitchen Merchant"
                        OrderStatus.PREPARING -> "Chef Preparing Your Golden Meal"
                        OrderStatus.DELIVERED -> "Delivered! Enjoy Your Flavor Feast!"
                        else -> ""
                    },
                    color = if (isActive) CreamWhite else LightGray.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = if (status == currentStatus) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}


// ==================== 4. CUSTOMER HISTORY SCREEN ====================
@Composable
fun CustomerHistoryScreen(viewModel: RestaurantViewModel) {
    val history by viewModel.customerOrderHistory.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Golden Orders History", color = PrimaryGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No previous order records found.", color = LightGray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(history) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                        border = BorderStroke(1.dp, DeepGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("ID: ${order.id}", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                // Status badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (order.status) {
                                                OrderStatus.DELIVERED -> SuccessGreen.copy(alpha = 0.2f)
                                                OrderStatus.PENDING -> PendingAmber.copy(alpha = 0.2f)
                                                else -> PrimaryGold.copy(alpha = 0.2f)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = order.status.displayName,
                                        color = when (order.status) {
                                            OrderStatus.DELIVERED -> SuccessGreen
                                            OrderStatus.PENDING -> PendingAmber
                                            else -> PrimaryGold
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            // Items summary
                            val itemsDesc = order.items.joinToString(", ") { "${it.menuItem.name} (x${it.quantity})" }
                            Text(itemsDesc, color = CreamWhite, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                            if (order.pointsRedeemed > 0 || order.pointsEarned > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (order.pointsRedeemed > 0) {
                                        Text(
                                            text = "Redeemed: -${order.pointsRedeemed} PTS",
                                            color = SuccessGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (order.pointsEarned > 0) {
                                        Text(
                                            text = "Earn: +${order.pointsEarned} PTS ${if (order.status == OrderStatus.DELIVERED) "(Credited)" else "(Pending)"}",
                                            color = PrimaryGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Paid: PKR ${order.totalAmount.toInt()}", color = LightGray, fontSize = 11.sp)
                                
                                Button(
                                    onClick = { 
                                        viewModel.repeatOrder(order)
                                        Toast.makeText(context, "Items reloaded into checkout!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepGray, contentColor = PrimaryGold),
                                    modifier = Modifier.height(28.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Filled.Repeat, "Repeat", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Repeat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==================== 5a. CUSTOMER PROFILE SCREEN ====================
@Composable
fun CustomerProfileScreen(viewModel: RestaurantViewModel) {
    val userName by viewModel.userName.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val loyaltyPoints by viewModel.userLoyaltyPoints.collectAsState()
    val orderHistory by viewModel.customerOrderHistory.collectAsState()
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }

    // Dialog state
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }

    if (showEditDialog) {
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("edit_profile_dialog"),
                colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                border = BorderStroke(1.dp, PrimaryGold),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Edit Profile Details",
                        color = PrimaryGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name", color = LightGray) },
                        textStyle = TextStyle(color = CreamWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = DeepGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number", color = LightGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = TextStyle(color = CreamWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = DeepGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Delivery Address", color = LightGray) },
                        textStyle = TextStyle(color = CreamWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = DeepGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_address"),
                        minLines = 2,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showEditDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepGray, contentColor = CreamWhite),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (editName.isBlank() || editPhone.isBlank() || editAddress.isBlank()) {
                                    Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.updateProfile(editName, editPhone, editAddress)
                                    showEditDialog = false
                                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("save_profile_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Intro Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_identity_card"),
                colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                border = BorderStroke(1.dp, DeepGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Initials / Avatar representation
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(PrimaryGold, Color(0xFF9E7E3C))
                                ),
                                CircleShape
                            )
                            .border(1.5.dp, CreamWhite, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val monogram = if (userName.length >= 2) {
                            userName.substring(0, 2).uppercase()
                        } else if (userName.isNotEmpty()) {
                            userName.substring(0, 1).uppercase()
                        } else {
                            "RF"
                        }
                        Text(
                            text = monogram,
                            color = RichBlack,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = userName,
                        color = CreamWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = userPhone,
                        color = LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = DeepGray, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Address",
                            tint = PrimaryGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = userAddress,
                            color = CreamWhite,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            editName = userName
                            editPhone = userPhone
                            editAddress = userAddress
                            showEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGray, contentColor = PrimaryGold),
                        border = BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("edit_profile_button"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Filled.Edit, "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Personal Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Loyalty Card (The Rewards display)
        item {
            val currentTier = when {
                loyaltyPoints >= 1000 -> "VIP Platinum Master"
                loyaltyPoints >= 500 -> "Club Silver VIP"
                else -> "Club Bronze Standard"
            }

            val progressValue = when {
                loyaltyPoints >= 1000 -> 1f
                loyaltyPoints >= 500 -> ((loyaltyPoints - 500).toFloat() / 500f).coerceIn(0f, 1f)
                else -> (loyaltyPoints.toFloat() / 500f).coerceIn(0f, 1f)
            }

            val nextTierTarget = when {
                loyaltyPoints >= 1000 -> ""
                loyaltyPoints >= 500 -> "1000 PTS (Platinum)"
                else -> "500 PTS (Silver)"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rewards_metrics_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFE5B84A), Color(0xFFC48B25), Color(0xFF96631E))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RAHMAN LOYALTY CLUB",
                                    color = RichBlack,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = currentTier,
                                        tint = RichBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentTier.uppercase(),
                                        color = RichBlack,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            // Elegant VIP graphic element
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RichBlack.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("VIP CARD", color = RichBlack, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Accumulated Balance",
                            color = RichBlack.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$loyaltyPoints",
                                color = RichBlack,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PTS",
                                color = RichBlack,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // Estimated PKR savings value
                        Text(
                            text = "= PKR $loyaltyPoints.00 Instant Checkout Credit",
                            color = RichBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress to next tier
                        if (loyaltyPoints < 1000) {
                            val ptsToNext = if (loyaltyPoints >= 500) 1000 - loyaltyPoints else 500 - loyaltyPoints
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$ptsToNext PTS to next target",
                                    color = RichBlack.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = nextTierTarget,
                                    color = RichBlack.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progressValue },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RichBlack,
                                trackColor = RichBlack.copy(alpha = 0.15f)
                            )
                        } else {
                            Text(
                                text = "★ Maximum Tier Unlocked! Platinum Elite Status",
                                color = RichBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // Real-time FCM Notifications Services Dashboard
        item {
            FCMConsoleSection(viewModel = viewModel)
        }

        // How it works guidelines Info card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("rewards_info_card"),
                colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                border = BorderStroke(1.dp, DeepGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "How Rewards Work",
                        color = PrimaryGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("1.", color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp))
                        Text("Get 5% points back as cashback on every checkout total (credited upon delivery).", color = LightGray, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("2.", color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp))
                        Text("Toggle the redemption switch in your Cart to discount your order instantly. 1 point = 1 PKR.", color = LightGray, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("3.", color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(16.dp))
                        Text("Any cancelled or rejected order fully restores your redeemed loyalty points.", color = LightGray, fontSize = 11.sp)
                    }
                }
            }
        }

        // Loyalty Points Activity Ledger LedgerTitle
        item {
            Text(
                text = "Points & Rewards Transaction History",
                color = PrimaryGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Ledger Items matching
        val ledgerItems = mutableListOf<LoyaltyTransaction>()

        // 1. Map actual order history
        orderHistory.forEach { order ->
            // If redeemed points
            if (order.pointsRedeemed > 0) {
                ledgerItems.add(
                    LoyaltyTransaction(
                        title = "Checkout Order Discount",
                        pointsDisp = "-${order.pointsRedeemed} PTS",
                        isPositive = false,
                        dateTime = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(order.timestamp)),
                        details = "Saved PKR ${order.discountAmount.toInt()} on Order #${order.id}"
                    )
                )
            }
            // If earned points
            if (order.pointsEarned > 0) {
                val isDelivered = order.status == OrderStatus.DELIVERED
                val isRejected = order.status == OrderStatus.REJECTED
                val textSuffix = when {
                    isDelivered -> " (Credited)"
                    isRejected -> " (Cancelled)"
                    else -> " (Pending Delivery)"
                }
                
                ledgerItems.add(
                    LoyaltyTransaction(
                        title = "5% Fast Food Cashback",
                        pointsDisp = "+${order.pointsEarned} PTS$textSuffix",
                        isPositive = !isRejected,
                        isPending = !isDelivered && !isRejected,
                        isRefunded = isRejected,
                        dateTime = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(order.timestamp)),
                        details = "Earned on PKR ${order.totalAmount.toInt()} spent on Order #${order.id}"
                    )
                )
            }
            
            // If order was rejected and spent points refunded
            if (order.status == OrderStatus.REJECTED && order.pointsRedeemed > 0) {
                ledgerItems.add(
                    LoyaltyTransaction(
                        title = "Loyalty Point Reclaimed",
                        pointsDisp = "+${order.pointsRedeemed} PTS",
                        isPositive = true,
                        dateTime = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(order.timestamp)),
                        details = "Refunded returned points for Order #${order.id}"
                    )
                )
            }
        }

        // 2. Predefined standard seed entries
        ledgerItems.add(
            LoyaltyTransaction(
                title = "Welcome SignUp Reward",
                pointsDisp = "+200 PTS",
                isPositive = true,
                dateTime = "15 Dec 2025 • 12:30 PM",
                details = "Membership registration introductory gift"
            )
        )
        ledgerItems.add(
            LoyaltyTransaction(
                title = "Loyalty Account Activation",
                pointsDisp = "+150 PTS",
                isPositive = true,
                dateTime = "10 Jan 2026 • 04:15 PM",
                details = "Opening balance bonus check"
            )
        )

        // Sort ledger items chronologically if possible, or just render
        items(ledgerItems) { tx ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("loyalty_tx_card_${tx.title.replace(' ', '_').lowercase()}"),
                colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                border = BorderStroke(1.dp, if (tx.isPending) WarmGold.copy(alpha = 0.4f) else DeepGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.title,
                            color = CreamWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tx.details,
                            color = LightGray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        Text(
                            text = tx.dateTime,
                            color = LightGray.copy(alpha = 0.6f),
                            fontSize = 8.sp
                        )
                    }

                    val textColor = when {
                        tx.isRefunded -> LightGray
                        tx.isPending -> PrimaryGold
                        tx.isPositive -> SuccessGreen
                        else -> AlertRed
                    }

                    Text(
                        text = tx.pointsDisp,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Simple supporting model
data class LoyaltyTransaction(
    val title: String,
    val pointsDisp: String,
    val isPositive: Boolean,
    val isPending: Boolean = false,
    val isRefunded: Boolean = false,
    val dateTime: String,
    val details: String
)


// ==================== FCM MESSAGING SERVICE CONTROL DECK ====================
@Composable
fun FCMConsoleSection(viewModel: RestaurantViewModel) {
    val fcmToken by viewModel.fcmToken.collectAsState()
    val fcmPayloads by viewModel.fcmPayloads.collectAsState()
    val context = LocalContext.current

    // Admin mock states
    var mockTitle by remember { mutableStateOf("Craving Double Cheese? 🍔") }
    var mockBody by remember { mutableStateOf("Get 20% cashback on your next order today with code GOLDEN20!") }
    var selectedChannel by remember { mutableStateOf(com.example.fcm.NotificationHelper.CHANNEL_PROMOS) }
    
    // Notification permission launcher for Android 13+
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "System push notification permissions GRANTED!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications blocked. Enable manually in system preferences.", Toast.LENGTH_LONG).show()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fcm_console_panel"),
        colors = CardDefaults.cardColors(containerColor = SoftObsidian),
        border = BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = "FCM Active",
                        tint = PrimaryGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FCM Cloud Services Console",
                        color = PrimaryGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (hasPermission) SuccessGreen.copy(alpha = 0.2f) else AlertRed.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (hasPermission) "ACTIVE" else "PERM REQUIRED",
                        color = if (hasPermission) SuccessGreen else AlertRed,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Token widget
            Text(
                text = "FCM REGISTRATION TOKEN (DEVICE TARGET ID)",
                color = LightGray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(RichBlack)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fcmToken,
                    color = CreamWhite,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("FCM Token", fcmToken)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "FCM Device Token copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy Token",
                        tint = PrimaryGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Notification permission prompt if not granted
            if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.2f), contentColor = AlertRed),
                    border = BorderStroke(1.dp, AlertRed),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Grant Android Push Permission", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DeepGray, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Admin broadcaster trigger
            Text(
                text = "ADMIN FCM BROADCAST SIMULATOR",
                color = PrimaryGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Text inputs
            OutlinedTextField(
                value = mockTitle,
                onValueChange = { mockTitle = it },
                label = { Text("Notification Title", color = LightGray, fontSize = 10.sp) },
                textStyle = TextStyle(color = CreamWhite, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = DeepGray
                ),
                modifier = Modifier.fillMaxWidth().testTag("fcm_input_title"),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = mockBody,
                onValueChange = { mockBody = it },
                label = { Text("Notification Body Text", color = LightGray, fontSize = 10.sp) },
                textStyle = TextStyle(color = CreamWhite, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = DeepGray
                ),
                modifier = Modifier.fillMaxWidth().testTag("fcm_input_body"),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Select channel row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedChannel = com.example.fcm.NotificationHelper.CHANNEL_PROMOS },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedChannel == com.example.fcm.NotificationHelper.CHANNEL_PROMOS) PrimaryGold else DeepGray,
                        contentColor = if (selectedChannel == com.example.fcm.NotificationHelper.CHANNEL_PROMOS) RichBlack else CreamWhite
                    ),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Promo Channel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { selectedChannel = com.example.fcm.NotificationHelper.CHANNEL_ORDERS },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedChannel == com.example.fcm.NotificationHelper.CHANNEL_ORDERS) PrimaryGold else DeepGray,
                        contentColor = if (selectedChannel == com.example.fcm.NotificationHelper.CHANNEL_ORDERS) RichBlack else CreamWhite
                    ),
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Orders Channel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (mockTitle.isBlank() || mockBody.isBlank()) {
                        Toast.makeText(context, "Please enter title and body content first", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.simulateInboundFCMMessage(context, mockTitle, mockBody, selectedChannel)
                        Toast.makeText(context, "FCM Push Broadcast Sent Successfully!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("broadcast_fcm_button")
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send Broadcast", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Broadcast FCM Push Notification", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Received payload streams
            if (fcmPayloads.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DeepGray, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INCOMING FCM TRANSIT PAYLOADS (${fcmPayloads.size})",
                        color = LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CLEAR LOGS",
                        color = PrimaryGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.clearFcmLogs() }
                            .padding(4.dp)
                            .testTag("clear_fcm_logs_button")
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fcmPayloads.take(5).forEach { payload ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = RichBlack),
                            border = BorderStroke(0.5.dp, DeepGray)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = payload.title,
                                        color = CreamWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = payload.type.uppercase(),
                                        color = if (payload.type.contains("Order")) PrimaryGold else SuccessGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = payload.body,
                                    color = LightGray,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("hh:mm:ss a • dd MMM", Locale.getDefault()).format(Date(payload.timestamp)),
                                    color = LightGray.copy(alpha = 0.5f),
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==================== 5. CUSTOMER FEEDBACK SCREEN ====================
@Composable
fun CustomerFeedbackScreen(viewModel: RestaurantViewModel) {
    val feedbacks by viewModel.feedbacks.collectAsState()
    var rating by remember { mutableStateOf(5) }
    var textMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Share Your Feedback", color = PrimaryGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Directly shared with Rahman Fast Food master kitchen", color = LightGray, fontSize = 11.sp)
        
        Spacer(modifier = Modifier.height(14.dp))

        // Feedback Entry Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SoftObsidian),
            border = BorderStroke(1.dp, DeepGray)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Master Rating", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$i Stars",
                                tint = if (i <= rating) PrimaryGold else LightGray.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = textMessage,
                    onValueChange = { textMessage = it },
                    placeholder = { Text("What made your dining experience gold std?", color = LightGray) },
                    textStyle = TextStyle(color = CreamWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedBorderColor = DeepGray),
                    modifier = Modifier.fillMaxWidth().height(90.dp).testTag("feedback_msg_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (textMessage.isNotBlank()) {
                            viewModel.submitFeedback(textMessage, rating)
                            textMessage = ""
                            Toast.makeText(context, "Feedback dispatched to administration. Thank you!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                    modifier = Modifier.align(Alignment.End).testTag("feedback_submit_btn")
                ) {
                    Text("Submit Feedback", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Recent Customer Testimonials", color = PrimaryGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(feedbacks) { f ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepGray, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = f.customerName, color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Row {
                                repeat(f.rating) {
                                    Icon(Icons.Filled.Star, "Star", tint = PrimaryGold, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                        Text(text = "\"${f.text}\"", color = CreamWhite, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}


// ==================== 6. ADMIN DASHBOARD SCREEN ====================
@Composable
fun AdminDashboardScreen(viewModel: RestaurantViewModel) {
    val stats by viewModel.adminStats.collectAsState()
    val allItems by viewModel.allMenuItems.collectAsState()
    val categories = viewModel.categories
    val context = LocalContext.current

    var dashboardTab by remember { mutableStateOf("analytics") } // "analytics", "branding", "menu"
    
    // Add/Edit Dialog toggles
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) } // null means "Add New Item"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Sub-tabs Selection Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(DeepGray, RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val tabs = listOf("analytics" to "📊 Analytics", "branding" to "🎨 Branding", "menu" to "🍔 Menu Editor")
            tabs.forEach { (route, label) ->
                val isSel = dashboardTab == route
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) PrimaryGold else Color.Transparent)
                        .clickable { dashboardTab = route }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSel) RichBlack else LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Active Tab Display
        when (dashboardTab) {
            "analytics" -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Merchant Analytics Bar", color = PrimaryGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    // Today's Revenue Indicators
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardMetricCard(
                            title = "Today's Revenue",
                            value = "PKR ${stats.todayRevenue.toInt()}",
                            subtitle = "Verified Completed Payments",
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Today's Orders",
                            value = "${stats.todayOrders} Orders",
                            subtitle = "Volume load current count",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardMetricCard(
                            title = "Weekly Volume",
                            value = "PKR ${stats.weeklyRevenue.toInt()}",
                            subtitle = "Verified Completed Payments",
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "Monthly Volume",
                            value = "PKR ${stats.monthlyRevenue.toInt()}",
                            subtitle = "Averages 500+ orders/day",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Order Pipelines status", color = PrimaryGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PipeStatusCell(name = "Pending", count = stats.pendingOrders, color = PendingAmber, modifier = Modifier.weight(1f))
                        PipeStatusCell(name = "Active Prep", count = stats.acceptedOrders, color = PrimaryGold, modifier = Modifier.weight(1f))
                        PipeStatusCell(name = "Delivered", count = stats.deliveredOrders, color = SuccessGreen, modifier = Modifier.weight(1f))
                        PipeStatusCell(name = "Cancelled", count = stats.cancelledOrders, color = AlertRed, modifier = Modifier.weight(1f))
                    }

                    // WhatsApp trigger integration logs
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("WhatsApp Gateway Integration Details", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(
                                text = "Cloud API Trigger payload mapped. Scale limit 500+ daily orders supported via firestore trigger triggers automatically directed to customer whatsapp numbers.",
                                color = LightGray,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            "branding" -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("App Color Theme Controller", color = PrimaryGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Select any premium color palette below to instantly style the entire application experience.", color = LightGray, fontSize = 11.sp)

                    AppColorTheme.values().forEach { theme ->
                        val isSelected = currentAppTheme == theme
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentAppTheme = theme
                                    Toast.makeText(context, "Theme updated: ${theme.displayName}", Toast.LENGTH_SHORT).show()
                                }
                                .border(
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) theme.accentColor else DeepGray
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) theme.chipBg else SoftObsidian
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = theme.displayName,
                                        color = if (isSelected) theme.accentColor else CreamWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isSelected) "Active System Palette" else "Tap to apply theme",
                                        color = LightGray.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                                
                                // Beautiful dynamic color preview bubbles
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(theme.accentColor, CircleShape)
                                            .border(1.dp, CreamWhite.copy(alpha = 0.5f), CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(theme.chipBg, CircleShape)
                                            .border(1.dp, CreamWhite.copy(alpha = 0.5f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "menu" -> {
                var searchAllQuery by remember { mutableStateOf("") }
                val filteredAllItems = remember(allItems, searchAllQuery) {
                    allItems.filter {
                        searchAllQuery.isEmpty() ||
                        it.name.contains(searchAllQuery, ignoreCase = true) ||
                        it.category.contains(searchAllQuery, ignoreCase = true)
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Menu Configuration Database", color = PrimaryGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Database Records: ${allItems.size} dishes listed", color = LightGray, fontSize = 10.sp)
                        }
                        
                        Button(
                            onClick = {
                                editingItem = null
                                showEditDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("admin_add_item_btn")
                        ) {
                            Icon(Icons.Filled.Add, "Add Item", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Dish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Search input
                    OutlinedTextField(
                        value = searchAllQuery,
                        onValueChange = { searchAllQuery = it },
                        placeholder = { Text("Filter database by name or category...", color = LightGray, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = PrimaryGold, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchAllQuery.isNotEmpty()) {
                                IconButton(onClick = { searchAllQuery = "" }) {
                                    Icon(Icons.Filled.Clear, "Clear", tint = LightGray, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        textStyle = TextStyle(color = CreamWhite, fontSize = 12.sp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = DeepGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("menu_db_search")
                    )

                    if (filteredAllItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No dishes match search search criteria.", color = LightGray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredAllItems) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().border(1.dp, DeepGray, RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.cardColors(containerColor = SoftObsidian),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Image thumbnail
                                        val thumbRes = getFoodImageResId(item.category, item.imageUrl)
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DeepGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (thumbRes != null) {
                                                Image(
                                                    painter = painterResource(id = thumbRes),
                                                    contentDescription = item.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text("🍽️", fontSize = 24.sp)
                                            }
                                        }

                                        // Info text
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                color = CreamWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = item.category,
                                                    color = PrimaryGold,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "•",
                                                    color = LightGray,
                                                    fontSize = 9.sp
                                                )
                                                Text(
                                                    text = "PKR ${item.price.toInt()}",
                                                    color = LightGray,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = item.description,
                                                color = LightGray.copy(alpha = 0.8f),
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Manage Buttons
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    editingItem = item
                                                    showEditDialog = true
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = "Edit Item",
                                                    tint = PrimaryGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteMenuItem(item.id)
                                                    Toast.makeText(context, "${item.name} deleted from database", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Delete Item",
                                                    tint = AlertRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dynamic Edit / Add Dialog Window
    if (showEditDialog) {
        var fieldName by remember { mutableStateOf(editingItem?.name ?: "") }
        var fieldDescription by remember { mutableStateOf(editingItem?.description ?: "") }
        var fieldPrice by remember { mutableStateOf(editingItem?.price?.toInt()?.toString() ?: "") }
        var fieldCategory by remember { mutableStateOf(editingItem?.category ?: categories.firstOrNull() ?: "Burgers") }
        var fieldImageRef by remember { mutableStateOf(editingItem?.imageUrl ?: "burger") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (editingItem == null) "✨ Add Culinary Dish" else "⚙️ Edit Dish Configuration",
                    color = PrimaryGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    // Name input
                    OutlinedTextField(
                        value = fieldName,
                        onValueChange = { fieldName = it },
                        label = { Text("Dish Title", color = PrimaryGold, fontSize = 11.sp) },
                        textStyle = TextStyle(color = CreamWhite, fontSize = 12.sp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = DeepGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_name")
                    )

                    // Description input
                    OutlinedTextField(
                        value = fieldDescription,
                        onValueChange = { fieldDescription = it },
                        label = { Text("Flavor Description", color = PrimaryGold, fontSize = 11.sp) },
                        textStyle = TextStyle(color = CreamWhite, fontSize = 12.sp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = DeepGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_desc")
                    )

                    // Price input
                    OutlinedTextField(
                        value = fieldPrice,
                        onValueChange = { fieldPrice = it },
                        label = { Text("Retail Price (PKR)", color = PrimaryGold, fontSize = 11.sp) },
                        textStyle = TextStyle(color = CreamWhite, fontSize = 12.sp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGold,
                            unfocusedBorderColor = DeepGray
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_price")
                    )

                    // Image selector (Beautiful Visual Preview Selection!)
                    Text("Select Premium Background Image:", color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val imagesList = listOf(
                            "burger" to "🍔 Burger",
                            "pizza" to "🍕 Pizza",
                            "fries" to "🍟 Fries",
                            "drink" to "🥤 Mocktail"
                        )
                        imagesList.forEach { (ref, label) ->
                            val isSel = fieldImageRef == ref
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) PrimaryGold else DeepGray)
                                    .clickable { fieldImageRef = ref }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) RichBlack else LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Category Selector
                    Text("Assigned Category:", color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    // Display category selector as flow chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val row1 = categories.take(8)
                        val row2 = categories.drop(8)
                        
                        // Row 1 chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(row1) { cat ->
                                val isChosen = fieldCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isChosen) PrimaryGold else DeepGray)
                                        .clickable { fieldCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(cat, color = if (isChosen) RichBlack else LightGray, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        
                        // Row 2 chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(row2) { cat ->
                                val isChosen = fieldCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isChosen) PrimaryGold else DeepGray)
                                        .clickable { fieldCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(cat, color = if (isChosen) RichBlack else LightGray, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceNum = fieldPrice.toDoubleOrNull() ?: 0.0
                        if (fieldName.isBlank() || priceNum <= 0.0) {
                            Toast.makeText(context, "Please enter a valid Name & Price!", Toast.LENGTH_SHORT).show()
                        } else {
                            if (editingItem == null) {
                                // Add wrap
                                val newItem = MenuItem(
                                    id = "item_custom_${System.currentTimeMillis()}",
                                    name = fieldName,
                                    description = fieldDescription,
                                    price = priceNum,
                                    category = fieldCategory,
                                    imageUrl = fieldImageRef,
                                    rating = 5.0,
                                    reviewsCount = 1
                                )
                                viewModel.addMenuItem(newItem)
                                Toast.makeText(context, "${fieldName} successfully created & pushed to live menu!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Edit wrap
                                val updatedItem = editingItem!!.copy(
                                    name = fieldName,
                                    description = fieldDescription,
                                    price = priceNum,
                                    category = fieldCategory,
                                    imageUrl = fieldImageRef
                                )
                                viewModel.updateMenuItem(updatedItem)
                                Toast.makeText(context, "${fieldName} records updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("save_dish_btn")
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = LightGray, fontSize = 11.sp)
                }
            },
            containerColor = SoftObsidian,
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun DashboardMetricCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, DeepGray, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = SoftObsidian),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = CreamWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            Text(subtitle, color = LightGray, fontSize = 9.sp)
        }
    }
}

@Composable
fun PipeStatusCell(name: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(SoftObsidian, RoundedCornerShape(8.dp))
            .border(1.dp, DeepGray, RoundedCornerShape(8.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, color = LightGray, fontSize = 10.sp)
            Text(text = count.toString(), color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}


// ==================== 7. ADMIN ORDERS CENTER SCREEN ====================
@Composable
fun AdminOrdersScreen(viewModel: RestaurantViewModel) {
    val orders by viewModel.orders.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order Processing Center",
                color = PrimaryGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total System Loads: ${orders.size}",
                color = LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No orders placed yet.", color = LightGray)
            }
        } else {
            val horizontalScrollState = rememberScrollState()
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
                    .border(1.dp, DeepGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(SoftObsidian)
            ) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .background(DeepGray)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(text = "ORDER ID", width = 110.dp, isHeader = true)
                    TableCell(text = "CUSTOMER", width = 150.dp, isHeader = true)
                    TableCell(text = "ITEMS ORDERED", width = 220.dp, isHeader = true)
                    TableCell(text = "TOTAL", width = 95.dp, isHeader = true)
                    TableCell(text = "PAYMENT", width = 150.dp, isHeader = true)
                    TableCell(text = "STATUS", width = 110.dp, isHeader = true)
                    TableCell(text = "QUICK ACTIONS", width = 250.dp, isHeader = true)
                }

                Divider(color = PrimaryGold.copy(alpha = 0.5f), thickness = 1.5.dp)

                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    itemsIndexed(orders) { index, order ->
                        val rowBgColor = if (index % 2 == 0) RichBlack else SoftObsidian
                        Row(
                            modifier = Modifier
                                .background(rowBgColor)
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: ID & TYPE
                            Column(
                                modifier = Modifier
                                    .width(110.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "#${order.id.take(8).uppercase()}",
                                    color = PrimaryGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .background(WarmGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = order.type.displayName,
                                        color = PrimaryGold,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Column 2: CUSTOMER
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(text = order.customerName, color = CreamWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(text = order.customerPhone, color = LightGray, fontSize = 10.sp)
                                if (order.type == OrderType.DELIVERY && order.deliveryAddress.isNotEmpty()) {
                                    Text(
                                        text = order.deliveryAddress,
                                        color = LightGray.copy(alpha = 0.8f),
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Column 3: ITEMS ORDERED
                            Column(
                                modifier = Modifier
                                    .width(220.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                order.items.forEach { cartItem ->
                                    Text(
                                        text = "• ${cartItem.menuItem.name} x${cartItem.quantity}",
                                        color = LightGray,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Column 4: TOTAL
                            Column(
                                modifier = Modifier
                                    .width(95.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "PKR ${order.totalAmount.toInt()}",
                                    color = CreamWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                if (order.pointsRedeemed > 0) {
                                    Text(
                                        text = "-PKR ${order.discountAmount.toInt()}",
                                        color = SuccessGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Column 5: PAYMENT
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = order.paymentMethod.displayName,
                                    color = LightGray,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        viewModel.togglePaymentVerification(order.id, order.paymentVerified)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (order.paymentVerified) Icons.Filled.Verified else Icons.Outlined.Verified,
                                        contentDescription = "Verified status",
                                        tint = if (order.paymentVerified) SuccessGreen else AlertRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (order.paymentVerified) "Verified Receipt" else "Unverified Proof",
                                        color = if (order.paymentVerified) SuccessGreen else AlertRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                }
                            }

                            // Column 6: STATUS
                            Column(
                                modifier = Modifier
                                    .width(110.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                val statusColor = when (order.status) {
                                    OrderStatus.PENDING -> PendingAmber
                                    OrderStatus.ACCEPTED -> PrimaryGold
                                    OrderStatus.PREPARING -> WarmGold
                                    OrderStatus.DELIVERED -> SuccessGreen
                                    OrderStatus.REJECTED -> AlertRed
                                }
                                Box(
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, statusColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = order.status.displayName,
                                        color = statusColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Column 7: QUICK ACTIONS (Options to mark as completed immediately)
                            Row(
                                modifier = Modifier
                                    .width(250.dp)
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (order.status) {
                                    OrderStatus.PENDING -> {
                                        Button(
                                            onClick = { viewModel.acceptOrder(order.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                                            modifier = Modifier.weight(1f).height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Approve", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.rejectOrder(order.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = Color.White),
                                            modifier = Modifier.weight(0.9f).height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Reject", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    OrderStatus.ACCEPTED -> {
                                        Button(
                                            onClick = { viewModel.markPreparing(order.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                                            modifier = Modifier.weight(1f).height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Prepare", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.markDelivered(order.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen.copy(alpha = 0.8f), contentColor = Color.White),
                                            modifier = Modifier.weight(1f).height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Complete ⚡", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    OrderStatus.PREPARING -> {
                                        Button(
                                            onClick = { viewModel.markDelivered(order.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White),
                                            modifier = Modifier.fillMaxWidth().height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Complete ✔", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    OrderStatus.REJECTED -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(DeepGray, RoundedCornerShape(4.dp))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Order Rejected", color = AlertRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    OrderStatus.DELIVERED -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(DeepGray, RoundedCornerShape(4.dp))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Completed Successfully", color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        Divider(color = DeepGray.copy(alpha = 0.5f), thickness = 0.8.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    width: Dp,
    isHeader: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isHeader) PrimaryGold else CreamWhite,
            fontWeight = if (isHeader) FontWeight.Black else FontWeight.Normal,
            fontSize = if (isHeader) 10.sp else 11.sp,
            letterSpacing = if (isHeader) 1.2.sp else 0.sp
        )
    }
}


// ==================== 8. DIALOGS & SHEET HELPERS ====================
@Composable
fun ProductDetailDialog(
    item: MenuItem,
    viewModel: RestaurantViewModel,
    onDismiss: () -> Unit
) {
    var specRating by remember { mutableStateOf(5) }
    var reviewTextInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(1.dp, PrimaryGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = RichBlack)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Headline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.name, color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Dismiss", tint = PrimaryGold)
                    }
                }

                val visualImageResId = getFoodImageResId(item.category, item.imageUrl)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (visualImageResId != null) {
                        Image(
                            painter = painterResource(id = visualImageResId),
                            contentDescription = item.category,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, RichBlack.copy(alpha = 0.85f))
                                    )
                                )
                        )
                    }
                    Text(
                        text = item.category,
                        color = PrimaryGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(RichBlack.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(item.description, color = CreamWhite, fontSize = 12.sp, lineHeight = 16.sp)
                
                Divider(color = DeepGray, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Price Point", color = LightGray, fontSize = 13.sp)
                    Text("PKR ${item.price.toInt()}", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                // Add to basket actions
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        viewModel.addToCart(item, 1)
                        Toast.makeText(context, "${item.name} added to cart!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.ShoppingCart, "Buy")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add To Basket", fontWeight = FontWeight.Bold)
                }

                // Reviews Section Ratings and reviews
                Divider(color = DeepGray, modifier = Modifier.padding(vertical = 12.dp))
                Text("Merchant Customer Review Feed", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Icon(Icons.Filled.Star, "Rating", tint = PrimaryGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${String.format("%.1f", item.rating)} Stars Average (${item.reviewsCount} metrics submitted)", color = CreamWhite, fontSize = 11.sp)
                }

                // Write review form block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepGray, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text("Add Your Food Experience Review", color = PrimaryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            for (i in 1..5) {
                                IconButton(onClick = { specRating = i }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = if (i <= specRating) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = "$i Stars",
                                        tint = PrimaryGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = reviewTextInput,
                            onValueChange = { reviewTextInput = it },
                            placeholder = { Text("Crispiness, portion, taste review...", color = LightGray) },
                            textStyle = TextStyle(color = CreamWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedBorderColor = DeepGray),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        )
                        
                        Button(
                            onClick = {
                                if (reviewTextInput.isNotBlank()) {
                                    viewModel.submitProductReview(item.id, reviewTextInput, specRating)
                                    reviewTextInput = ""
                                    Toast.makeText(context, "Review saved to database!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                            modifier = Modifier.align(Alignment.End).padding(top = 6.dp).height(24.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Post Review", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceAssistantDialog(
    viewModel: RestaurantViewModel,
    onDismiss: () -> Unit
) {
    val voiceStatus by viewModel.voiceStatus.collectAsState()
    var commandInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PrimaryGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = RichBlack)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Mic, "Mic", tint = PrimaryGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Voice ordering Assistant", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Dismiss", tint = PrimaryGold)
                    }
                }

                Text(
                    text = "Future Ready Voice integration framework. Speak or enter a natural command mapping directly to Rahman Fast Food food listings catalog.",
                    color = LightGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Quick templates
                Text("Select Sample Commands Prompt:", color = PrimaryGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    VoicePromptItem("Add 2 RF Supreme Zingers and a Mint Margarita Splash") { commandInput = it }
                    VoicePromptItem("Double Decker Zinger and Lava Loaded Fries please") { commandInput = it }
                    VoicePromptItem("I want RF Gold Beef Burger with fries") { commandInput = it }
                }

                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    placeholder = { Text("Command string goes here...", color = LightGray) },
                    textStyle = TextStyle(color = CreamWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedBorderColor = DeepGray),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                if (voiceStatus.isNotEmpty()) {
                    Text(
                        text = voiceStatus,
                        color = if (voiceStatus.startsWith("Success")) SuccessGreen else PrimaryGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DeepGray, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            viewModel.executeVoiceCommand(commandInput)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold, contentColor = RichBlack),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Trigger Mock Voice Processing", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VoicePromptItem(promptText: String, onSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(promptText) }
            .background(DeepGray, RoundedCornerShape(4.dp))
            .padding(6.dp)
    ) {
        Text(promptText, color = CreamWhite, fontSize = 10.sp)
    }
}

// Custom Border tools for layout aesthetics
fun BottomBorder(width: androidx.compose.ui.unit.Dp, color: Color) = BorderStroke(width, color)
val GrayBorder = Color(0xFF424242)
