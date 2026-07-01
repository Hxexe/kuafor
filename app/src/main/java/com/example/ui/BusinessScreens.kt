package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.util.*
import androidx.activity.compose.BackHandler
// We import android/compose classes to enable launching an external intent to WhatsApp.
// This allows business owners to seamlessly send confirmation texts without leaving the app.
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessMainScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateTo("ONBOARDING") }
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeSalon by viewModel.activeBusinessSalon.collectAsState()
    val allSalons by viewModel.allAdminSalons.collectAsState()

    var activeTab by remember { mutableStateOf("DASHBOARD") } // "DASHBOARD", "CALENDAR", "STAFF", "SERVICES", "CAMPAIGNS"
    var showBranchSwitcher by remember { mutableStateOf(false) }
    var showCreateBranchDialog by remember { mutableStateOf(false) }

    val notificationMessage by viewModel.notificationMessage.collectAsState(initial = "")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationMessage) {
        if (notificationMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(notificationMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable { showBranchSwitcher = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeSalon?.name ?: "Şube Seçilmedi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = "Yönetici Paneli (Şube Değiştirmek İçin Dokunun)",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.navigateTo("ONBOARDING") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Ana Sayfa",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = " Ana Sayfa",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "DASHBOARD",
                    onClick = { activeTab = "DASHBOARD" },
                    icon = { Icon(Icons.Outlined.Analytics, "Özet") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = activeTab == "CALENDAR",
                    onClick = { activeTab = "CALENDAR" },
                    icon = { Icon(Icons.Outlined.List, "Takvim") },
                    label = { Text("Takvim") }
                )
                NavigationBarItem(
                    selected = activeTab == "STAFF",
                    onClick = { activeTab = "STAFF" },
                    icon = { Icon(Icons.Outlined.Badge, "Personel") },
                    label = { Text("Personel") }
                )
                NavigationBarItem(
                    selected = activeTab == "SERVICES",
                    onClick = { activeTab = "SERVICES" },
                    icon = { Icon(Icons.Outlined.ContentCut, "Hizmetler") },
                    label = { Text("Hizmetler") }
                )
                NavigationBarItem(
                    selected = activeTab == "CAMPAIGNS",
                    onClick = { activeTab = "CAMPAIGNS" },
                    icon = { Icon(Icons.Outlined.LocalActivity, "Kuponlar") },
                    label = { Text("Kampanyalar") }
                )
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
            when (activeTab) {
                "DASHBOARD" -> BusinessDashboardScreen(viewModel)
                "CALENDAR" -> BusinessCalendarScreen(viewModel)
                "STAFF" -> BusinessStaffScreen(viewModel)
                "SERVICES" -> BusinessServicesScreen(viewModel)
                "CAMPAIGNS" -> BusinessCampaignsScreen(viewModel)
            }
        }
    }

    // BRANCH SWITCHER BOTTOM SHEET SIMULATION
    if (showBranchSwitcher) {
        AlertDialog(
            onDismissRequest = { showBranchSwitcher = false },
            title = { Text("Şubeleriniz", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Yönetmek istediğiniz şubeyi seçin:", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        items(allSalons) { salon ->
                            val isCurrent = salon.id == activeSalon?.id
                            Card(
                                onClick = {
                                    viewModel.activeBusinessSalonId.value = salon.id
                                    showBranchSwitcher = false
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isCurrent) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth().testTag("select_branch_card_${salon.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Storefront, null, tint = MaterialTheme.colorScheme.primary)
                                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                        Text(salon.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(salon.category, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    if (isCurrent) {
                                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showBranchSwitcher = false
                            showCreateBranchDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().testTag("create_branch_trigger")
                    ) {
                        Icon(Icons.Default.Add, null)
                        Text("Yeni Şube / Zincir Ekle", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBranchSwitcher = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // CREATE NEW BRANCH DIALOG (B-002: Birden fazla şube açabilmeli...)
    if (showCreateBranchDialog) {
        var bName by remember { mutableStateOf("") }
        var bCat by remember { mutableStateOf("ERKEK") } // "ERKEK", "KADIN", "UNISEX"
        var bAddress by remember { mutableStateOf("") }
        var bHours by remember { mutableStateOf("09:00 - 21:00") }

        AlertDialog(
            onDismissRequest = { showCreateBranchDialog = false },
            title = { Text("Yeni Şube Açılış Bildirimi", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Kuaförüm platformuna yeni şube ekleyerek zincir işletmenizi büyütün.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bName,
                        onValueChange = { bName = it },
                        label = { Text("Şube Adı (Örn: Makas Beşiktaş)") },
                        modifier = Modifier.fillMaxWidth().testTag("branch_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Kategori Seçimi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        listOf("ERKEK", "KADIN", "UNISEX").forEach { cat ->
                            val isSel = bCat == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { bCat = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bAddress,
                        onValueChange = { bAddress = it },
                        label = { Text("Şube Açık Adresi") },
                        modifier = Modifier.fillMaxWidth().testTag("branch_address_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bHours,
                        onValueChange = { bHours = it },
                        label = { Text("Çalışma Saatleri") },
                        modifier = Modifier.fillMaxWidth().testTag("branch_hours_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bName.isNotBlank() && bAddress.isNotBlank()) {
                            viewModel.createSalonBranch(bName, bCat, bAddress, bHours)
                            showCreateBranchDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_branch_button")
                ) {
                    Text("Kaydet & Aç")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateBranchDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 1. BUSINESS ANALYTICS DASHBOARD
// -------------------------------------------------------------
// -------------------------------------------------------------
// TAB A: BUSINESS METRICS SUMMARY & CREDIT MANAGEMENT
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDashboardScreen(viewModel: AppViewModel) {
    val activeSalon by viewModel.activeBusinessSalon.collectAsState()
    val activeSalonId by viewModel.activeBusinessSalonId.collectAsState()
    val listAppointments by viewModel.businessAppointments.collectAsState()
    val staffList by viewModel.businessStaffList.collectAsState()

    // Notification credit balances
    val pushCreditsMap by viewModel.salonPushCredits.collectAsState()
    val whatsappCreditsMap by viewModel.salonWhatsappCredits.collectAsState()
    val currentPushCredits = pushCreditsMap[activeSalonId] ?: 0
    val currentWhatsappCredits = whatsappCreditsMap[activeSalonId] ?: 0
    
    var showPurchasePacketsDialog by remember { mutableStateOf(false) }
    val availablePackages by viewModel.notificationPackages.collectAsState()
    val allPurchases by viewModel.packetPurchases.collectAsState()
    val salonPurchases = allPurchases.filter { it.salonId == activeSalonId }

    // Multi-step checkout states
    var purchaseStep by remember { mutableStateOf(1) } // 1: Select Pack, 2: Billing Info, 3: Card Details, 4: Success Receipt
    var selectedPkg by remember { mutableStateOf<AppViewModel.NotificationPackage?>(null) }
    
    var billingTitle by remember { mutableStateOf(activeSalon?.name ?: "") }
    var billingEmail by remember { mutableStateOf("efaturalar@${(activeSalon?.name ?: "salon").lowercase().replace(" ", "")}.com") }
    var billingAddress by remember { mutableStateOf(activeSalon?.address ?: "Merkez Cd. No:42") }
    
    var cardNo by remember { mutableStateOf("4355 8812 9034 5112") }
    var cardHolder by remember { mutableStateOf("Ahmet Yılmaz") }
    var cardExpiry by remember { mutableStateOf("12/29") }
    var cardCvv by remember { mutableStateOf("382") }
    var processingPayment by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    // Computing dynamic analytics metrics based on current active branch appointments
    val completedBookings = listAppointments.filter { it.status == "COMPLETED" }
    val revenueTotal = completedBookings.sumOf { it.totalPrice }
    val noShowCount = listAppointments.count { it.status == "NO_SHOW" }
    val allValidCount = listAppointments.size
    
    // No show rate
    val noShowRate = if (allValidCount > 0) (noShowCount.toDouble() / allValidCount) * 100 else 0.0
    // Occupancy rate dummy based on staff size
    val occupancyRate = if (staffList.isNotEmpty()) {
        (completedBookings.size * 100 / (staffList.size * 5)).coerceIn(15, 95)
    } else 40

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Şube Özet & Bildirim Dashboard",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // KPI METRICS COUNTERS GRID
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Toplam Ciro", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₺${revenueTotal.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text("Gerçekleşen kazanç", fontSize = 9.sp, color = Color(0xFF94A3B8))
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PieChart, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kapasite Dolum", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%$occupancyRate",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text("Haftalık ortalama", fontSize = 9.sp, color = Color(0xFF94A3B8))
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NoAccounts, null, tint = if (noShowRate > 15) Color(0xFFEF4444) else Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("No-Show Oranı", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%%.1f", noShowRate),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (noShowRate > 15) Color(0xFFEF4444) else Color(0xFF10B981)
                    )
                    Text("Gelmedi oranı", fontSize = 9.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MODERN NOTIFICATION PACKETS SECTION (Müşteri Bildirim Altyapısı ve Kredi Satın Al) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Müşteri Bildirim Servisleri",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Anlık Bakiye ve Hatırlatma Kredileriniz",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                    Button(
                        onClick = { 
                            purchaseStep = 1
                            selectedPkg = null
                            showPurchasePacketsDialog = true 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5A059)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("buy_packets_trigger")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paket Satın Al", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(12.dp))
 
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Push notification credits
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFFBFDBFE)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Mobil Push", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("$currentPushCredits Adet", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        }
                    }
 
                    // WhatsApp SMS credits
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFECFDF5), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFFA7F3D0)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Sms, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("WhatsApp", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("$currentWhatsappCredits Adet", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
         // ACQUIRED PACKAGE HISTORY & STATUS INFORMATION CARD BOX
        Text(
            text = "Aktif Kampanyalı Tanımlı Paketlerim",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 8.dp)
        )
 
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (salonPurchases.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.History, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Şubenize ait aktif bakiye yükleme kaydı bulunmamaktadır.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Servisleri kullanmaya başlamak için yukarıdan ilk paketinizi satın alabilirsiniz.",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Şubeye Yüklenmiş Bakiye Siparişleri ve Kayıtlar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC5A059),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    salonPurchases.forEachIndexed { index, purchase ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (purchase.type == "PUSH") Color(0xFF3B82F6).copy(alpha = 0.1f)
                                            else Color(0xFF10B981).copy(alpha = 0.1f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (purchase.type == "PUSH") Icons.Default.NotificationsActive else Icons.Default.Sms,
                                        contentDescription = null,
                                        tint = if (purchase.type == "PUSH") Color(0xFF3B82F6) else Color(0xFF10B981),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(purchase.packageName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    Text("Fatura Seri: WP-${10342 + purchase.id}", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("+${purchase.count} Kredi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                Text("₺${purchase.price.toInt()} (Başarılı)", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                        if (index < salonPurchases.size - 1) {
                            HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
 
        // REVENUE HISTOGRAM CHART (CUSTOM CANVAS)
        Text(
            text = "Haftalık Gelir Dağılım Grafiği (TL)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 8.dp)
        )
 
        // Draw dynamic canvas chart representation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dummy weekly income breakdown
                val dailyIncomes = listOf(400.0, 800.0, 1500.0, 600.0, 950.0, 1800.0, 300.0)
                val dayLabels = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Kmt", "Paz")
 
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barWidth = (width / dailyIncomes.size) * 0.6f
                    val spacing = (width / dailyIncomes.size) * 0.4f
                    val maxVal = dailyIncomes.maxOrNull() ?: 1.0
 
                    for (i in dailyIncomes.indices) {
                        val barHeight = (dailyIncomes[i] / maxVal) * (height - 30.dp.toPx())
                        val x = i * (barWidth + spacing) + spacing / 2
                        val y = height - barHeight.toFloat() - 20.dp.toPx()
 
                        // Draw Bar
                        drawRect(
                            color = if (i == 5) Color(0xFFC5A059) else Color(0xFF2563EB), // Blue primary color accented
                            topLeft = Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight.toFloat())
                        )
                    }
                }
 
                // Labels overlay bottom
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    dayLabels.forEach { label ->
                        Text(label, fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
 
        Spacer(modifier = Modifier.height(20.dp))
 
        // MOST POPULAR HAKK-I HİZMETLER SECTION
        Text(
            text = "En Çok Tercih Edilen Hizmetler",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 8.dp)
        )
 
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                listOf(
                    Pair("Premium Saç Kesimi", "%48 Tercih Oranı"),
                    Pair("Saç & Sakal Kombin", "%32 Tercih Oranı"),
                    Pair("Komple Boya", "%12 Tercih Oranı"),
                    Pair("Cilt Maskesi", "%8 Tercih Oranı")
                ).forEachIndexed { index, (sName, stat) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${index + 1}. ",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC5A059)
                            )
                            Text(sName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF0F172A))
                        }
                        Text(stat, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }
                    if (index < 3) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))
                }
            }
        }
 
        Spacer(modifier = Modifier.height(20.dp))
 
        // NO SHOW WARNING TIP BOX
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFB000).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, Color(0xFFFFB000).copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Icon(Icons.Default.Warning, null, tint = Color(0xFFFFB300))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Gereksiz No-Show Kaybını Azaltın",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFFFFB300)
                )
                Text(
                    text = "Müşterilerinizin gecikmesini önlemek için profil ayarlarından SMS & WhatsApp hatırlatma mesajlarını aktif tutun.",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }
 
    // PURCHASE SMS / WHATSAPP PACKAGES DIALOG - 4 STEP TRANSACTION WIZARD
    if (showPurchasePacketsDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!processingPayment) showPurchasePacketsDialog = false 
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when(purchaseStep) {
                            1 -> Icons.Default.AddShoppingCart
                            2 -> Icons.Default.ReceiptLong
                            3 -> Icons.Default.CreditCard
                            else -> Icons.Default.Verified
                        }, 
                        null, 
                        tint = Color(0xFFC5A059)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when(purchaseStep) {
                            1 -> "Paket Seçimi"
                            2 -> "Fatura Detayları"
                            3 -> "Güvenli Ödeme Kart Girişi"
                            else -> "Sipariş Onaylandı!"
                        }, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    // Progress Indicator Steps Top
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..4).forEach { st ->
                            val isActive = purchaseStep >= st
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .padding(horizontal = 2.dp)
                                    .background(
                                        if (isActive) Color(0xFFC5A059) else Color(0xFFE2E8F0),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }

                    // Interactive Step Contents
                    when(purchaseStep) {
                        1 -> { // PACK SELECTION
                            Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                                Text(
                                    text = "Aktif kuaför şubeniz için bildirim bakiye paketi seçin:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                availablePackages.forEach { pkg ->
                                    val isPush = pkg.type == "PUSH"
                                    val typeBg = if (isPush) Color(0xFFEFF6FF) else Color(0xFFECFDF5)
                                    val typeBorder = if (isPush) Color(0xFF3B82F6) else Color(0xFF10B981)
                                    val typeIcon = if (isPush) Icons.Default.NotificationsActive else Icons.Default.Sms
                                    val typeIconColor = if (isPush) Color(0xFF3B82F6) else Color(0xFF10B981)

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                selectedPkg = pkg
                                                purchaseStep = 2
                                            },
                                        colors = CardDefaults.cardColors(containerColor = typeBg),
                                        border = BorderStroke(1.dp, typeBorder.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(Color.White, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(typeIcon, null, tint = typeIconColor, modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                                    Text(pkg.description, fontSize = 10.sp, color = Color(0xFF64748B))
                                                }
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("₺${pkg.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                                Text("Seç", fontSize = 10.sp, color = typeIconColor, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> { // BILLING DETAILS INPUT
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = "E-Fatura Müşteri Bilgilerini Belirleyin:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = billingTitle,
                                    onValueChange = { billingTitle = it },
                                    label = { Text("Fatura Başlığı / Ticari Ünvan") },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                OutlinedTextField(
                                    value = billingEmail,
                                    onValueChange = { billingEmail = it },
                                    label = { Text("E-Posta Adresi") },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                OutlinedTextField(
                                    value = billingAddress,
                                    onValueChange = { billingAddress = it },
                                    label = { Text("Fatura Adresi") },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                selectedPkg?.let { pkg ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Seçilen Hizmet: ${pkg.name}", fontSize = 11.sp, color = Color.LightGray)
                                            Text("Tutar: ₺${pkg.price.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        3 -> { // CARD PAYMENT INFO DETAILS SCREEN
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                selectedPkg?.let { pkg ->
                                    // Stunning custom-styled credit card canvas simulation UI
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D2939)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CreditCard, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                                Text("SAFE PAY SECURE", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = if (cardNo.isBlank()) "•••• •••• •••• ••••" else cardNo,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("KART SAHİBİ", fontSize = 8.sp, color = Color.Gray)
                                                    Text(
                                                        text = if (cardHolder.isBlank()) "AD SOYAD" else cardHolder.uppercase(),
                                                        fontSize = 11.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("KT / CVV", fontSize = 8.sp, color = Color.Gray)
                                                    Text(
                                                        text = "${if(cardExpiry.isBlank()) "AA/YY" else cardExpiry}  ${if(cardCvv.isBlank()) "•••" else cardCvv}",
                                                        fontSize = 11.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (processingPayment) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("Sanal POS Ödemesi Onaylanıyor...", fontSize = 12.sp, color = Color.White)
                                            Text("3D Secure doğrulaması yapılıyor", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Güvenli 256-bit SSL ödeme tüneli kart bilgileri:",
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )

                                    OutlinedTextField(
                                        value = cardNo,
                                        onValueChange = { if (it.length <= 19) cardNo = it },
                                        label = { Text("Kart Numarası (16 Hane)") },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    OutlinedTextField(
                                        value = cardHolder,
                                        onValueChange = { cardHolder = it },
                                        label = { Text("Kart Üzerindeki İsim") },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = cardExpiry,
                                            onValueChange = { if (it.length <= 5) cardExpiry = it },
                                            label = { Text("Son Kul. (AA/YY)") },
                                            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        OutlinedTextField(
                                            value = cardCvv,
                                            onValueChange = { if (it.length <= 3) cardCvv = it },
                                            label = { Text("CVV") },
                                            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        else -> { // SUCCESS RECEIPT SUMMARY 
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color(0xFF0F172A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Verified, null, tint = Color(0xFF34D399), modifier = Modifier.size(48.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Ödeme Başarıyla Alındı!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                Text("Haklar şubenize yüklendi ve aktifleşti.", fontSize = 11.sp, color = Color.LightGray)
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                selectedPkg?.let { pkg ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Sipariş No:", fontSize = 11.sp, color = Color.Gray)
                                                Text("TX-${100000 + (Math.random() * 89999).toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Satın Alınan:", fontSize = 11.sp, color = Color.Gray)
                                                Text(pkg.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Kazanılan Bakiye:", fontSize = 11.sp, color = Color.Gray)
                                                Text("+${pkg.count} Kredi", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF34D399))
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Tahsil Edilen Fiyat:", fontSize = 11.sp, color = Color.Gray)
                                                Text("₺${pkg.price.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (purchaseStep in 2..3 && !processingPayment) {
                        TextButton(
                            onClick = { purchaseStep -= 1 }
                        ) {
                            Text("Geri", color = Color.White)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Row {
                        if (purchaseStep < 4) {
                            TextButton(
                                onClick = { showPurchasePacketsDialog = false },
                                enabled = !processingPayment
                            ) {
                                Text("Kapat", color = Color.LightGray)
                            }
                        }
                        
                        if (purchaseStep == 2) {
                            Button(
                                onClick = { purchaseStep = 3 },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("İleri (Ödeme Yap)", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } 
                        else if (purchaseStep == 3) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        processingPayment = true
                                        kotlinx.coroutines.delay(2000) // Simulated checkout tunnel authentication latency
                                        selectedPkg?.let { pkg ->
                                            viewModel.purchaseNotificationPackage(activeSalonId, pkg.id)
                                        }
                                        processingPayment = false
                                        purchaseStep = 4
                                    }
                                },
                                enabled = !processingPayment && cardNo.length >= 12,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34D399))
                            ) {
                                Text("Kart ile Güvenli Öde", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else if (purchaseStep == 4) {
                            Button(
                                onClick = { showPurchasePacketsDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Geri Dön (Bakiye Güncellendi)", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 2. DAILY CALENDAR TRACKER (APPROVE / REJECT FLOWS)
// -------------------------------------------------------------
@Composable
fun BusinessCalendarScreen(viewModel: AppViewModel) {
    // We obtain the local context from Jetpack Compose to be able to start the WhatsApp sharing Intent.
    // This is crucial for launching activities from within a Composable function.
    val context = LocalContext.current
    val selectedDate by viewModel.businessCalendarDate.collectAsState()
    val appointments by viewModel.filteredBusinessAppointments.collectAsState()
    var showManualBookingDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Date controller header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.changeBusinessCalendarDate(-1) }) {
                Icon(Icons.Default.ChevronLeft, "Önceki Gün")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = " Tarih: $selectedDate",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = { viewModel.changeBusinessCalendarDate(1) }) {
                Icon(Icons.Default.ChevronRight, "Sonraki Gün")
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        // Manuel phone reservation CTA trigger

        Button(
            onClick = {
                viewModel.manualSelectedDate.value = selectedDate
                showManualBookingDialog = true
            },
            modifier = Modifier.fillMaxWidth().testTag("add_manual_appt_trigger"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.PhoneCallback, null, tint = Color.White)
            Text(" Telefonla Randevu Ekle (Manuel)", color = Color.White, fontWeight = FontWeight.Bold)
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gelen Randevu Listesi",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.EventBusy, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bu tarihte herhangi bir randevu talebi bulunmamaktadır.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(appointments) { appt ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_appointment_card_${appt.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = " Saat: ${appt.time}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                // Status text format
                                val (statusText, statusColor) = when (appt.status) {
                                    "PENDING" -> "Onay Bekliyor" to Color(0xFFD97706)
                                    "APPROVED" -> "Onaylandı" to Color(0xFF059669)
                                    "COMPLETED" -> "Tamamlandı" to Color(0xFF0284C7)
                                    "REJECTED" -> "Reddedildi" to Color(0xFFDC2626)
                                    else -> appt.status to Color.Gray
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Müşteri: ${appt.customerName} (${appt.customerPhone})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Seçilen Hizmet: ${appt.serviceNames}", fontSize = 13.sp, color = Color.DarkGray)
                            Text(text = "Tercih Personel: ${appt.staffName}", fontSize = 12.sp, color = Color.Gray)

                            if (appt.note.isNotBlank()) {
                                Text(
                                    text = "Not: \"${appt.note}\"",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive workflow triggers (B-004, B-006)
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                when (appt.status) {
                                    "PENDING" -> {
                                        OutlinedButton(
                                            onClick = { viewModel.updateAppointmentStatus(appt.id, "REJECTED") },
                                            modifier = Modifier.padding(end = 8.dp).height(32.dp).testTag("reject_button_${appt.id}")
                                        ) {
                                            Text("Reddet", color = Color.Red, fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.updateAppointmentStatus(appt.id, "APPROVED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.height(32.dp).testTag("approve_button_${appt.id}")
                                        ) {
                                            Text("Onayla", color = Color.Black, fontSize = 11.sp)
                                        }
                                    }
                                    "APPROVED" -> {
                                        OutlinedButton(
                                            onClick = { viewModel.updateAppointmentStatus(appt.id, "NO_SHOW") },
                                            modifier = Modifier.padding(end = 8.dp).height(32.dp).testTag("noshow_button_${appt.id}")
                                        ) {
                                            Text("Gidilmedi", color = Color.Gray, fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.updateAppointmentStatus(appt.id, "COMPLETED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.height(32.dp).testTag("complete_button_${appt.id}")
                                        ) {
                                            Text("Tamamlandı", color = Color.Black, fontSize = 11.sp)
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

    // MANUEL BOOKING OVERLAY CREATOR POPUP
    if (showManualBookingDialog) {
        val activeServices by viewModel.businessServices.collectAsState()
        val staffList by viewModel.businessStaffList.collectAsState()

        val inputName by viewModel.manualCustomerName.collectAsState()
        val inputPhone by viewModel.manualCustomerPhone.collectAsState()
        val inputDate by viewModel.manualSelectedDate.collectAsState()
        val chosenStaff by viewModel.manualSelectedStaff.collectAsState()
        val chosenTime by viewModel.manualSelectedTime.collectAsState()
        val chosenServices by viewModel.manualSelectedServices.collectAsState()

        AlertDialog(
            onDismissRequest = { showManualBookingDialog = false },
            title = { Text("Telefonla Rezervasyon Al", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Müşterinin bilgilerini girerek randevuyu takvime işleyin.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { viewModel.manualCustomerName.value = it },
                        label = { Text("Müşteri Adı") },
                        modifier = Modifier.fillMaxWidth().testTag("manual_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = { viewModel.manualCustomerPhone.value = it },
                        label = { Text("Telefon Numarası") },
                        modifier = Modifier.fillMaxWidth().testTag("manual_phone_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputDate,
                        onValueChange = { viewModel.manualSelectedDate.value = it },
                        label = { Text("Randevu Tarihi (Örn: 04.06.2026)") },
                        modifier = Modifier.fillMaxWidth().testTag("manual_date_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))


                    Text("Hizmet Seçimi (Çoklu)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    activeServices.forEach { s ->
                        val isSelected = chosenServices.any { it.id == s.id }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newList = chosenServices.toMutableList()
                                    if (isSelected) newList.removeAll { it.id == s.id } else newList.add(s)
                                    viewModel.manualSelectedServices.value = newList
                                }
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    val newList = chosenServices.toMutableList()
                                    if (isSelected) newList.removeAll { it.id == s.id } else newList.add(s)
                                    viewModel.manualSelectedServices.value = newList
                                }
                            )
                            Text("${s.name} - ₺${s.price.toInt()}", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Personel Seçimi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    staffList.forEach { staff ->
                        val isSelected = chosenStaff?.id == staff.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.manualSelectedStaff.value = staff }
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.manualSelectedStaff.value = staff }
                            )
                            Text(staff.name, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = chosenTime,
                        onValueChange = { viewModel.manualSelectedTime.value = it },
                        label = { Text("Randevu Saati (Örn: 14:30)") },
                        modifier = Modifier.fillMaxWidth().testTag("manual_time_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank() && inputPhone.isNotBlank() && chosenServices.isNotEmpty() && chosenTime.isNotBlank()) {
                            // We save client info before addManualAppointment clears them from the state.
                            // This ensures the WhatsApp invite is constructed using correct and complete user details.
                            val clientName = inputName
                            val clientPhone = inputPhone
                            val clientTime = chosenTime
                            val clientDate = viewModel.manualSelectedDate.value

                            viewModel.addManualAppointment()
                            showManualBookingDialog = false
                            sendWhatsAppInvite(context, clientPhone, clientName, clientDate, clientTime)
                        }
                    },
                    modifier = Modifier.testTag("submit_manual_booking_button")
                ) {
                    Text("Takvime Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualBookingDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 3. STAFF PROFILE DIRECTORY MANAGER
// -------------------------------------------------------------
@Composable
fun BusinessStaffScreen(viewModel: AppViewModel) {
    val staffList by viewModel.businessStaffList.collectAsState()
    var showAddStaffDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Personel Yönetimi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Button(
                onClick = { showAddStaffDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_staff_trigger")
            ) {
                Icon(Icons.Default.Add, null)
                Text("Ekle")
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        if (staffList.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Henüz çalışan personel kaydı yapmadınız.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(staffList) { staff ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(staff.role, fontSize = 12.sp, color = Color.Gray)
                                Text("Çalışma Saati: ${staff.workingHours} | Tatil: ${staff.offDays}", fontSize = 10.sp, color = Color.LightGray)
                            }

                            IconButton(onClick = { viewModel.deleteBusinessStaff(staff.id) }) {
                                Icon(Icons.Default.Delete, "Sil", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD NEW STAFF OVERLAY FORM
    if (showAddStaffDialog) {
        var sName by remember { mutableStateOf("") }
        var sRole by remember { mutableStateOf("Usta Berber") }
        var sHours by remember { mutableStateOf("09:00 - 19:30") }
        var sOff by remember { mutableStateOf("Pazar") }

        AlertDialog(
            onDismissRequest = { showAddStaffDialog = false },
            title = { Text("Personel İşe Alım Bildirimi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = sName,
                        onValueChange = { sName = it },
                        label = { Text("Personel Adı Soyadı") },
                        modifier = Modifier.fillMaxWidth().testTag("staff_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sRole,
                        onValueChange = { sRole = it },
                        label = { Text("Uzmanlık / Rol") },
                        modifier = Modifier.fillMaxWidth().testTag("staff_role_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sHours,
                        onValueChange = { sHours = it },
                        label = { Text("Çalışma Saatleri (Örn: 09:00-19:00)") },
                        modifier = Modifier.fillMaxWidth().testTag("staff_hours_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sOff,
                        onValueChange = { sOff = it },
                        label = { Text("Tatil Günleri (Örn: Pazar)") },
                        modifier = Modifier.fillMaxWidth().testTag("staff_off_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (sName.isNotBlank()) {
                            viewModel.addBusinessStaff(sName, sRole, sHours, sOff)
                            showAddStaffDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_staff_button")
                ) {
                    Text("Rostere Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 4. SERVICES CATALOG EDITOR
// -------------------------------------------------------------
@Composable
fun BusinessServicesScreen(viewModel: AppViewModel) {
    val services by viewModel.businessServices.collectAsState()
    var showAddServiceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hizmet Kataloğu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Button(
                onClick = { showAddServiceDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_service_trigger")
            ) {
                Icon(Icons.Default.Add, null)
                Text("Yeni")
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        if (services.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Henüz bir şube hizmeti tanımlamadınız.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(services) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(service.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Süre: ${service.durationMinutes} dk | Kategori: ${service.category}", fontSize = 12.sp, color = Color.Gray)
                                if (service.description.isNotBlank()) {
                                    Text(service.description, fontSize = 11.sp, color = Color.LightGray)
                                }
                            }
                            Text("₺${service.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(onClick = { viewModel.deleteBusinessService(service.id) }) {
                                Icon(Icons.Default.Delete, "Sil", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD NEW SERVICE OVERLAY FORM
    if (showAddServiceDialog) {
        var name by remember { mutableStateOf("") }
        var priceStr by remember { mutableStateOf("") }
        var durStr by remember { mutableStateOf("30") }
        var cat by remember { mutableStateOf("SACH_KESIMI") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddServiceDialog = false },
            title = { Text("Yeni Hizmet Tanımlama Formu", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Hizmet Modeli Adı (Örn: Sakal Tıraşı)") },
                        modifier = Modifier.fillMaxWidth().testTag("service_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Ücret (TL)") },
                            modifier = Modifier.weight(1f).testTag("service_price_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = durStr,
                            onValueChange = { durStr = it },
                            label = { Text("Süre (Dk)") },
                            modifier = Modifier.weight(1f).testTag("service_duration_input"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Açıklama / Paket İçeriği") },
                        modifier = Modifier.fillMaxWidth().testTag("service_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = priceStr.toDoubleOrNull() ?: 100.0
                        val dur = durStr.toIntOrNull() ?: 30
                        if (name.isNotBlank()) {
                            viewModel.addBusinessService(name, price, dur, cat, desc)
                            showAddServiceDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_service_button")
                ) {
                    Text("Kaydet & Yayınla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddServiceDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 5. COUPONS / PROMOTIONS MANAGER (B-014)
// -------------------------------------------------------------
@Composable
fun BusinessCampaignsScreen(viewModel: AppViewModel) {
    val coupons by viewModel.businessCoupons.collectAsState()
    var showAddCouponDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kampanyalar & Kuponlar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Button(
                onClick = { showAddCouponDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_coupon_trigger")
            ) {
                Icon(Icons.Default.Add, null)
                Text("Oluştur")
            }
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(12.dp))

        if (coupons.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Tanımlanmış aktif kampanya kuponu bulunmamaktadır.", color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(coupons) { coupon ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalActivity, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(coupon.code, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = if (coupon.discountType == "PERCENT") {
                                        "İndirim Oranı: %${coupon.value.toInt()}"
                                    } else {
                                        "İndirim Tutarı: ₺${coupon.value.toInt()}"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Min. Harcama Limiti: ₺${coupon.minAmount.toInt()}", fontSize = 11.sp, color = Color.LightGray)
                            }
                            IconButton(onClick = { viewModel.deleteCoupon(coupon.id) }) {
                                Icon(Icons.Default.Delete, "Sil", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE KAMPANYA DIALOG
    if (showAddCouponDialog) {
        var code by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("PERCENT") } // "PERCENT" or "FIXED"
        var valStr by remember { mutableStateOf("") }
        var minStr by remember { mutableStateOf("150") }
        var limitStr by remember { mutableStateOf("100") }

        AlertDialog(
            onDismissRequest = { showAddCouponDialog = false },
            title = { Text("Promosyon Kuponu Tanımla", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Kupon Kodu (Örn: SARI25)") },
                        modifier = Modifier.fillMaxWidth().testTag("coupon_code_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("İndirim Modeli", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = type == "PERCENT",
                            onClick = { type = "PERCENT" },
                            label = { Text("Yüzdelik Oran (%)") }
                        )
                        FilterChip(
                            selected = type == "FIXED",
                            onClick = { type = "FIXED" },
                            label = { Text("Net Tutar (TL)") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = valStr,
                        onValueChange = { valStr = it },
                        label = { Text(if (type == "PERCENT") "İndirim Oranı (Örn: 20)" else "İndirim Tutarı (Örn: 50)") },
                        modifier = Modifier.fillMaxWidth().testTag("coupon_val_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minStr,
                            onValueChange = { minStr = it },
                            label = { Text("Min. Tutar (TL)") },
                            modifier = Modifier.weight(1f).testTag("coupon_min_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = limitStr,
                            onValueChange = { limitStr = it },
                            label = { Text("Kullanım Sınırı") },
                            modifier = Modifier.weight(1f).testTag("coupon_limit_input"),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val numVal = valStr.toDoubleOrNull() ?: 10.0
                        val min = minStr.toDoubleOrNull() ?: 0.0
                        val limit = limitStr.toIntOrNull() ?: 100
                        if (code.isNotBlank()) {
                            viewModel.addCoupon(code, type, numVal, min, limit)
                            showAddCouponDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_coupon_button")
                ) {
                    Text("Kuponu Tanımla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCouponDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

// We define a helper function to send an invite message containing booking details and download link via WhatsApp.
// This implements a low-friction viral growth loop by encouraging customers to download the app.
fun sendWhatsAppInvite(context: Context, phone: String, name: String, date: String, time: String) {
    val message = "Merhaba $name, randevunuz $date saat $time için başarıyla oluşturuldu. Bir sonraki randevunuzu telefonla aramadan 3 tıkla kolayca almak için uygulamamızı indirin: https://kuaforumapp.com/download"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}")
    }
    context.startActivity(intent)
}
