package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val appViewModel: AppViewModel = viewModel()
                val currentScreen by appViewModel.currentScreen.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "MainScreenNavigation"
                    ) { screen ->
                        when {
                            screen == "ONBOARDING" -> OnboardingPortalScreen(appViewModel)
                            screen == "CUSTOMER_AUTH" -> CustomerLoginScreen(appViewModel)
                            screen.startsWith("CUSTOMER") -> CustomerMainScreen(appViewModel)
                            screen == "SALON_DETAIL" -> CustomerMainScreen(appViewModel)
                            screen == "BOOKING_WIZARD" -> CustomerMainScreen(appViewModel)
                            screen == "ARTIST_DETAIL" -> CustomerMainScreen(appViewModel)
                            screen.startsWith("BUSINESS") -> BusinessMainScreen(appViewModel)
                            screen == "ADMIN" -> AdminMainScreen(appViewModel)
                            screen == "STAFF_PANEL" -> StaffMainScreen(appViewModel)
                            else -> OnboardingPortalScreen(appViewModel)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CENTRAL ROLE SELECTION & ONBOARDING ENTRY-PORTAL WITH CAROUSEL SLIDES
// -------------------------------------------------------------
@Composable
fun OnboardingPortalScreen(viewModel: AppViewModel) {
    var showRoleSelection by remember { mutableStateOf(false) }
    var currentSlide by remember { mutableStateOf(0) }
    var showPhoneLoginDialog by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var adminPasswordInput by remember { mutableStateOf("") }
    var adminLoginError by remember { mutableStateOf<String?>(null) }

    val slides = listOf(
        OnboardingSlideData(
            title = "Kolay Kuaför & Berber\nRandevu Sistemi",
            description = "Bölgenizdeki en iyi salonları, popüler hizmetleri ve uzman uzmanları gerçek zamanlı olarak keşfedin.",
            icon = Icons.Default.Spa,
            accentColor = Color(0xFF185C5C)
        ),
        OnboardingSlideData(
            title = "Harita Üzerinden\nSalonları Keşfedin",
            description = "Akıllı yarıçap arama ve canlı filtreleme ile en iyi saç tasarımcılarını ve berberleri bulun.",
            icon = Icons.Default.DirectionsRun,
            accentColor = Color(0xFFFFA12E)
        ),
        OnboardingSlideData(
            title = "Favori Salon & Tasarımlarınızı\nKaydedin",
            description = "Sevdiğiniz uzman saç tasarımcılarını ve tasarımlarını kaydederek 7/24 anında randevu alın.",
            icon = Icons.Default.Favorite,
            accentColor = Color(0xFFE11D48)
        ),
        OnboardingSlideData(
            title = "Tek Panelden Randevularınızı\nYönetin",
            description = "Gelecek randevularınızı takip edin, dijital fişlerinizi görüntüleyin veya kolayca iptal edin.",
            icon = Icons.Default.CalendarMonth,
            accentColor = Color(0xFF185C5C)
        ),
        OnboardingSlideData(
            title = "Profesyonel Tasarımcılarla\nDoğrudan Sohbet Edin",
            description = "Gerçek zamanlı olarak merak ettiğiniz soruları sorun, saç ve tarz idham fotoğraflarını paylaşın.",
            icon = Icons.Default.Comment,
            accentColor = Color(0xFF10B981)
        )
    )

    if (!showRoleSelection) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F6))
                .statusBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Header: Logo and Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ContentCut,
                        contentDescription = null,
                        tint = Color(0xFF185C5C),
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF185C5C).copy(alpha = 0.1f), CircleShape)
                            .padding(4.dp)
                    )
                    Text(
                        text = "Kuaförüm",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF242424),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "Atla",
                    color = Color(0xFF797979),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { showRoleSelection = true }
                        .padding(8.dp)
                )
            }

            // Slide visual layout preview mock (Dynamic high fidelity UI demonstration)
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                    },
                    label = "SlideVisual"
                ) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        when (index) {
                            0 -> MockSalonCard()
                            1 -> MockMapSearch()
                            2 -> MockFavoritesList()
                            3 -> MockBookingsManager()
                            4 -> MockArtistChatBubble()
                            else -> MockSalonCard()
                        }
                    }
                }
            }

            // Title & Description Text Block (The exact visual text & font styles)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = slides[currentSlide].title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF242424),
                    lineHeight = 34.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = slides[currentSlide].description,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF797979),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    for (i in 0 until 5) {
                        val isSelected = i == currentSlide
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(if (isSelected) 18.dp else 6.dp)
                                .background(
                                    color = if (isSelected) Color(0xFF185C5C) else Color(0xFF797979).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }

                // Primary full-bleed rounded button
                Button(
                    onClick = {
                        if (currentSlide < 4) {
                            currentSlide++
                        } else {
                            showRoleSelection = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185C5C)),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Text(
                        text = if (currentSlide == 4) "Hemen Başlayın" else "İleri",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showRoleSelection = true }
                ) {
                    Text("Zaten bir hesabınız var mı? ", fontSize = 13.sp, color = Color(0xFF797979))
                    Text("Giriş Yap", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF185C5C))
                }
            }
        }
    } else {
        // High fidelity elegant Select Access Mode Dashboard role chooser
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F6F6))
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo & Branding
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF185C5C).copy(alpha = 0.08f), CircleShape)
                    .border(BorderStroke(1.5.dp, Color(0xFF185C5C)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCut,
                    contentDescription = null,
                    tint = Color(0xFF185C5C),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Salon Randevu Sistemine Hoş Geldiniz",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF242424),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Giriş yapmak istediğiniz paneli seçin. Müşteri, salon yöneticisi veya sistem yöneticisi ekranlarını test edebilirsiniz.",
                fontSize = 13.sp,
                color = Color(0xFF797979),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 28.dp)
            )

            // Role Card 1: CUSTOMER
            ModernOnboardingRoleCard(
                title = "Müşteri Girişi",
                description = "Salonları keşfedin, en iyi saç tasarımcılarından anında randevu alın ve ödemenizi cüzdanınızla yapın.",
                icon = Icons.Filled.Person,
                tintColor = Color(0xFF185C5C),
                tag = "role_btn_customer",
                onClick = { viewModel.navigateTo("CUSTOMER_AUTH") }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Role Card 2: BUSINESS/OWNER
            ModernOnboardingRoleCard(
                title = "Salon Sahibi & Çalışanı",
                description = "Randevuları, çalışma saatlerini, gelirleri yönetin veya çalışan olarak takviminizi ve performansınızı görün.",
                icon = Icons.Filled.Storefront,
                tintColor = Color(0xFFFFA12E),
                tag = "role_btn_business",
                onClick = { showPhoneLoginDialog = true }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Role Card 3: CENTRAL PLATFORM ADMIN
            ModernOnboardingRoleCard(
                title = "Sistem Yöneticisi",
                description = "Platform genelindeki salonları denetleyin, komisyon oranlarını ayarlayın ve reklam afişlerini yönetin.",
                icon = Icons.Filled.Security,
                tintColor = Color(0xFF1E293B),
                tag = "role_btn_admin",
                onClick = { showAdminLoginDialog = true }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Kuaförüm • Premium Tasarım Sürümü",
                fontSize = 11.sp,
                color = Color(0xFF797979),
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showPhoneLoginDialog) {
        var phoneInput by remember { mutableStateOf("") }
        var staffPasswordInput by remember { mutableStateOf("") }
        var loginError by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { 
                showPhoneLoginDialog = false
                loginError = null
                staffPasswordInput = ""
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFA12E).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color(0xFFFFA12E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Salon Sahibi & Çalışanı Girişi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF242424)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lütfen sisteme kayıtlı telefon numaranızı giriniz (Örn: 05551112233 veya 05559998877):",
                        fontSize = 13.sp,
                        color = Color(0xFF797979)
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = {
                            phoneInput = it
                            loginError = null
                        },
                        placeholder = { Text("0555 111 22 33") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("staff_login_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFA12E),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = staffPasswordInput,
                        onValueChange = {
                            staffPasswordInput = it
                            loginError = null
                        },
                        label = { Text("Şifre") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFA12E),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true
                    )

                    if (loginError != null) {
                        Text(
                            text = loginError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    TextButton(
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "Şifre sıfırlama linki kayıtlı numaranıza iletildi",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Şifremi Unuttum",
                            color = Color(0xFFFFA12E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (phoneInput.trim().isBlank()) {
                            loginError = "Telefon numarası boş bırakılamaz!"
                        } else if (staffPasswordInput.isBlank()) {
                            loginError = "Şifre boş bırakılamaz!"
                        } else {
                            viewModel.loginBusinessOrStaff(phoneInput, staffPasswordInput) { errorMsg ->
                                loginError = errorMsg
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA12E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Giriş Yap", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPhoneLoginDialog = false
                        loginError = null
                        staffPasswordInput = ""
                    }
                ) {
                    Text("İptal", color = Color(0xFF797979), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (showAdminLoginDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAdminLoginDialog = false
                adminPasswordInput = ""
                adminLoginError = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF1E293B).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Yönetici Girişi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF242424)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lütfen sistem yöneticisi şifresini giriniz:",
                        fontSize = 13.sp,
                        color = Color(0xFF797979)
                    )

                    OutlinedTextField(
                        value = adminPasswordInput,
                        onValueChange = {
                            adminPasswordInput = it
                            adminLoginError = null
                        },
                        placeholder = { Text("Şifre") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E293B),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true
                    )

                    if (adminLoginError != null) {
                        Text(
                            text = adminLoginError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminPasswordInput == "admin123") {
                            showAdminLoginDialog = false
                            adminPasswordInput = ""
                            viewModel.navigateTo("ADMIN")
                        } else {
                            adminLoginError = "Hata: Geçersiz yönetici şifresi!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Giriş Yap", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAdminLoginDialog = false
                        adminPasswordInput = ""
                        adminLoginError = null
                    }
                ) {
                    Text("İptal", color = Color(0xFF797979), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

// -------------------------------------------------------------
// HIGH-FIDELITY ONBOARDING MOCK GRAPHICS DRAWINGS
// -------------------------------------------------------------
data class OnboardingSlideData(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color
)

@Composable
fun MockSalonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFF185C5C).copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ContentCut, null, tint = Color(0xFF185C5C), modifier = Modifier.size(44.dp))
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Işıltı & Zarafet Saç Tasarım", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF242424))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFA12E).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("%10 İNDİRİM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA12E))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF797979), modifier = Modifier.size(12.dp))
                    Text(" 4321 Parkside Cad. Beşiktaş, İstanbul", fontSize = 11.sp, color = Color(0xFF797979))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFFA12E), modifier = Modifier.size(13.dp))
                    Text(" 4.9 (385 değerlendirme)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF242424))
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.DirectionsRun, null, tint = Color(0xFF797979), modifier = Modifier.size(13.dp))
                    Text(" 12 Dk • 1.5 km", fontSize = 11.sp, color = Color(0xFF797979))
                }
            }
        }
    }
}

