package com.example.ui

import java.util.Locale
import java.util.Calendar
import java.text.SimpleDateFormat
import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import androidx.activity.compose.BackHandler

// --- MAIN CUSTOMER SHELL CONTAINER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMainScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState(initial = "")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationMessage) {
        if (notificationMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(notificationMessage)
        }
    }

    val showOnlyFavs by viewModel.showOnlyFavorites.collectAsState()
    val shouldInterceptMainTabs = when (currentScreen) {
        "CUSTOMER", "CUSTOMER_BOOKINGS", "CUSTOMER_PROFILE" -> true
        else -> false
    }
    BackHandler(enabled = shouldInterceptMainTabs) {
        if (currentScreen == "CUSTOMER" && showOnlyFavs) {
            viewModel.showOnlyFavorites.value = false
        } else {
            viewModel.navigateTo("ONBOARDING")
        }
    }
    val activeTab = when (currentScreen) {
        "CUSTOMER" -> if (showOnlyFavs) "FAVORITES" else "DISCOVER"
        "CUSTOMER_BOOKINGS" -> "BOOKINGS"
        "CUSTOMER_PROFILE", "CUSTOMER_WALLET", "CUSTOMER_COUPONS", "CUSTOMER_CHATS" -> "PROFILE"
        else -> "DISCOVER"
    }

    Scaffold(
        bottomBar = {
            val shouldShowBottomBar = when (currentScreen) {
                "CUSTOMER", "CUSTOMER_BOOKINGS", "CUSTOMER_PROFILE" -> true
                else -> false
            }
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NavigationBarItem(
                        selected = activeTab == "DISCOVER",
                        onClick = {
                            viewModel.showOnlyFavorites.value = false
                            viewModel.navigateTo("CUSTOMER")
                        },
                        icon = { Icon(if (activeTab == "DISCOVER") Icons.Filled.Search else Icons.Outlined.Search, "Keşfet") },
                        label = { Text("Keşfet", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF185C5C),
                            unselectedIconColor = Color(0xFF797979),
                            unselectedTextColor = Color(0xFF797979),
                            indicatorColor = Color(0xFF185C5C)
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "BOOKINGS",
                        onClick = {
                            viewModel.navigateTo("CUSTOMER_BOOKINGS")
                        },
                        icon = { Icon(if (activeTab == "BOOKINGS") Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth, "Randevularım") },
                        label = { Text("Randevularım", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF185C5C),
                            unselectedIconColor = Color(0xFF797979),
                            unselectedTextColor = Color(0xFF797979),
                            indicatorColor = Color(0xFF185C5C)
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "FAVORITES",
                        onClick = {
                            viewModel.showOnlyFavorites.value = true
                            viewModel.navigateTo("CUSTOMER")
                        },
                        icon = { Icon(if (activeTab == "FAVORITES") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Favoriler") },
                        label = { Text("Favoriler", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF185C5C),
                            unselectedIconColor = Color(0xFF797979),
                            unselectedTextColor = Color(0xFF797979),
                            indicatorColor = Color(0xFF185C5C)
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "PROFILE",
                        onClick = {
                            viewModel.navigateTo("CUSTOMER_PROFILE")
                        },
                        icon = { Icon(if (activeTab == "PROFILE") Icons.Filled.Person else Icons.Outlined.Person, "Profil") },
                        label = { Text("Profil", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF185C5C),
                            unselectedIconColor = Color(0xFF797979),
                            unselectedTextColor = Color(0xFF797979),
                            indicatorColor = Color(0xFF185C5C)
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                "CUSTOMER" -> CustomerDiscoverScreen(viewModel)
                "SALON_DETAIL" -> SalonDetailScreen(viewModel)
                "BOOKING_WIZARD" -> BookingWizardScreen(viewModel)
                "CUSTOMER_BOOKINGS" -> CustomerBookingsScreen(viewModel)
                "CUSTOMER_PROFILE" -> CustomerProfileScreen(viewModel)
                "CUSTOMER_WALLET" -> CustomerWalletScreen(viewModel)
                "CUSTOMER_COUPONS" -> CustomerCouponsScreen(viewModel)
                "CUSTOMER_CHATS" -> CustomerChatsScreen(viewModel)
                "ARTIST_DETAIL" -> ArtistDetailScreen(viewModel)
                else -> CustomerDiscoverScreen(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. DISCOVER / SALON EXPLORER SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDiscoverScreen(viewModel: AppViewModel) {
    val listSalons by viewModel.filteredSalons.collectAsState()
    val searchTxt by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategoryFilter.collectAsState()
    val showOnlyFavs by viewModel.showOnlyFavorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Location Selector Header Area (Sleek professional top banner)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Konumunuz",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.triggerNotification("Konum Servisleri: En yakın dükkanlar listeleniyor.")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Konum",
                        tint = Color(0xFF185C5C),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = " Beşiktaş, İstanbul",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF242424)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF242424),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Notification Circular Action Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), CircleShape)
                    .clickable {
                        viewModel.triggerNotification("Bildirim Kutusu: Henüz yeni bir duyuru yok.")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF242424),
                    modifier = Modifier.size(20.dp)
                )
                // Red active circular notification dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                )
            }
        }

        // Welcome text
        val name by viewModel.customerName.collectAsState()
        Text(
            text = "Merhaba, $name 👋",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "En Yakın Profesyonelleri Bul",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF242424),
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // Search text field with Filter icon row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchTxt,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("salon_search_input"),
                placeholder = { Text("Salon, hizmet veya berber ara...", fontSize = 13.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchTxt.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF185C5C),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Premium Teal filter search action block
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFF185C5C), RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.triggerNotification("Filtreleme seçenekleri aktif edildi.")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filtrele",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Visual Graphic categories rounded icon selectors
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val cats = listOf(
                Triple("ALL", "Tümü", Icons.Default.Spa),
                Triple("ERKEK", "Erkek", Icons.Default.Face),
                Triple("KADIN", "Kadın", Icons.Default.FaceRetouchingNatural),
                Triple("UNISEX", "Unisex", Icons.Default.ContentCut)
            )
            cats.forEach { (id, label, icon) ->
                val isSelected = selectedCat == id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            viewModel.selectedCategoryFilter.value = id
                            viewModel.showOnlyFavorites.value = false
                        }
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = if (isSelected) Color(0xFF185C5C) else Color.White,
                                shape = CircleShape
                            )
                            .border(
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF185C5C) else Color(0xFFE2E8F0)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else Color(0xFF185C5C),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF185C5C) else Color(0xFF797979)
                    )
                }
            }
        }

        if (showOnlyFavs) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF185C5C).copy(alpha = 0.08f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                Text(
                    text = "Sadece favorilediğiniz salonlar gösteriliyor.",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color(0xFF242424)
                )
                Spacer(modifier = Modifier.weight(1.0f))
                TextButton(onClick = { viewModel.showOnlyFavorites.value = false }) {
                    Text("Temizle", fontSize = 12.sp, color = Color(0xFF185C5C), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // List Grid
        if (listSalons.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.ContentCut,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aradığınız kriterde uygun işletme bulunamadı.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(listSalons) { salon ->
                    SalonCard(
                        salon = salon,
                        onClick = {
                            viewModel.selectedSalon.value = salon
                            viewModel.navigateTo("SALON_DETAIL")
                        },
                        onFavClick = {
                            viewModel.toggleFavoriteSalon(salon.id, salon.isFavorite)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SalonCard(
    salon: SalonEntity,
    onClick: () -> Unit,
    onFavClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("salon_item_card_${salon.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = salon.imageUrl,
                    contentDescription = salon.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
                // Proximity distance dummy badge
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Text("1.2 km uzakta", color = Color.White, fontSize = 11.sp)
                }

                // Category tag badge
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = when (salon.category) {
                            "ERKEK" -> "Erkek Berberi"
                            "KADIN" -> "Kadın Kuaförü"
                            else -> "Unisex Salon"
                        },
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Favorite action circle
                IconButton(
                    onClick = onFavClick,
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color.White, CircleShape)
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (salon.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favoriye Ekle",
                        tint = if (salon.isFavorite) Color.Red else Color.LightGray
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = salon.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", salon.rating),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Text(
                            text = " (${salon.reviewCount})",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(
                        text = salon.address,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Çalışma Saatleri: ${salon.workingHours}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. SALON DETAIL VIEW
// -------------------------------------------------------------
@Composable
fun SalonDetailScreen(viewModel: AppViewModel) {
    BackHandler { viewModel.navigateTo("CUSTOMER") }
    val salon = viewModel.selectedSalon.collectAsState().value ?: return
    val services by viewModel.activeSalonServices.collectAsState()
    val staffList by viewModel.activeSalonStaff.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Upper Poster
        Box(modifier = Modifier.height(220.dp)) {
            AsyncImage(
                model = salon.imageUrl,
                contentDescription = salon.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
            // Back button
            IconButton(
                onClick = { viewModel.navigateTo("CUSTOMER") },
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
            }
        }

        // Title and description card block
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = salon.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.toggleFavoriteSalon(salon.id, salon.isFavorite) }) {
                    Icon(
                        imageVector = if (salon.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = if (salon.isFavorite) Color.Red else Color.LightGray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.Filled.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                Text(
                    text = "${salon.rating} (${salon.reviewCount} Değerlendirme)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Text(
                text = "📍 ${salon.address}",
                fontSize = 13.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Services list section
            Text(
                text = "Hizmetlerimiz",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (services.isEmpty()) {
                Text(
                    text = "Henüz tanımlanmış bir hizmet bulunamadı.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            } else {
                services.forEach { service ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Süre: ${service.durationMinutes} dk",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                if (service.description.isNotBlank()) {
                                    Text(
                                        text = service.description,
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            Text(
                                text = "₺${service.price.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Staff list section
            Text(
                text = "Uzman Personel Kadromuz",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (staffList.isEmpty()) {
                Text(
                    text = "Hizmet veren aktif personel bulunmamaktadır.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(staffList) { staff ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(100.dp)
                                .clickable {
                                    viewModel.selectedStaffDetail.value = staff
                                    viewModel.navigateTo("ARTIST_DETAIL")
                                }
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, Color(0xFFF1F5F9)), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            // Avatar placeholder
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = staff.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = staff.role,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(10.dp))
                                Text(" ${staff.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Gallery Section
            Text(
                text = "Galeri ve Tasarımlarımız",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val listOptions = listOf(
                    "Ombre Balayage",
                    "Kalıcı Fön / Keratin",
                    "Pixie Saç Kesimi",
                    "Gelin Saçı & Makyaj"
                )
                items(listOptions) { style ->
                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .clickable { viewModel.triggerNotification("$style tasarımı görsele tıklandı.") },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Column {
                            // Design mock image icon indicator
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(85.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = style,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = style,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reviews / Testimonials Section
            Text(
                text = "Müşteri Yorumları (4.9/5)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val reviewers = listOf(
                Triple("Elif Karaca", "Beste hanım harikalar yarattı! Ombre saç boyama işleminden aşırı memnun kaldım. Kesinlikle tavsiye ederim.", "5.0"),
                Triple("Deniz Yılmaz", "Personel saç kesiminde usta. Randevu saatinde hemen aldılar ve salon tertemizdi.", "4.8")
            )
            reviewers.forEach { (user, comment, ratingVal) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(user, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                Text(" $ratingVal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(comment, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Booking action CTA Footer
            Button(
                onClick = {
                    viewModel.bookingSelectedServices.value = emptyList()
                    viewModel.bookingSelectedStaff.value = null
                    viewModel.navigateTo("BOOKING_WIZARD")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("appointment_start_button")
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185C5C))
            ) {
                Text("Hemen Randevu Al", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// -------------------------------------------------------------
// 3. BOOKING ACTION WIZARD (STEPS 1-4)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingWizardScreen(viewModel: AppViewModel) {
    val salon = viewModel.selectedSalon.collectAsState().value ?: return
    val services by viewModel.activeSalonServices.collectAsState()
    val staffList by viewModel.activeSalonStaff.collectAsState()

    val chosenServices by viewModel.bookingSelectedServices.collectAsState()
    val chosenStaff by viewModel.bookingSelectedStaff.collectAsState()
    val chosenDate by viewModel.bookingSelectedDate.collectAsState()
    val chosenTime by viewModel.bookingSelectedTime.collectAsState()
    val note by viewModel.bookingNote.collectAsState()

    // We collect all appointments dynamically to compute available slots in real-time.
    // This approach avoids showing double-booked or invalid time slots to the customer.
    val allAppointments by viewModel.adminAllAppointments.collectAsState()
    val totalDuration = chosenServices.sumOf { it.durationMinutes }

    var activeStep by remember { mutableStateOf(1) } // 1: Services, 2: Barber, 3: Date/Time, 4: Summary checkout

    BackHandler {
        if (activeStep > 1) activeStep-- else viewModel.navigateTo("SALON_DETAIL")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Step header timeline
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (activeStep > 1) activeStep-- else viewModel.navigateTo("SALON_DETAIL")
            }) {
                Icon(Icons.Default.ArrowBack, "Geri")
            }
            Text(
                text = "${salon.name}\nRezervasyon Sihirbazı",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "$activeStep / 4",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        // Horizontal status bar progress indicator
        LinearProgressIndicator(
            progress = { activeStep / 4.0f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color(0xFFF1F5F9)
        )

        // STEP RENDER SWITCHES
        Box(modifier = Modifier.weight(1f)) {
            when (activeStep) {
                1 -> {
                    // STEP 1: SELECT SERVICES (CAN CHOOSE MULTIPLE)
                    Column {
                        Text(
                            text = "Adım 1: İstediğiniz Hizmetleri Seçin",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Tek randevuda birden fazla saç tasarımı veya bakım kombinini sepete ekleyebilirsiniz.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(services) { service ->
                                val isSelected = chosenServices.any { it.id == service.id }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("service_item_${service.id}")
                                        .clickable { viewModel.toggleServiceSelection(service) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.White
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { viewModel.toggleServiceSelection(service) }
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 8.dp)
                                        ) {
                                            Text(
                                                text = service.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Süre: ${service.durationMinutes} dk",
                                                fontSize = 12.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                        Text(
                                            text = "₺${service.price.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // STEP 2: CHOOSE BARBER / STAFF
                    Column {
                        Text(
                            text = "Adım 2: Tercih Ettiğiniz Personeli Seçin",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Neutral "Herhangi / Fark Etmez" selection card
                            item {
                                val isSelected = chosenStaff == null
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("staff_any_card")
                                        .clickable { viewModel.bookingSelectedStaff.value = null },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .background(Color.LightGray.copy(alpha = 0.3f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Groups, null, tint = Color.Gray)
                                        }
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 12.dp)
                                        ) {
                                            Text(
                                                text = "Fark Etmez (Müsait İlk Personel)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Benim için fark etmez, en hızlı slota ata",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.bookingSelectedStaff.value = null }
                                        )
                                    }
                                }
                            }

                            // Render individual specialists
                            items(staffList) { staff ->
                                val isSelected = chosenStaff?.id == staff.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("staff_item_${staff.id}")
                                        .clickable { viewModel.bookingSelectedStaff.value = staff },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 12.dp)
                                        ) {
                                            Text(
                                                text = staff.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = staff.role,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                                Text(" ${staff.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.bookingSelectedStaff.value = staff }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // STEP 3: SELECT DATE & TIME
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Adım 3: Randevu Tarihi ve Saati",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Date Picker Input Simulator (Simplified but fully functional)
                        Text("Tarih Seçin", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val calendarRef = Calendar.getInstance()
                        val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendarRef.time)

                        calendarRef.add(Calendar.DAY_OF_YEAR, 1)
                        val tomStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendarRef.time)

                        calendarRef.add(Calendar.DAY_OF_YEAR, 1)
                        val dayAfterStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendarRef.time)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            listOf(todayStr, tomStr, dayAfterStr).forEach { dStr ->
                                val isSel = chosenDate == dStr
                                Card(
                                    onClick = { viewModel.bookingSelectedDate.value = dStr },
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSel) MaterialTheme.colorScheme.primary else Color.White
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (dStr == todayStr) "Bugün" else if (dStr == tomStr) "Yarın" else "Gelecek",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSel) Color.Black else Color.Gray
                                        )
                                        Text(
                                            text = dStr.substringBeforeLast(".2026"),
                                            fontSize = 11.sp,
                                            color = if (isSel) Color.Black else Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hourly Slots Select pills
                        Text("Müsait Saat Slotları", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // We replace the hardcoded time list with a dynamically calculated availability slot list.
                        // This filters out slots that conflict with existing appointments on the chosen date for the selected staff member.
                        val hourlySlots = remember(allAppointments, chosenDate, chosenStaff, chosenServices) {
                            val currentStaff = chosenStaff
                            if (currentStaff != null) {
                                val staffAppts = allAppointments.filter { it.salonId == salon.id && it.staffId == currentStaff.id && it.date == chosenDate && it.status != "REJECTED" }
                                viewModel.calculateAvailableSlots(salon.workingHours, staffAppts, totalDuration)
                            } else {
                                staffList.flatMap { staff ->
                                    val staffAppts = allAppointments.filter { it.salonId == salon.id && it.staffId == staff.id && it.date == chosenDate && it.status != "REJECTED" }
                                    viewModel.calculateAvailableSlots(salon.workingHours, staffAppts, totalDuration)
                                }.distinct().sorted()
                            }
                        }

                        // Wrap layout
                        Column {
                            val rows = hourlySlots.chunked(4)
                            rows.forEach { rowSlots ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowSlots.forEach { slotTime ->
                                        val isSel = chosenTime == slotTime
                                        Card(
                                            onClick = { viewModel.bookingSelectedTime.value = slotTime },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("time_slot_button_$slotTime"),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else Color.White
                                            ),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = slotTime,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSel) Color.Black else Color.Black
                                                )
                                            }
                                        }
                                    }
                                    // Pad remaining spaces on less slots
                                    if (rowSlots.size < 4) {
                                        Spacer(modifier = Modifier.weight((4 - rowSlots.size).toFloat()))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Not Ekle (Opsiyonel)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        OutlinedTextField(
                            value = note,
                            onValueChange = { viewModel.bookingNote.value = it },
                            placeholder = { Text("Personel için özel notunuzu buraya yazabilirsiniz...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            maxLines = 3
                        )
                    }
                }

                4 -> {
                    // STEP 4: RANDEVU ÖZETİ & CHECKOUT
                    val promoCode by viewModel.bookingCouponCode.collectAsState()
                    val promoApplied by viewModel.appliedCoupon.collectAsState()

                    val originalTotal = chosenServices.sumOf { it.price }
                    val finalTotal = promoApplied?.let { coupon ->
                        if (coupon.discountType == "PERCENT") {
                            originalTotal * (1 - (coupon.value / 100.0))
                        } else {
                            (originalTotal - coupon.value).coerceAtLeast(0.0)
                        }
                    } ?: originalTotal

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Son Adım: Randevunuzu Onaylayın",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Info Outline Container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "İşletme:",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = salon.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Tasarım & Hizmetler:",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            chosenServices.forEach { s ->
                                Text(
                                    text = "• ${s.name} (${s.durationMinutes} dk) - ₺${s.price.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Personel:",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = chosenStaff?.name ?: "Fark Etmez (Müsait İlk Personel)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Tarih & Saat:",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "$chosenDate saat $chosenTime",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Randevu Notunuz:",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = note,
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Coupon input block
                        Text("İndirim Kuponu Girişi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = promoCode,
                                onValueChange = { viewModel.bookingCouponCode.value = it },
                                placeholder = { Text("Kupon Kodu (Örn: MAKAS10)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("coupon_input_field"),
                                singleLine = true
                            )
                            Button(
                                onClick = { viewModel.applyCoupon(promoCode) },
                                modifier = Modifier.height(56.dp).testTag("coupon_apply_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Uygula", color = Color.White)
                            }
                        }

                        // Promos available advice helper
                        Text(
                            text = "Bölgesel İndirimler: Deneyebileceğiniz kuponlar: MAKAS10 (%10), SARI20 (%20), FIRSAT50 (50 TL)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        // Payment Method Selector
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Ödeme Yöntemi Seçin", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val paymentMethod by viewModel.bookingPaymentMethod.collectAsState()
                        val walletBalance by viewModel.customerWalletBalance.collectAsState()
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("CASH", "Elde Nakit", Icons.Default.Money),
                                Triple("CARD", "Kredi Kartı", Icons.Default.CreditCard),
                                Triple("WALLET", "Cüzdanım", Icons.Default.Wallet)
                            ).forEach { (method, label, icon) ->
                                val isSelected = paymentMethod == method
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.bookingPaymentMethod.value = method },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.White
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                                        )
                                        if (method == "WALLET") {
                                            Text(
                                                text = "₺${walletBalance.toInt()}",
                                                fontSize = 9.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pricing calculation box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Ara Toplam", fontSize = 14.sp, color = Color.DarkGray)
                                    Text("₺${originalTotal.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                if (promoApplied != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Kupon İndirimi (${promoApplied!!.code})", fontSize = 14.sp, color = Color.Red)
                                        Text(
                                            text = if (promoApplied!!.discountType == "PERCENT") {
                                                "-%${promoApplied!!.value.toInt()}"
                                            } else {
                                                "-₺${promoApplied!!.value.toInt()}"
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Toplam Ödenecek (Platformda)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("₺${finalTotal.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // CONTROL NAV BUTTONS FOOTER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeStep > 1) {
                OutlinedButton(
                    onClick = { activeStep-- },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Önceki Adım")
                }
            }

            // Next steps flow controller
            Button(
                onClick = {
                    when (activeStep) {
                        1 -> {
                            if (chosenServices.isNotEmpty()) {
                                activeStep = 2
                            }
                        }
                        2 -> activeStep = 3
                        3 -> {
                            if (chosenTime.isNotBlank()) {
                                activeStep = 4
                            }
                        }
                        4 -> {
                            viewModel.confirmBooking()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("booking_next_buttonStep$activeStep")
                    .height(44.dp),
                enabled = when (activeStep) {
                    1 -> chosenServices.isNotEmpty()
                    3 -> chosenTime.isNotBlank()
                    else -> true
                }
            ) {
                Text(
                    text = if (activeStep == 4) "Randevu Talebi Gönder" else "Sonraki Adım",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 4. CUSTOMER APPOINTMENTS SCREEN (TRACKING AND RATINGS)
// -------------------------------------------------------------
@Composable
fun CustomerBookingsScreen(viewModel: AppViewModel) {
    val listAppointments by viewModel.customerAppointments.collectAsState()
    var ratingDialogId by remember { mutableStateOf<Int?>(null) }

    var selectedStatusTab by remember { mutableStateOf("ACTIVE") } // "ACTIVE" or "PAST"

    val activeAppointments = listAppointments.filter { it.status == "PENDING" || it.status == "APPROVED" }
    val pastAppointments = listAppointments.filter { it.status == "COMPLETED" || it.status == "REJECTED" || it.status == "NO_SHOW" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Randevu Takibi",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        TabRow(
            selectedTabIndex = if (selectedStatusTab == "ACTIVE") 0 else 1,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedStatusTab == "ACTIVE",
                onClick = { selectedStatusTab = "ACTIVE" },
                text = { Text("Aktif Randevular (${activeAppointments.size})") }
            )
            Tab(
                selected = selectedStatusTab == "PAST",
                onClick = { selectedStatusTab = "PAST" },
                text = { Text("Geçmiş / Tamamlananlar (${pastAppointments.size})") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentShowList = if (selectedStatusTab == "ACTIVE") activeAppointments else pastAppointments

        if (currentShowList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bu listede henüz bir randevunuz bulunmamaktadır.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(currentShowList) { appt ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_appointment_item_${appt.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Status bar indicator row
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Status badge
                                val (bgClr, txtClr, statusLabel) = when (appt.status) {
                                    "PENDING" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Onay Bekliyor")
                                    "APPROVED" -> Triple(Color(0xFFD1FAE5), Color(0xFF059669), "Randevu Onaylandı")
                                    "COMPLETED" -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), "Tamamlandı")
                                    "REJECTED" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "İptal Edildi")
                                    "NO_SHOW" -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), "Gidilmedi")
                                    else -> Triple(Color.LightGray, Color.DarkGray, appt.status)
                                }

                                Box(
                                    modifier = Modifier
                                        .background(bgClr, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = statusLabel, fontSize = 11.sp, color = txtClr, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = "₺${appt.totalPrice.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = appt.salonName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Hizmetler: ${appt.serviceNames}",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )

                            Text(
                                text = "Personel: ${appt.staffName}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = " ${appt.date} - ${appt.time}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Cancel click callback or Review rating builders
                                if (appt.status == "PENDING" || appt.status == "APPROVED") {
                                    TextButton(
                                        onClick = { viewModel.cancelAppointment(appt.id) },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                    ) {
                                        Text("Randevuyu İptal Et", fontSize = 12.sp)
                                    }
                                } else if (appt.status == "COMPLETED") {
                                    if (appt.isRated) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                            Text(" Değerlendirildi (${appt.ratingStars}/5)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = { ratingDialogId = appt.id },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp).testTag("rate_button_${appt.id}")
                                        ) {
                                            Text("Puanla & Yorumla", fontSize = 11.sp, color = Color.Black)
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

    // REVIEW AND RATINGS OVERLAY DIALOG
    if (ratingDialogId != null) {
        val apptId = ratingDialogId!!
        var starRate by remember { mutableStateBy(5) }
        var commentRate by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { ratingDialogId = null },
            title = { Text("Randevuyu Puanla", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "İşletme kalitesini ve hizmet veren personelin el becerisini değerlendirin:",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stars rating selector
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (1..5).forEach { starIndex ->
                            val isLit = starIndex <= starRate
                            IconButton(onClick = { starRate = starIndex }) {
                                Icon(
                                    imageVector = if (isLit) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (isLit) Color(0xFFFFB300) else Color.LightGray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = commentRate,
                        onValueChange = { commentRate = it },
                        placeholder = { Text("Örn: Çok profesyoneller, her şey mükemmeldi...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitRating(apptId, starRate, commentRate)
                        ratingDialogId = null
                    },
                    modifier = Modifier.testTag("submit_review_button")
                ) {
                    Text("Gönder")
                }
            },
            dismissButton = {
                TextButton(onClick = { ratingDialogId = null }) {
                    Text("İptal")
                }
            }
        )
    }
}

private fun <T> mutableStateBy(value: T): MutableState<T> = mutableStateOf(value)

// -------------------------------------------------------------
// 5. CUSTOMER PROFILE & SETTINGS
// -------------------------------------------------------------
@Composable
fun CustomerProfileScreen(viewModel: AppViewModel) {
    val name by viewModel.customerName.collectAsState()
    val phone by viewModel.customerPhone.collectAsState()
    val appointments by viewModel.customerAppointments.collectAsState()

    var activeSmsPref by remember { mutableStateOf(true) }
    var activePushPref by remember { mutableStateOf(true) }
    var activeWpPref by remember { mutableStateOf(false) }

    // Calculate dynamic loyal points: 10 points for each COMPLETED appointment
    val completedCount = appointments.count { it.status == "COMPLETED" }
    val loyaltyPoints = completedCount * 15 // 15 points per visit!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Profilim & Ayarlar",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Customer Profile Identity Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.toString()?.uppercase() ?: "K",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Teleton No: $phone",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Fast Actions Row / Grid for Wallet, Coupons and Chat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Wallet Card Button
            val walletBlnc by viewModel.customerWalletBalance.collectAsState()
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo("CUSTOMER_WALLET") },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Wallet, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cüzdanım", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("₺${walletBlnc.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Coupon Card Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo("CUSTOMER_COUPONS") },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Discount, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kuponlarım", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("4 Aktif", fontSize = 11.sp, color = Color.Gray)
                }
            }

            // Chat Card Button
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo("CUSTOMER_CHATS") },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Chat, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sohbet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("Canlı", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loyal Points Status Section
        Text("Sadakat Programı", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Toplam Kuaförüm Puanınız",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "$loyaltyPoints Puan",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "100 Puanda %15 indirim kuponu kazanırsınız!",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification preferences as per M-019
        Text("Bildirim Kanal Tercihleri", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sms, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text(" SMS Onay & Hatırlatma", fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                    Switch(checked = activeSmsPref, onCheckedChange = { activeSmsPref = it })
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text(" Push (Mobil) Bildirimi", fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                    Switch(checked = activePushPref, onCheckedChange = { activePushPref = it })
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text(" WhatsApp Bildirimi", fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
                    }
                    Switch(checked = activeWpPref, onCheckedChange = { activeWpPref = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Exit / Log out
        Button(
            onClick = {
                viewModel.customerName.value = ""
                viewModel.customerPhone.value = ""
                viewModel.navigateTo("ONBOARDING")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Çıkış Yap", color = Color.White)
        }
    }
}

// -------------------------------------------------------------
// 6. CUSTOMER WALLET SCREEN (CÜZDANIM)
// -------------------------------------------------------------
@Composable
fun CustomerWalletScreen(viewModel: AppViewModel) {
    BackHandler { viewModel.navigateTo("CUSTOMER_PROFILE") }
    val walletBalance by viewModel.customerWalletBalance.collectAsState()
    val transactions by viewModel.walletTransactions.collectAsState()
    
    var bakiyeMiktari by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo("CUSTOMER_PROFILE") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
            Text(
                text = "Kuaförüm Cüzdanım",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Balance Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Kullanılabilir Toplam Bakiye",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "₺${String.format(Locale.US, "%,.2f", walletBalance)}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Anında Güvenli Ödeme & Kolay İade Avantajı",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Fast Money Top-Up option
        Text("Bakiye Yükle (Kredi/Banka Kartı)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
        
        OutlinedTextField(
            value = bakiyeMiktari,
            onValueChange = { bakiyeMiktari = it.filter { char -> char.isDigit() } },
            label = { Text("Yüklenecek Miktar (₺)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null) }
        )
        
        // Fast selection list
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(100.0, 250.0, 500.0, 1000.0).forEach { amount ->
                OutlinedButton(
                    onClick = { viewModel.addMoneyToWallet(amount); bakiyeMiktari = "" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text("+₺${amount.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Button(
            onClick = {
                val valDouble = bakiyeMiktari.toDoubleOrNull()
                if (valDouble != null && valDouble > 0.0) {
                    viewModel.addMoneyToWallet(valDouble)
                    bakiyeMiktari = ""
                } else {
                    viewModel.triggerNotification("Lütfen geçerli bir yükleme miktarı girin.")
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Bakiyeyi Güvenle Yükle", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Transaction History List
        Text("Cüzdan Hareketlerim", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
        
        if (transactions.isEmpty()) {
            Text(
                text = "Cüzdan hareketiniz bulunmamaktadır.",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            transactions.forEach { trans ->
                Card(
                     modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = if (trans.type == "TOP_UP") Color(0xFFE6F4EA) else Color(0xFFFCE8E6),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (trans.type == "TOP_UP") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (trans.type == "TOP_UP") Color(0xFF137333) else Color(0xFFC5221F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = trans.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(trans.date, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        
                        Text(
                            text = "${if (trans.type == "TOP_UP") "+" else ""}₺${trans.amount.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (trans.type == "TOP_UP") Color(0xFF137333) else Color(0xFF242424)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. CUSTOMER COUPONS SCREEN (KUPONLARIM)
// -------------------------------------------------------------
@Composable
fun CustomerCouponsScreen(viewModel: AppViewModel) {
    BackHandler { viewModel.navigateTo("CUSTOMER_PROFILE") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Custom Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo("CUSTOMER_PROFILE") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
            Text(
                text = "Aktif Kampanyalar & Kuponlar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Text(
            text = "Rezervasyon adımında kupon kodlarını uygulayarak özel indirimlerden yararlanabilirsiniz.",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.myCouponsList) { coupon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Visual Accent Coupon Display
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = coupon.discount,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Center text Info Block
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = coupon.code,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Aktif", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = coupon.desc,
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Right action "Kopyala" button block
                        OutlinedButton(
                            onClick = {
                                viewModel.bookingCouponCode.value = coupon.code
                                viewModel.triggerNotification("${coupon.code} kopyalandı! Sihirbazda kullanabilirsiniz.")
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Kopyala", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. CUSTOMER CHATS (SANATÇILARLA SOHBET)
// -------------------------------------------------------------
@Composable
fun CustomerChatsScreen(viewModel: AppViewModel) {
    val activeChatId by viewModel.activeChatStaffId.collectAsState()
    BackHandler {
        if (activeChatId != null) {
            viewModel.activeChatStaffId.value = null
        } else {
            viewModel.navigateTo("CUSTOMER_PROFILE")
        }
    }
    
    if (activeChatId != null) {
        // Show Active Chat view
        ActiveChatView(viewModel = viewModel, staffId = activeChatId!!)
    } else {
        // Show Conversation list
        ChatConversationList(viewModel = viewModel)
    }
}

@Composable
fun ChatConversationList(viewModel: AppViewModel) {
    val staffList by viewModel.activeSalonStaff.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Custom Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo("CUSTOMER_PROFILE") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
            Text(
                text = "Kuaförüm Canlı Destek",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Text(
            text = "Randevunuz veya saç tasarımlarınız hakkında stilistlerimizle direkt canlı görüşün.",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // List of conversation threads (fallback to all staff list as active channels)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            val validStaff = if (staffList.isNotEmpty()) staffList else listOf(
                StaffEntity(1, 1, "Sheila Logan", "Uzman Saç Stilisti", ""),
                StaffEntity(2, 1, "Mason Pruett", "Kıdemli Berber", "")
            )
            items(validStaff) { staff ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.activeChatStaffId.value = staff.id },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = staff.name.firstOrNull()?.toString() ?: "U",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = staff.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = staff.role,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Canlı Yazışma Kanalı",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Sohbete Git",
                            tint = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveChatView(viewModel: AppViewModel, staffId: Int) {
    val chatData by viewModel.chatMessages.collectAsState()
    val messages = chatData[staffId] ?: emptyList()
    
    var textInput by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Chat Window App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .border(BorderStroke(1.dp, Color(0xFFF1F5F9))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.activeChatStaffId.value = null }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
            }
            
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = "Beste Berber (Uzman Stilist)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF34A853), CircleShape)
                    )
                    Text(" Çevrimiçi", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
        
        // Chat dialog messages history stream pane
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val bubbleAlign = if (msg.isUser) Alignment.End else Alignment.Start
                val bubbleColor = if (msg.isUser) MaterialTheme.colorScheme.primary else Color.White
                val textColor = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = bubbleAlign
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = bubbleColor),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (msg.isUser) 16.dp else 4.dp,
                            bottomEnd = if (msg.isUser) 4.dp else 16.dp
                        ),
                        border = if (msg.isUser) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = textColor,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.timestamp,
                                fontSize = 9.sp,
                                color = if (msg.isUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
        
        // Message Compose Textbar panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Simulated voice record action
            IconButton(
                onClick = { viewModel.triggerNotification("Ses kaydetme simüle edildi! Mikrofon izni aktif.") },
                modifier = Modifier
                    .background(Color(0xFFF1F5F9), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Mic, "Ses At", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
            
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Mesaj yazın...", fontSize = 13.sp) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            // Send action button
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendChatMessage(staffId, textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Send, "Gönder", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// 9. ARTIST / SPECIALIST PROFILE DETAIL SCREEN
// -------------------------------------------------------------
@Composable
fun ArtistDetailScreen(viewModel: AppViewModel) {
    BackHandler { viewModel.navigateTo("SALON_DETAIL") }
    val artist = viewModel.selectedStaffDetail.collectAsState().value
    
    if (artist == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Personel detayı bulunamadı.")
            Button(onClick = { viewModel.navigateTo("SALON_DETAIL") }) {
                Text("Geri Dön")
            }
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Back Button navigation Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = { viewModel.navigateTo("SALON_DETAIL") }) {
                Icon(Icons.Default.ArrowBack, "Geri")
            }
            Text(
                text = "Uzman Profili",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Massive profile visual avatar row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Face, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
            }
            
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = artist.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = artist.role,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Dynamic credentials statistic panels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("11B+", "Müşteri", Icons.Default.People),
                Triple("8+ Yıl", "Deneyim", Icons.Default.Star),
                Triple("4.9", "Puan", Icons.Default.Star),
                Triple("5B+", "Yorum", Icons.Default.Star)
            ).forEach { (stat, label, icon) ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(stat, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(label, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Working constraints info
        Text("Çalışma Gün & Saatleri", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Saat Planı:", fontSize = 13.sp, color = Color.Gray)
                    Text(artist.workingHours, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("İzin Günleri:", fontSize = 13.sp, color = Color.Gray)
                    Text(artist.offDays, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Immediate call to actions CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.activeChatStaffId.value = artist.id
                    viewModel.navigateTo("CUSTOMER_CHATS")
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Chat, null, tint = Color.White)
                Text(" Sohbet Başlat", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = {
                    viewModel.bookingSelectedStaff.value = artist
                    viewModel.bookingSelectedServices.value = emptyList()
                    viewModel.navigateTo("BOOKING_WIZARD")
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("E-Randevu Al", fontWeight = FontWeight.Bold)
            }
        }
    }
}

