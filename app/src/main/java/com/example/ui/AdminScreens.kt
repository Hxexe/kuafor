package com.example.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.util.Locale
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateTo("ONBOARDING") }
    val allSalons by viewModel.allAdminSalons.collectAsState()
    val allAppointments by viewModel.adminAllAppointments.collectAsState()

    var activeViewTab by remember { mutableStateOf("APPROVALS") } // "APPROVALS", "COMMISSIONS", "PACKAGES", "MODERATION"

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = " Kuaförüm Platform Yöneticisi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A)
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = " Ana Sayfa",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeViewTab == "APPROVALS",
                    onClick = { activeViewTab = "APPROVALS" },
                    icon = { Icon(Icons.Default.TaskAlt, "Onaylar") },
                    label = { Text("Onaylar", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeViewTab == "COMMISSIONS",
                    onClick = { activeViewTab = "COMMISSIONS" },
                    icon = { Icon(Icons.Default.Payments, "Komisyon") },
                    label = { Text("Komisyon", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeViewTab == "PACKAGES",
                    onClick = { activeViewTab = "PACKAGES" },
                    icon = { Icon(Icons.Default.Receipt, "Paketler") },
                    label = { Text("Paketler", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeViewTab == "MODERATION",
                    onClick = { activeViewTab = "MODERATION" },
                    icon = { Icon(Icons.Default.ThumbsUpDown, "Yorumlar") },
                    label = { Text("Yorumlar", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = activeViewTab == "ADS",
                    onClick = { activeViewTab = "ADS" },
                    icon = { Icon(Icons.Default.Campaign, "Reklam") },
                    label = { Text("Reklamlar", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)) // Luxurious light slate-white base
        ) {
            when (activeViewTab) {
                "APPROVALS" -> AdminApprovalsTab(viewModel, allSalons)
                "COMMISSIONS" -> AdminCommissionsTab(viewModel, allSalons, allAppointments)
                "PACKAGES" -> AdminPackagesTab(viewModel)
                "MODERATION" -> AdminModerationTab(viewModel, allAppointments)
                "ADS" -> AdminAdsTab(viewModel, allSalons)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB A: CO-PARTNER SALON APPROVALS / SUSPENSIONS (A-001)
// -------------------------------------------------------------
@Composable
fun AdminApprovalsTab(viewModel: AppViewModel, salons: List<SalonEntity>) {
    var selectedSubTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Kayıt Başvuruları", "Aktif Salonlar", "Kısıtlı Salonlar")

    // State for Custom Commission Dialog
    var showCommissionDialogForSalon by remember { mutableStateOf<SalonEntity?>(null) }
    var newCommissionRateText by remember { mutableStateOf("") }

    val salonCommissionRates by viewModel.salonCommissionRates.collectAsState()
    val defaultCommissionPercent by viewModel.commissionRatePercent.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Salon ve Başvuru Yönetimi",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        val filteredSalons = remember(salons, selectedSubTab) {
            salons.filter { salon ->
                when (selectedSubTab) {
                    0 -> !salon.isApproved && salon.status == "PENDING"
                    1 -> salon.isApproved
                    2 -> !salon.isApproved && salon.status == "SUSPENDED"
                    else -> true
                }
            }
        }

        if (filteredSalons.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedSubTab) {
                        0 -> "Onay bekleyen kayıt başvurusu bulunmamaktadır."
                        1 -> "Aktif kayıtlı salon bulunmamaktadır."
                        2 -> "Kısıtlanmış salon bulunmamaktadır."
                        else -> ""
                    },
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredSalons) { salon ->
                    val commissionRate = salonCommissionRates[salon.id] ?: defaultCommissionPercent

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = salon.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Sahibi: ${salon.ownerName}", fontSize = 12.sp, color = Color(0xFF475569))
                                    Text(text = "Telefon: ${salon.ownerPhone}", fontSize = 12.sp, color = Color(0xFF475569))
                                    Text(text = "Konum: ${salon.address}", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = "Kategori: ${salon.category}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Komisyon Oranı: %${String.format(Locale.US, "%.1f", commissionRate)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (selectedSubTab) {
                                    0 -> { // PENDING
                                        Button(
                                            onClick = { viewModel.rejectSalonByAdmin(salon) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            modifier = Modifier.height(36.dp).padding(end = 8.dp).testTag("reject_salon_${salon.id}"),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reddet", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.approveSalonByAdmin(salon) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            modifier = Modifier.height(36.dp).testTag("approve_salon_${salon.id}"),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Onayla", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    1 -> { // APPROVED
                                        Button(
                                            onClick = {
                                                showCommissionDialogForSalon = salon
                                                newCommissionRateText = commissionRate.toString()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                            modifier = Modifier.height(36.dp).padding(end = 8.dp).testTag("edit_commission_${salon.id}"),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Komisyon Düzenle", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.restrictSalonByAdmin(salon) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            modifier = Modifier.height(36.dp).testTag("restrict_salon_${salon.id}"),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Kısıtla (Suspend)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    2 -> { // SUSPENDED
                                        Button(
                                            onClick = { viewModel.approveSalonByAdmin(salon) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            modifier = Modifier.height(36.dp).testTag("activate_salon_${salon.id}"),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Etkinleştir", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
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

    // Commission Edit Dialog
    showCommissionDialogForSalon?.let { salon ->
        AlertDialog(
            onDismissRequest = { showCommissionDialogForSalon = null },
            title = {
                Text(
                    text = "Komisyon Oranını Düzenle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${salon.name} salonu için özel komisyon oranı belirleyin:",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newCommissionRateText,
                        onValueChange = { newCommissionRateText = it },
                        label = { Text("Komisyon Oranı (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showCommissionDialogForSalon = null }) {
                        Text("İptal", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val rate = newCommissionRateText.toDoubleOrNull()
                            if (rate != null) {
                                viewModel.saveCustomCommissionRate(salon.id, rate)
                            }
                            showCommissionDialogForSalon = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Kaydet", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB B: REVENUE COMMISSIONS PLATFORM TRACKER (A-003, A-004)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCommissionsTab(
    viewModel: AppViewModel,
    salons: List<SalonEntity>,
    appointments: List<AppointmentEntity>
) {
    val commissionPercent by viewModel.commissionRatePercent.collectAsState()
    val salonCommissionRates by viewModel.salonCommissionRates.collectAsState()
    val completedBookings = appointments.filter { it.status == "COMPLETED" }
    val accumulatedGmvValue = completedBookings.sumOf { it.totalPrice }
    
    // Calculate total accrued platform commission from each salon based on its individual rate
    val commissionAccrued = completedBookings.sumOf { apt ->
        val rate = salonCommissionRates[apt.salonId] ?: commissionPercent
        apt.totalPrice * (rate / 100.0)
    }

    var expandedSalonId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Komisyon Takip & GMV Dashboard",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // General default platform totals & standards
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SettingsSuggest, null, tint = Color(0xFFC5A059))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sistem Varsayılan Komisyon Oranı", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Aşağıda özel komisyon oranı atanmayan tüm yeni üye işletmeler için varsayılan platform oranını belirleyin.",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(5.0, 8.0, 10.0, 12.0, 15.0).forEach { rate ->
                        val isSelected = commissionPercent == rate
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateCommissionRateByAdmin(rate) },
                            label = { Text("%$rate", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFC5A059),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF334155)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Fine refinement slider
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Hassas Ayar: ", fontSize = 12.sp, color = Color(0xFF64748B))
                    Slider(
                        value = commissionPercent.toFloat(),
                        onValueChange = { viewModel.updateCommissionRateByAdmin(it.toDouble()) },
                        valueRange = 1.0f..35.0f,
                        steps = 34,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFC5A059),
                            activeTrackColor = Color(0xFFC5A059),
                            inactiveTrackColor = Color(0xFFE2E8F0)
                        )
                    )
                    Text(" %${String.format(Locale.US, "%.1f", commissionPercent)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
            }
        }

        // General platform totals
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Platform Toplam GMV İşlem Hacmi", fontSize = 12.sp, color = Color(0xFF64748B))
                Text("₺${accumulatedGmvValue.toInt()}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC5A059))
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Sistem Genel Oranı", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("%${String.format(Locale.US, "%.1f", commissionPercent)} Oran", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Platform Net Kazancı", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("₺${commissionAccrued.toInt()}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981), fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Şube Bazlı Komisyon Oranı ve Ciro Raporu",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        salons.forEach { salon ->
            val salonCompleteds = completedBookings.filter { it.salonId == salon.id }
            val salonGmvValue = salonCompleteds.sumOf { it.totalPrice }
            val customRate = salonCommissionRates[salon.id]
            val activeRate = customRate ?: commissionPercent
            val salonComValue = salonGmvValue * (activeRate / 100.0)
            val isExpanded = expandedSalonId == salon.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isExpanded) Color(0xFFF8FAFC) else Color.White
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isExpanded) Color(0xFFC5A059) else Color(0xFFE2E8F0)
                ),
                elevation = CardDefaults.cardElevation(if (isExpanded) 4.dp else 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(salon.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.width(6.dp))
                                if (customRate != null) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("Özel Oran", fontSize = 9.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("Sistem Oranı", fontSize = 9.sp, color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Şube Özel Komisyon Oranı: %${String.format(Locale.US, "%.1f", activeRate)}", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("Tamamlanan Randevu: ${salonCompleteds.size} Adet", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                        
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                            Text("Ciro: ₺${salonGmvValue.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                            Text("Kesinti: ₺${salonComValue.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            IconButton(
                                onClick = { expandedSalonId = if (isExpanded) null else salon.id },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Komisyon Düzenle",
                                    tint = if (isExpanded) Color(0xFFC5A059) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Expandable per-salon custom commission slider & tags
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, null, tint = Color(0xFFC5A059), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("İşletme Komisyon Değişikliği", fontSize = 12.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Select presets
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(4.0, 5.0, 8.0, 10.0, 12.0, 15.0, 20.0).forEach { rate ->
                                val isSelected = activeRate == rate
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateSalonCommissionRate(salon.id, rate) },
                                    label = { Text("%$rate", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFC5A059),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Slider
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Hassas Ayar: ", fontSize = 11.sp, color = Color(0xFF64748B))
                            Slider(
                                value = activeRate.toFloat(),
                                onValueChange = { viewModel.updateSalonCommissionRate(salon.id, it.toDouble()) },
                                valueRange = 0.0f..40.0f,
                                steps = 80,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFC5A059),
                                    activeTrackColor = Color(0xFFC5A059),
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                )
                            )
                            Text(" %${String.format(Locale.US, "%.1f", activeRate)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB C: CUSTOMER REVIEWS & MODERATION (A-007)
// -------------------------------------------------------------
@Composable
fun AdminModerationTab(viewModel: AppViewModel, appointments: List<AppointmentEntity>) {
    val ratedBookings = appointments.filter { it.isRated }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Yorum & Geri Bildirim Moderasyonu",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (ratedBookings.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Sistemde henüz yapılmış müşteri yorumu bulunmuyor.", color = Color(0xFF64748B), textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(ratedBookings) { review ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("review_moderation_card_${review.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Müşteri: ${review.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                    Text(text = "İşletme: ${review.salonName}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                    Text(" ${review.ratingStars}/5", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Yorum: \"${review.ratingComment}\"",
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                modifier = Modifier
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Abusive action triggers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.submitRating(review.id, 0, "[Yorum yayından kaldırıldı]")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    modifier = Modifier.height(30.dp).testTag("delete_review_button_${review.id}"),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text("Yorumu Sil / Kaldır", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB D: NOTIFICATION PACKAGES SALES LEDGER & REGULATION
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPackagesTab(
    viewModel: AppViewModel
) {
    val purchases by viewModel.packetPurchases.collectAsState()
    val availablePackages by viewModel.notificationPackages.collectAsState()
    
    val totalPushSales = purchases.filter { it.type == "PUSH" }.sumOf { it.price }
    val totalWpSales = purchases.filter { it.type == "WHATSAPP" }.sumOf { it.price }
    val totalOverallSales = totalPushSales + totalWpSales

    var selectedPkgForEdit by remember { mutableStateOf<AppViewModel.NotificationPackage?>(null) }
    var showAddNewPackageDialog by remember { mutableStateOf(false) }

    // Dialog state holders
    var editName by remember { mutableStateOf("") }
    var editType by remember { mutableStateOf("PUSH") }
    var editCount by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }

    var addName by remember { mutableStateOf("") }
    var addType by remember { mutableStateOf("PUSH") }
    var addCount by remember { mutableStateOf("") }
    var addPrice by remember { mutableStateOf("") }
    var addDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bildirim Paketi Satış Raporları",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Button(
                onClick = {
                    addName = ""
                    addType = "PUSH"
                    addCount = "100"
                    addPrice = "150"
                    addDescription = ""
                    showAddNewPackageDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5A059)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Yeni Paket Ekle", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // KPI Summary cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Toplam Paket Satış Geliri", fontSize = 12.sp, color = Color(0xFF64748B))
                Text("₺${totalOverallSales.toInt()}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Push Bildirim Satışı", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("₺${totalPushSales.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 15.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("WhatsApp Bildirim Satışı", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("₺${totalWpSales.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aktif Satıştaki Paket Tarifeleri",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Show all active packages
        availablePackages.forEach { pkg ->
            val isPush = pkg.type == "PUSH"
            val typeColor = if (isPush) Color(0xFF3B82F6) else Color(0xFF10B981)
            val typeBg = if (isPush) Color(0xFFEFF6FF) else Color(0xFFECFDF5)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(typeBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPush) Icons.Default.NotificationsActive else Icons.Default.Sms,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(typeBg, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(if (isPush) "PUSH" else "WP SMS", fontSize = 8.sp, color = typeColor, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(pkg.description, fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("Bakiye: ${pkg.count} Kredi", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₺${pkg.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(4.dp))
                        IconButton(
                            onClick = {
                                selectedPkgForEdit = pkg
                                editName = pkg.name
                                editType = pkg.type
                                editCount = pkg.count.toString()
                                editPrice = pkg.price.toInt().toString()
                                editDescription = pkg.description
                            },
                            modifier = Modifier.size(28.dp).testTag("edit_pkg_${pkg.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Duzenle", tint = Color(0xFFC5A059), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Son Siparişler (İşletme Satın Alımları)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        purchases.reversed().forEach { purchase ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
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
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(purchase.packageName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                            Text(purchase.salonName, fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₺${purchase.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF10B981))
                        Text("+${purchase.count} Kredi", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }

    // UPDATE PACKAGE DIALOG
    selectedPkgForEdit?.let { pkg ->
        AlertDialog(
            onDismissRequest = { selectedPkgForEdit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFFC5A059))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paket Tarifesini Düzenle", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Paket Adı") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC5A059),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedLabelColor = Color(0xFFC5A059)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Paket Türü:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = editType == "PUSH",
                            onClick = { editType = "PUSH" },
                            label = { Text("Mobil Push") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF475569)
                            )
                        )
                        FilterChip(
                            selected = editType == "WHATSAPP",
                            onClick = { editType = "WHATSAPP" },
                            label = { Text("WhatsApp SMS") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF475569)
                            )
                        )
                    }
                    
                    OutlinedTextField(
                        value = editCount,
                        onValueChange = { editCount = it },
                        label = { Text("Kredi Adeti") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC5A059),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedLabelColor = Color(0xFFC5A059)
                        )
                    )
                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("Paket Fiyatı (TL)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC5A059),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedLabelColor = Color(0xFFC5A059)
                        )
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Paket Açıklaması") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC5A059),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedLabelColor = Color(0xFFC5A059)
                        )
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            viewModel.deleteNotificationPackage(pkg.id)
                            selectedPkgForEdit = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("Tarifeyi Sil", fontWeight = FontWeight.Bold)
                    }
                    
                    Row {
                        TextButton(onClick = { selectedPkgForEdit = null }) {
                            Text("İptal", color = Color(0xFF64748B))
                        }
                        Button(
                            onClick = {
                                val countVal = editCount.toIntOrNull() ?: 100
                                val priceVal = editPrice.toDoubleOrNull() ?: 150.0
                                viewModel.updateNotificationPackage(pkg.id, editName, editType, countVal, priceVal, editDescription)
                                selectedPkgForEdit = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5A059)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Kaydet", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )
    }

    // ADD NEW PACKAGE DIALOG
    if (showAddNewPackageDialog) {
        AlertDialog(
            onDismissRequest = { showAddNewPackageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFFC5A059))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yeni Bildirim Paketi Ekle", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = addName,
                        onValueChange = { addName = it },
                        label = { Text("Paket Adı") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC5A059),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedLabelColor = Color(0xFFC5A059)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Paket Türü:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = addType == "PUSH",
                            onClick = { addType = "PUSH" },
                            label = { Text("Mobil Push") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF475569)
                            )
                        )
                        FilterChip(
                            selected = addType == "WHATSAPP",
                            onClick = { addType = "WHATSAPP" },
                            label = { Text("WhatsApp SMS") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF475569)
                            )
                        )
                    }
                    
                    OutlinedTextField(
                        value = addCount,
                        onValueChange = { addCount = it },
                        label = { Text("Kredi Adeti") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC5A059),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedLabelColor = Color(0xFFC5A059)
                        )
                    )
                    OutlinedTextField(
                        value = addPrice,
                        onValueChange = { addPrice = it },
                        label = { Text("Paket Fiyatı (TL)") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC5A059),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedLabelColor = Color(0xFFC5A059)
                        )
                    )
                    OutlinedTextField(
                        value = addDescription,
                        onValueChange = { addDescription = it },
                        label = { Text("Paket Açıklaması") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC5A059),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedLabelColor = Color(0xFFC5A059)
                        )
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showAddNewPackageDialog = false }) {
                        Text("Vazgeç", color = Color(0xFF64748B))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val countVal = addCount.toIntOrNull() ?: 100
                            val priceVal = addPrice.toDoubleOrNull() ?: 150.0
                            viewModel.addNotificationPackage(addName, addType, countVal, priceVal, addDescription)
                            showAddNewPackageDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5A059)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ekle", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAdsTab(
    viewModel: AppViewModel,
    salons: List<SalonEntity>
) {
    val activeAds by viewModel.businessAds.collectAsState(initial = emptyList())
    val approvedSalons = remember(salons) { salons.filter { it.isApproved } }

    var adTitle by remember { mutableStateOf("") }
    var adImageUrl by remember { mutableStateOf("") }
    
    // Dropdown state
    var selectedSalonForAd by remember { mutableStateOf<SalonEntity?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Reklam Yönetim Paneli",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // New Ad Banner Form
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Yeni Reklam Banner Oluştur",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = adTitle,
                    onValueChange = { adTitle = it },
                    label = { Text("Reklam Başlığı") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                OutlinedTextField(
                    value = adImageUrl,
                    onValueChange = { adImageUrl = it },
                    label = { Text("Görsel URL'si") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Salon Selector Dropdown
                Text(
                    text = "Yönlendirilecek Salon:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedSalonForAd?.name ?: "Bir Salon Seçin (Opsiyonel)",
                                color = if (selectedSalonForAd != null) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Bağlantısız (Sadece Görsel)") },
                            onClick = {
                                selectedSalonForAd = null
                                dropdownExpanded = false
                            }
                        )
                        approvedSalons.forEach { salon ->
                            DropdownMenuItem(
                                text = { Text(salon.name) },
                                onClick = {
                                    selectedSalonForAd = salon
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (adTitle.isNotBlank() && adImageUrl.isNotBlank()) {
                            viewModel.addNewAd(adTitle, adImageUrl, selectedSalonForAd?.id)
                            adTitle = ""
                            adImageUrl = ""
                            selectedSalonForAd = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    enabled = adTitle.isNotBlank() && adImageUrl.isNotBlank()
                ) {
                    Text("Reklamı Yayınla", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Ads List
        Text(
            text = "Mevcut Aktif Reklamlar",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (activeAds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aktif reklam bulunmuyor.",
                    color = Color(0xFF64748B)
                )
            }
        } else {
            activeAds.forEach { ad ->
                val targetSalonName = remember(ad.targetSalonId, salons) {
                    salons.find { it.id == ad.targetSalonId }?.name ?: "Genel Reklam"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = ad.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Yönlendirme: $targetSalonName",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.deleteAd(ad) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Reklamı Kaldır", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