@Composable
fun MockMapSearch() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            // Simulated Map Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFFDEE5E5)),
                contentAlignment = Alignment.Center
            ) {
                // Background grid pattern simulations
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val spacing = 35.dp.toPx()
                    for (x in 0..size.width.toInt() step spacing.toInt()) {
                        drawLine(Color.White, androidx.compose.ui.geometry.Offset(x.toFloat(), 0f), androidx.compose.ui.geometry.Offset(x.toFloat(), size.height), strokeWidth = 1.5f)
                    }
                    for (y in 0..size.height.toInt() step spacing.toInt()) {
                        drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, y.toFloat()), androidx.compose.ui.geometry.Offset(size.width, y.toFloat()), strokeWidth = 1.5f)
                    }
                }
                // Custom Simulated Pins
                Box(modifier = Modifier.align(Alignment.Center).background(Color(0xFF185C5C), CircleShape).padding(8.dp)) {
                    Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                // Nearby Pin
                Box(modifier = Modifier.offset(x = 60.dp, y = (-20).dp).background(Color(0xFFFFA12E), CircleShape).padding(6.dp)) {
                    Icon(Icons.Filled.Storefront, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
            // Search Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = Color(0xFF797979), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salon veya Uzman Ara...", fontSize = 13.sp, color = Color(0xFF797979), modifier = Modifier.weight(1f))
                Icon(Icons.Default.Menu, null, tint = Color(0xFF185C5C), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun MockFavoritesList() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Favori Salonlar", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF242424))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFFF1F5F9)), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(44.dp).background(Color(0xFF185C5C).copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Spa, null, tint = Color(0xFF185C5C), modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text("Nail Envy Manikür & Protez", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF242424))
                    Text("Tırnak Tasarım, Manikür, Pedikür", fontSize = 11.sp, color = Color(0xFF797979))
                }
                Icon(Icons.Filled.Favorite, null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFFF1F5F9)), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(44.dp).background(Color(0xFFFFA12E).copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ContentCut, null, tint = Color(0xFFFFA12E), modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text("Kadife Dokunuş Güzellik Salonu", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF242424))
                    Text("Cilt Bakımı, Makyaj & Saç Tasarım", fontSize = 11.sp, color = Color(0xFF797979))
                }
                Icon(Icons.Filled.Favorite, null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun MockBookingsManager() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Simulated Tabs Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.background(Color(0xFF185C5C), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("Yaklaşanlar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("Tamamlananlar", fontSize = 11.sp, color = Color(0xFF797979))
                }
                Box(modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("İptal Edilenler", fontSize = 11.sp, color = Color(0xFF797979))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFFF1F5F9)), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Profesyonel Saç Tasarım & Boyama", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF242424))
                    Box(modifier = Modifier.background(Color(0xFFE0F2FE), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("Onaylandı", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("📅 18 Ocak - 13:00 • Işıltı & Zarafet", fontSize = 11.sp, color = Color(0xFF797979))
            }
        }
    }
}

@Composable
fun MockArtistChatBubble() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Artist Title Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(Color(0xFFFFA12E), CircleShape), contentAlignment = Alignment.Center) {
                    Text("S", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text("Şeyda Yıldız", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF242424))
                    Text("Çevrimiçi", fontSize = 9.sp, color = Color(0xFF10B981))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Chat bubble 1
            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                    .padding(8.dp)
            ) {
                Text("Merhaba! Bugün saat 14:00'teki saç kesim modelimi biraz değiştirebilir miyim?", fontSize = 11.sp, color = Color(0xFF242424))
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Chat bubble 2 (Response)
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(Color(0xFF185C5C), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 0.dp))
                    .padding(8.dp)
            ) {
                Text("Tabii ki, hiç sorun değil! Seansınız için yeterli süre ayırdım.", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ModernOnboardingRoleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tintColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .testTag(tag)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(tintColor.copy(alpha = 0.08f), CircleShape)
                    .border(BorderStroke(1.5.dp, tintColor.copy(alpha = 0.2f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF242424),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF797979),
                    lineHeight = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF797979),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// SECURE REGISTER & PROFILE LOGIN FLOW - EXACT SCREEN 1-TO-1
// -------------------------------------------------------------
@Composable
fun CustomerLoginScreen(viewModel: AppViewModel) {
    BackHandler { viewModel.navigateTo("ONBOARDING") }
    var nameInput by remember { mutableStateOf("Mehmet Can") }
    var phoneInput by remember { mutableStateOf("05551112233") }
    var emailInput by remember { mutableStateOf("hkprosell@gmail.com") }
    var passwordInput by remember { mutableStateOf("••••••••") }
    var referralCodeInput by remember { mutableStateOf("") }
    
    var isNameError by remember { mutableStateOf(false) }
    var isPhoneError by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simple elegant back line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo("ONBOARDING") }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color(0xFF242424),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Geri Dön",
                color = Color(0xFF797979),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Small brand graphic
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCut,
                contentDescription = null,
                tint = Color(0xFF185C5C),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = " Kuaförüm",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF185C5C)
            )
        }

        Text(
            text = "Hesap Oluştur",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF242424)
        )

        Text(
            text = "Hızlıca üye olun, bölgenizdeki en iyi salonlardan anında yer ayırtın.",
            fontSize = 13.sp,
            color = Color(0xFF797979),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp)
        )

        // Custom High Fidelity Input Boxes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            // Field 1: Name
            Text(
                text = "Ad Soyad",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF242424),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    if (it.isNotBlank()) isNameError = false
                },
                placeholder = { Text("Ahmet Yılmaz") },
                isError = isNameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_name_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF185C5C),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Field 2: Email (Authentic display)
            Text(
                text = "E-posta",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF242424),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                placeholder = { Text("ornek@gmail.com") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF185C5C),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Field 3: Phone Number
            Text(
                text = "Telefon Numarası",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF242424),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = phoneInput,
                onValueChange = {
                    phoneInput = it
                    if (it.isNotBlank()) isPhoneError = false
                },
                placeholder = { Text("0555 111 22 33") },
                isError = isPhoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_phone_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF185C5C),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Field 4: Password
            Text(
                text = "Şifre",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF242424),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF185C5C),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Field 5: Referans Kodu
            Text(
                text = "Referans Kodu (İsteğe Bağlı)",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF242424),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = referralCodeInput,
                onValueChange = { referralCodeInput = it },
                placeholder = { Text("Örn: CUST_05551112233") },
                modifier = Modifier.fillMaxWidth().testTag("auth_ref_code_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF185C5C),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Terms Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { agreeToTerms = !agreeToTerms }
            ) {
                Checkbox(
                    checked = agreeToTerms,
                    onCheckedChange = { agreeToTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF185C5C))
                )
                Text(
                    text = "Kullanım Şartlarını ve Politikaları kabul ediyorum.",
                    fontSize = 11.sp,
                    color = Color(0xFF797979),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Submit Button
            Button(
                onClick = {
                    val hasName = nameInput.trim().isNotBlank()
                    val hasPhone = phoneInput.trim().isNotBlank()
                    isNameError = !hasName
                    isPhoneError = !hasPhone
                    
                    if (hasName && hasPhone) {
                        if (agreeToTerms) {
                            val pass = passwordInput.trim()
                            if (pass.isBlank()) {
                                viewModel.triggerNotification("Hata: Şifre alanı boş bırakılamaz.")
                            } else {
                                viewModel.loginCustomer(nameInput, phoneInput, pass, referralCodeInput) { errorMsg ->
                                    viewModel.triggerNotification(errorMsg)
                                }
                            }
                        } else {
                            viewModel.triggerNotification("Hata: Platform kurallarını kabul etmelisiniz!")
                        }
                    } else {
                        viewModel.triggerNotification("Hata: Ad ve Telefon alanları zorunludur.")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_login_button")
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185C5C))
            ) {
                Text(
                    text = "Kayıt Ol & Giriş Yap",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System multi-role testing option (essential for preview validation)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF185C5C).copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Test Notu: Kayıtlı roller arasında dilediğiniz zaman onboarding ekranları üzerinden geçiş yapabilirsiniz.", fontSize = 11.sp, color = Color(0xFF185C5C), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

