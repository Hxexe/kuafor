package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(database.appDao())

    // Roles: "ONBOARDING", "CUSTOMER_AUTH", "CUSTOMER", "BUSINESS_AUTH", "BUSINESS", "ADMIN"
    private val _currentScreen = MutableStateFlow("ONBOARDING")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Active customer user details
    val customerName = MutableStateFlow("Mehmet Can")
    val customerPhone = MutableStateFlow("05551112233") // prefilled for convenience

    // Active customer select filters
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("ALL") // "ALL", "KADIN", "ERKEK", "UNISEX"
    val showOnlyFavorites = MutableStateFlow(false)

    // All approved salons visible to customer
    val salons: StateFlow<List<SalonEntity>> = repository.allApprovedSalons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered salons computed state
    val filteredSalons: StateFlow<List<SalonEntity>> = combine(
        salons,
        searchQuery,
        selectedCategoryFilter,
        showOnlyFavorites
    ) { salonList, query, catFilter, onlyFav ->
        salonList.filter { salon ->
            val matchesQuery = salon.name.contains(query, ignoreCase = true) ||
                    salon.address.contains(query, ignoreCase = true)
            val matchesCat = catFilter == "ALL" || salon.category.uppercase() == catFilter.uppercase()
            val matchesFav = !onlyFav || salon.isFavorite
            matchesQuery && matchesCat && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customer Detail & Booking Wizard States
    val selectedSalon = MutableStateFlow<SalonEntity?>(null)
    
    // Services for active salon
    val activeSalonServices: StateFlow<List<ServiceEntity>> = selectedSalon
        .flatMapLatest { salon ->
            if (salon != null) repository.getServicesBySalon(salon.id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Staff roster for active salon
    val activeSalonStaff: StateFlow<List<StaffEntity>> = selectedSalon
        .flatMapLatest { salon ->
            if (salon != null) repository.getStaffBySalon(salon.id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Coupons active salon has
    val activeSalonCoupons: StateFlow<List<CouponEntity>> = selectedSalon
        .flatMapLatest { salon ->
            if (salon != null) repository.getCouponsForSalon(salon.id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wizard choices
    val bookingSelectedServices = MutableStateFlow<List<ServiceEntity>>(emptyList())
    val bookingSelectedStaff = MutableStateFlow<StaffEntity?>(null) // null means "Fark Etmez"
    val bookingSelectedDate = MutableStateFlow("")
    val bookingSelectedTime = MutableStateFlow("")
    val bookingNote = MutableStateFlow("")
    val bookingCouponCode = MutableStateFlow("")
    val appliedCoupon = MutableStateFlow<CouponEntity?>(null)

    // Notification toast / banner message helper
    private val _notificationMessage = MutableSharedFlow<String>()
    val notificationMessage = _notificationMessage.asSharedFlow()

    // Customer appointments list flow
    val customerAppointments: StateFlow<List<AppointmentEntity>> = customerPhone
        .flatMapLatest { phone ->
            repository.getAppointmentsForCustomer(phone)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessAds = repository.getAllAdsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val loggedInStaff = MutableStateFlow<StaffEntity?>(null)
    val customerReferralCount = MutableStateFlow(0)

    // --- BUSINESS MODE STATE ---
    // The business owner is associated with a specific salon branch.
    // They can switch between their multiple branches!
    val allAdminSalons: StateFlow<List<SalonEntity>> = repository.allAdminSalons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBusinessSalonId = MutableStateFlow(1) // Defaults to 1 (Makas & Tarak)
    val activeBusinessSalon = combine(allAdminSalons, activeBusinessSalonId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current date filter for business appointment calendar tracker (Defaults to "04.06.2026")
    val businessCalendarDate = MutableStateFlow("04.06.2026")

    // Retrieve active business salon's services
    val businessServices: StateFlow<List<ServiceEntity>> = activeBusinessSalonId
        .flatMapLatest { id -> repository.getServicesBySalon(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Retrieve active business salon's staff list
    val businessStaffList: StateFlow<List<StaffEntity>> = activeBusinessSalonId
        .flatMapLatest { id -> repository.getStaffBySalon(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Retrieve appointments registered for active salon
    val businessAppointments: StateFlow<List<AppointmentEntity>> = activeBusinessSalonId
        .flatMapLatest { id -> repository.getAppointmentsForSalon(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered calendar appointments for active salon
    val filteredBusinessAppointments = combine(businessAppointments, businessCalendarDate) { appts, date ->
        appts.filter { it.date == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Business Coupons
    val businessCoupons: StateFlow<List<CouponEntity>> = activeBusinessSalonId
        .flatMapLatest { id -> repository.getCouponsForSalon(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Manual appointment creator states in Business mode
    val manualCustomerName = MutableStateFlow("")
    val manualCustomerPhone = MutableStateFlow("")
    val manualSelectedServices = MutableStateFlow<List<ServiceEntity>>(emptyList())
    val manualSelectedStaff = MutableStateFlow<StaffEntity?>(null)
    val manualSelectedDate = MutableStateFlow("04.06.2026")
    val manualSelectedTime = MutableStateFlow("")

    // Global all appointments (for Admin dashboard)
    val adminAllAppointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- GLOBAL PLATFORM ADMINISTRATIVE CONFIG & PACKAGE MANAGEMENT ---
    val commissionRatePercent = MutableStateFlow(8.0) // Customizable takerate percentage (e.g. 5%, 8%, 10%)
    val salonCommissionRates = MutableStateFlow<Map<Int, Double>>(mapOf(1 to 8.0, 2 to 6.0, 3 to 10.0))

    fun getSalonCommissionRate(salonId: Int): Double {
        return salonCommissionRates.value[salonId] ?: commissionRatePercent.value
    }

    fun updateSalonCommissionRate(salonId: Int, rate: Double) {
        val current = salonCommissionRates.value.toMutableMap()
        current[salonId] = rate.coerceIn(0.0, 100.0)
        salonCommissionRates.value = current
        triggerNotification("Şube özel komisyon oranı %${String.format(Locale.US, "%.1f", rate)} olarak güncellendi.")
    }

    data class NotificationPackage(
        val id: Int,
        val name: String,
        val type: String, // "PUSH" or "WHATSAPP"
        val count: Int,
        val price: Double,
        val description: String
    )

    data class PacketPurchase(
        val id: Int,
        val salonId: Int,
        val salonName: String,
        val packageName: String,
        val type: String, // "PUSH" or "WHATSAPP"
        val count: Int,
        val price: Double,
        val timestamp: Long = System.currentTimeMillis()
    )

    val notificationPackages = MutableStateFlow<List<NotificationPackage>>(
        listOf(
            NotificationPackage(1, "Bronz Push Paketi", "PUSH", 100, 150.0, "100 Adet Anlık Bildirim Gönderimi"),
            NotificationPackage(2, "Gümüş Push Paketi", "PUSH", 250, 320.0, "250 Adet Anlık Bildirim Gönderimi"),
            NotificationPackage(3, "Altın Push Paketi", "PUSH", 500, 550.0, "500 Adet Anlık Bildirim Gönderimi (Popüler!)"),
            NotificationPackage(4, "Bronz WP Paketi", "WHATSAPP", 100, 250.0, "100 Adet WhatsApp Hatırlatma Bildirimi"),
            NotificationPackage(5, "Gümüş WP Paketi", "WHATSAPP", 250, 520.0, "250 Adet WhatsApp Hatırlatma Bildirimi"),
            NotificationPackage(6, "Altın WP Paketi", "WHATSAPP", 500, 900.0, "500 Adet WhatsApp Hatırlatma Bildirimi (En İyi Değer!)")
        )
    )

    val packetPurchases = MutableStateFlow<List<PacketPurchase>>(
        listOf(
            PacketPurchase(1, 1, "Makas & Tarak Erkek Berberi", "Altın Push Paketi", "PUSH", 500, 550.0, System.currentTimeMillis() - 86400000),
            PacketPurchase(2, 2, "Güzellik Vadisi Kadın Salonu", "Bronz WP Paketi", "WHATSAPP", 100, 250.0, System.currentTimeMillis() - 172800000)
        )
    )

    val salonPushCredits = MutableStateFlow<Map<Int, Int>>(mapOf(1 to 120, 2 to 340, 3 to 15))
    val salonWhatsappCredits = MutableStateFlow<Map<Int, Int>>(mapOf(1 to 85, 2 to 190, 3 to 8))

    fun purchaseNotificationPackage(salonId: Int, packageId: Int) {
        val pkg = notificationPackages.value.find { it.id == packageId } ?: return
        val salonName = allAdminSalons.value.find { it.id == salonId }?.name ?: "Şube"
        
        val purchase = PacketPurchase(
            id = packetPurchases.value.size + 1,
            salonId = salonId,
            salonName = salonName,
            packageName = pkg.name,
            type = pkg.type,
            count = pkg.count,
            price = pkg.price,
            timestamp = System.currentTimeMillis()
        )
        packetPurchases.value = packetPurchases.value + purchase
        
        if (pkg.type == "PUSH") {
            val currentMap = salonPushCredits.value.toMutableMap()
            val currentCredits = currentMap[salonId] ?: 0
            currentMap[salonId] = currentCredits + pkg.count
            salonPushCredits.value = currentMap
        } else {
            val currentMap = salonWhatsappCredits.value.toMutableMap()
            val currentCredits = currentMap[salonId] ?: 0
            currentMap[salonId] = currentCredits + pkg.count
            salonWhatsappCredits.value = currentMap
        }
        
        triggerNotification("${pkg.name} başarıyla satın alındı! Entegre WhatsApp/Push paketiniz güncellendi.")
    }

    fun updateNotificationPackage(id: Int, name: String, type: String, count: Int, price: Double, description: String) {
        val current = notificationPackages.value.map { pkg ->
            if (pkg.id == id) {
                NotificationPackage(id, name, type, count, price, description)
            } else {
                pkg
            }
        }
        notificationPackages.value = current
        triggerNotification("Bildirim paketi şablonu/oranı güncellendi: $name")
    }

    fun addNotificationPackage(name: String, type: String, count: Int, price: Double, description: String) {
        val newId = (notificationPackages.value.maxOfOrNull { it.id } ?: 0) + 1
        val newPkg = NotificationPackage(newId, name, type, count, price, description)
        notificationPackages.value = notificationPackages.value + newPkg
        triggerNotification("Yeni paket başarıyla eklendi: $name")
    }

    fun deleteNotificationPackage(id: Int) {
        notificationPackages.value = notificationPackages.value.filter { it.id != id }
        triggerNotification("Paket şablonu başarıyla silindi.")
    }

    fun updateCommissionRateByAdmin(newRate: Double) {
        commissionRatePercent.value = newRate.coerceIn(0.0, 100.0)
        triggerNotification("Varsayılan platform komisyon oranı %${String.format(Locale.US, "%.1f", newRate)} olarak güncellendi.")
    }

    fun saveCustomCommissionRate(salonId: Int, rate: Double) {
        viewModelScope.launch {
            val salon = repository.getSalonById(salonId)
            if (salon != null) {
                val updated = salon.copy(commissionRate = rate)
                repository.updateSalon(updated)
                
                // Update local state flow mapping just in case it is observed elsewhere
                val currentRates = salonCommissionRates.value.toMutableMap()
                currentRates[salonId] = rate
                salonCommissionRates.value = currentRates
                
                _notificationMessage.emit("Salon komisyon oranı %$rate olarak veritabanına kaydedildi.")
            }
        }
    }

    fun addNewAd(title: String, imageUrl: String, targetSalonId: Int?) {
        viewModelScope.launch {
            val ad = AdEntity(title = title, imageUrl = imageUrl, targetSalonId = targetSalonId, isActive = true)
            repository.insertAd(ad)
            _notificationMessage.emit("Yeni reklam afişi başarıyla eklendi.")
        }
    }

    fun deleteAd(ad: AdEntity) {
        viewModelScope.launch {
            repository.deleteAd(ad)
            _notificationMessage.emit("Reklam başarıyla kaldırıldı.")
        }
    }

    private val prefs = application.getSharedPreferences("kuafor_prefs", android.content.Context.MODE_PRIVATE)

    init {
        // Load persistable elements
        _currentScreen.value = prefs.getString("current_screen", "ONBOARDING") ?: "ONBOARDING"
        customerName.value = prefs.getString("customer_name", "Mehmet Can") ?: "Mehmet Can"
        customerPhone.value = prefs.getString("customer_phone", "05551112233") ?: "05551112233"
        activeBusinessSalonId.value = prefs.getInt("active_business_salon_id", 1)
        commissionRatePercent.value = prefs.getFloat("commission_rate_percent", 8.0f).toDouble()

        // Set initial booking date to tomorrow
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        bookingSelectedDate.value = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(tomorrow.time)

        // Listen for changes and save to preferences automatically
        viewModelScope.launch {
            _currentScreen.collect { screen ->
                prefs.edit().putString("current_screen", screen).apply()
            }
        }
        viewModelScope.launch {
            customerName.collect { name ->
                prefs.edit().putString("customer_name", name).apply()
            }
        }
        viewModelScope.launch {
            customerPhone.collect { phone ->
                prefs.edit().putString("customer_phone", phone).apply()
            }
        }
        viewModelScope.launch {
            activeBusinessSalonId.collect { id ->
                prefs.edit().putInt("active_business_salon_id", id).apply()
            }
        }
        viewModelScope.launch {
            commissionRatePercent.collect { rate ->
                prefs.edit().putFloat("commission_rate_percent", rate.toFloat()).apply()
            }
        }

        // Robust database seeding fallback block
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.getSalonsCount() == 0) {
                    AppDatabase.populateDatabase(database.appDao())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Auto sync commission rates from DB to the local map
        viewModelScope.launch {
            repository.allAdminSalons.collect { salons ->
                val ratesMap = salons.associate { it.id to it.commissionRate }
                salonCommissionRates.value = ratesMap
            }
        }
    }

    // Helper to trigger floating dynamic notification messages
    fun triggerNotification(message: String) {
        viewModelScope.launch {
            _notificationMessage.emit(message)
        }
    }

    // Navigation triggers
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // Toggle Favorite
    fun toggleFavoriteSalon(salonId: Int, currentFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(salonId, !currentFav)
            _notificationMessage.emit(if (!currentFav) "Favorilere eklendi" else "Favorilerden çıkarıldı")
        }
    }

    // Complete customer login / profile update
    fun loginCustomer(name: String, phone: String, password: String, refCode: String = "", onFail: (String) -> Unit) {
        if (name.isBlank() || phone.isBlank() || password.isBlank()) {
            onFail("Tüm alanlar zorunludur.")
            return
        }
        val passKey = "customer_password_$phone"
        if (prefs.contains(passKey)) {
            val storedPass = prefs.getString(passKey, "")
            if (storedPass != password) {
                onFail("Hata: Şifre hatalı!")
                return
            }
        } else {
            // Register new user
            prefs.edit().putString(passKey, password).apply()
        }
        customerName.value = name
        customerPhone.value = phone
        _currentScreen.value = "CUSTOMER"
        
        if (refCode.trim().isNotBlank()) {
            handleReferralRegistration(refCode.trim().uppercase(), phone)
        } else {
            viewModelScope.launch {
                _notificationMessage.emit("Giriş yapıldı: Hoş geldiniz, $name")
            }
        }
    }

    fun loginBusinessOrStaff(phone: String, password: String, onFail: (String) -> Unit) {
        viewModelScope.launch {
            val normalizedPhone = phone.replace(" ", "")
            if (normalizedPhone == "05559998877") {
                val salon = repository.getSalonById(1)
                if (salon != null && salon.password == password) {
                    loggedInStaff.value = null
                    navigateTo("BUSINESS")
                } else {
                    onFail("Hata: İşletme sahibi şifresi yanlış!")
                }
            } else {
                val staff = repository.getStaffByPhone(normalizedPhone)
                if (staff != null) {
                    if (staff.password == password) {
                        loggedInStaff.value = staff
                        activeBusinessSalonId.value = staff.salonId
                        navigateTo("STAFF_PANEL")
                    } else {
                        onFail("Hata: Çalışan şifresi yanlış!")
                    }
                } else {
                    onFail("Telefon numarası sistemde kayıtlı bir salon sahibine veya çalışana ait değil!")
                }
            }
        }
    }

    fun updateStaffProfile(staff: StaffEntity) {
        viewModelScope.launch {
            repository.insertStaff(staff)
            loggedInStaff.value = staff
            triggerNotification("Profiliniz başarıyla güncellendi.")
        }
    }

    fun handleReferralRegistration(refCode: String, newCustomerPhone: String) {
        viewModelScope.launch {
            if (refCode.startsWith("STAFF_")) {
                val staffId = refCode.removePrefix("STAFF_").toIntOrNull()
                if (staffId != null) {
                    repository.incrementStaffReferral(staffId)
                    // Müşteriye %10 hoş geldin kuponu
                    val welcomeCoupon = CouponEntity(
                        salonId = 0, // tüm salonlarda geçerli
                        code = "HOSGELDIN_${newCustomerPhone.takeLast(4)}",
                        discountType = "PERCENT",
                        value = 10.0
                    )
                    repository.insertCoupon(welcomeCoupon)
                    _notificationMessage.emit("Referanslı kayıt başarılı! İlk randevunuz için %10 kupon tanımlandı.")
                }
            } else if (refCode.startsWith("CUST_")) {
                val referrerPhone = refCode.removePrefix("CUST_")
                // Davet edene %15 kupon
                val refCoupon = CouponEntity(
                    salonId = 0,
                    code = "DAVET_${referrerPhone.takeLast(4)}",
                    discountType = "PERCENT",
                    value = 15.0
                )
                // Davet edilene %10 kupon
                val welcomeCoupon = CouponEntity(
                    salonId = 0,
                    code = "HOSGELDIN_${newCustomerPhone.takeLast(4)}",
                    discountType = "PERCENT",
                    value = 10.0
                )
                repository.insertCoupon(refCoupon)
                repository.insertCoupon(welcomeCoupon)
                _notificationMessage.emit("Referanslı üyelik! Hem size hem arkadaşınıza indirim kuponları tanımlandı.")
            }
        }
    }

    // Add service to wizard selection
    fun toggleServiceSelection(service: ServiceEntity) {
        val current = bookingSelectedServices.value.toMutableList()
        if (current.any { it.id == service.id }) {
            current.removeAll { it.id == service.id }
        } else {
            current.add(service)
        }
        bookingSelectedServices.value = current
    }

    // Apply Coupon Code
    fun applyCoupon(code: String) {
        viewModelScope.launch {
            val coupon = repository.getCouponByCode(code.trim().uppercase())
            if (coupon != null && (coupon.salonId == selectedSalon.value?.id || coupon.salonId == 0)) {
                appliedCoupon.value = coupon
                _notificationMessage.emit("Kupon başarıyla uygulandı: %${coupon.value} indirim!")
            } else {
                appliedCoupon.value = null
                _notificationMessage.emit("Geçersiz kupon kodu!")
            }
        }
    }

    // Submit Appointment (Customer Side)
    fun confirmBooking() {
        val salon = selectedSalon.value ?: return
        val services = bookingSelectedServices.value
        if (services.isEmpty()) return

        val sIds = services.joinToString(",") { it.id.toString() }
        val sNames = services.joinToString(", ") { it.name }
        val totalPr = services.sumOf { it.price }
        val finalPr = appliedCoupon.value?.let { coupon ->
            if (coupon.discountType == "PERCENT") {
                totalPr * (1 - (coupon.value / 100.0))
            } else {
                (totalPr - coupon.value).coerceAtLeast(0.0)
            }
        } ?: totalPr

        val totalDur = services.sumOf { it.durationMinutes }

        val payMethod = bookingPaymentMethod.value
        if (payMethod == "WALLET" && customerWalletBalance.value < finalPr) {
            triggerNotification("Yetersiz Cüzdan Bakiyesi! Lütfen nakit veya kart seçin ya da bakiye yükleyin.")
            return
        }

        viewModelScope.launch {
            if (payMethod == "WALLET") {
                customerWalletBalance.value -= finalPr
                val nowStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
                val trx = WalletTransaction(
                    id = walletTransactions.value.size + 1,
                    type = "PAYMENT",
                    title = "${salon.name} Ödemesi",
                    amount = -finalPr,
                    date = nowStr
                )
                walletTransactions.value = listOf(trx) + walletTransactions.value
            }

            val appointment = AppointmentEntity(
                salonId = salon.id,
                salonName = salon.name,
                serviceIds = sIds,
                serviceNames = sNames,
                staffId = bookingSelectedStaff.value?.id,
                staffName = bookingSelectedStaff.value?.name ?: "Fark Etmez",
                date = bookingSelectedDate.value,
                time = bookingSelectedTime.value,
                totalPrice = finalPr,
                totalDuration = totalDur,
                customerName = customerName.value,
                customerPhone = customerPhone.value,
                note = bookingNote.value,
                status = "PENDING", // needs business approval
                timestamp = System.currentTimeMillis()
            )
            repository.insertAppointment(appointment)
            _notificationMessage.emit("Randevu talebi oluşturuldu! Onay bekleniyor.")

            // Reset wizard states
            bookingSelectedServices.value = emptyList()
            bookingSelectedStaff.value = null
            bookingSelectedTime.value = ""
            bookingNote.value = ""
            appliedCoupon.value = null
            bookingCouponCode.value = ""

            // Navigate back to my appointments
            navigateTo("CUSTOMER_BOOKINGS")
        }
    }

    // Rate / Review an appointment
    fun submitRating(appointmentId: Int, stars: Int, comment: String) {
        viewModelScope.launch {
            repository.rateAppointment(appointmentId, stars, comment)
            
            // Recalculate average rating for the salon
            val appt = repository.getAppointmentById(appointmentId)
            if (appt != null) {
                val salonAppts = repository.getAppointmentsForSalonSync(appt.salonId)
                val ratedAppts = salonAppts.filter { it.isRated || it.id == appointmentId }
                val totalStars = ratedAppts.sumOf { if (it.id == appointmentId) stars else it.ratingStars }
                val count = ratedAppts.size
                val avgRating = if (count > 0) totalStars.toFloat() / count else 0.0f
                
                val salon = repository.getSalonById(appt.salonId)
                if (salon != null) {
                    val updatedSalon = salon.copy(rating = avgRating, reviewCount = count)
                    repository.updateSalon(updatedSalon)
                }
            }
            
            _notificationMessage.emit("Yorumunuz ve puanınız iletildi, teşekkürler!")
        }
    }

    // Cancel appointment (Customer Side)
    fun cancelAppointment(appointmentId: Int) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointmentId, "REJECTED")
            _notificationMessage.emit("Randevu iptal edildi.")
        }
    }

    // --- BUSINESS ACTIONS ---
    fun updateAppointmentStatus(appointmentId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointmentId, newStatus)
            val trStatus = when(newStatus) {
                "APPROVED" -> "onaylandı"
                "REJECTED" -> "reddedildi"
                "COMPLETED" -> "tamamlandı"
                "NO_SHOW" -> "gelmedi"
                else -> newStatus
            }
            _notificationMessage.emit("Randevu $trStatus olarak güncellendi.")
        }
    }

    // Register / Add a salon branch
    fun createSalonBranch(name: String, category: String, address: String, hours: String) {
        viewModelScope.launch {
            val newSalon = SalonEntity(
                name = name,
                category = category,
                address = address,
                imageUrl = "https://images.unsplash.com/photo-1621605815971-fbc98d665033?auto=format&fit=crop&w=500&q=80",
                rating = 5.0f,
                reviewCount = 0,
                workingHours = hours,
                isApproved = true // auto approved for testing convenience
            )
            val newId = repository.insertSalon(newSalon).toInt()
            
            // Seed a default service & staff for the new branch immediately so it works!
            repository.insertService(ServiceEntity(salonId = newId, name = "Standart Saç Kesimi", price = 200.0, durationMinutes = 30, category = "SACH_KESIMI", description = "Yeni şubemizin ilk hizmeti"))
            repository.insertStaff(StaffEntity(salonId = newId, name = "Yeni Personel 1", role = "Usta Stilist", imageUrl = ""))

            activeBusinessSalonId.value = newId
            _notificationMessage.emit("Yeni şube başarıyla açıldı!")
        }
    }

    // Business adds service
    fun addBusinessService(name: String, price: Double, duration: Int, category: String, desc: String) {
        viewModelScope.launch {
            val s = ServiceEntity(
                salonId = activeBusinessSalonId.value,
                name = name,
                price = price,
                durationMinutes = duration,
                category = category,
                description = desc
            )
            repository.insertService(s)
            _notificationMessage.emit("Yeni hizmet başarıyla eklendi.")
        }
    }

    // Business deletes service
    fun deleteBusinessService(id: Int) {
        viewModelScope.launch {
            repository.deleteService(id)
            _notificationMessage.emit("Hizmet silindi.")
        }
    }

    // Business adds staff
    fun addBusinessStaff(name: String, role: String, hours: String = "09:00 - 19:00", offDys: String = "Pazar") {
        viewModelScope.launch {
            val stf = StaffEntity(
                salonId = activeBusinessSalonId.value,
                name = name,
                role = role,
                imageUrl = "",
                workingHours = hours,
                offDays = offDys
            )
            repository.insertStaff(stf)
            _notificationMessage.emit("Yeni personel başarıyla eklendi.")
        }
    }

    fun deleteBusinessStaff(id: Int) {
        viewModelScope.launch {
            repository.deleteStaff(id)
            _notificationMessage.emit("Personel silindi.")
        }
    }

    // Business creates a discount coupon
    fun addCoupon(code: String, discountType: String, value: Double, minAmt: Double, limit: Int) {
        viewModelScope.launch {
            val coup = CouponEntity(
                salonId = activeBusinessSalonId.value,
                code = code.trim().uppercase(),
                discountType = discountType,
                value = value,
                minAmount = minAmt,
                limitCount = limit
            )
            repository.insertCoupon(coup)
            _notificationMessage.emit("Promosyon kuponu tanımlandı!")
        }
    }

    fun deleteCoupon(id: Int) {
        viewModelScope.launch {
            repository.deleteCoupon(id)
            _notificationMessage.emit("Kupon silindi.")
        }
    }

    // Create Business Manual Phone Booking
    fun addManualAppointment() {
        val name = manualCustomerName.value
        val phone = manualCustomerPhone.value
        val services = manualSelectedServices.value
        if (name.isBlank() || phone.isBlank() || services.isEmpty() || manualSelectedTime.value.isBlank()) return

        val sIds = services.joinToString(",") { it.id.toString() }
        val sNames = services.joinToString(", ") { it.name }
        val totalPr = services.sumOf { it.price }
        val totalDur = services.sumOf { it.durationMinutes }

        viewModelScope.launch {
            val booking = AppointmentEntity(
                salonId = activeBusinessSalonId.value,
                salonName = activeBusinessSalon.value?.name ?: "Şubem",
                serviceIds = sIds,
                serviceNames = sNames,
                staffId = manualSelectedStaff.value?.id,
                staffName = manualSelectedStaff.value?.name ?: "Fark Etmez",
                date = manualSelectedDate.value,
                time = manualSelectedTime.value,
                totalPrice = totalPr,
                totalDuration = totalDur,
                customerName = name,
                customerPhone = phone,
                note = "Telefonla gelen rezervasyon",
                status = "APPROVED", // manual insertion is auto-approved!
                timestamp = System.currentTimeMillis()
            )
            repository.insertAppointment(booking)
            _notificationMessage.emit("Manuel telefon randevusu takvime işlendi!")

            // Clear states
            manualCustomerName.value = ""
            manualCustomerPhone.value = ""
            manualSelectedServices.value = emptyList()
            manualSelectedStaff.value = null
            manualSelectedTime.value = ""
        }
    }

    // Platform Admin triggers approval callback
    fun approveSalonByAdmin(salon: SalonEntity) {
        viewModelScope.launch {
            val updated = salon.copy(isApproved = true, status = "APPROVED")
            repository.updateSalon(updated)
            _notificationMessage.emit("${salon.name} onaylandı.")
        }
    }

    fun restrictSalonByAdmin(salon: SalonEntity) {
        viewModelScope.launch {
            val updated = salon.copy(isApproved = false, status = "SUSPENDED")
            repository.updateSalon(updated)
            _notificationMessage.emit("${salon.name} kısıtlandı/yayından çekildi.")
        }
    }

    fun rejectSalonByAdmin(salon: SalonEntity) {
        viewModelScope.launch {
            repository.deleteSalon(salon)
            _notificationMessage.emit("${salon.name} başvurusu reddedildi.")
        }
    }

    // --- CHAT WITH ARTIST STATE ---
    data class ChatMessage(
        val id: Int,
        val sender: String,
        val text: String,
        val timestamp: String,
        val isUser: Boolean,
        val hasVoice: Boolean = false
    )

    val activeChatStaffId = MutableStateFlow<Int?>(null)
    
    // Conversation databases in memory
    val chatMessages = MutableStateFlow<Map<Int, List<ChatMessage>>>(
        mapOf(
            1 to listOf(
                ChatMessage(1, "Sheila L.", "Merhaba Mehmet Bey! Randevunuzu sabırsızlıkla bekliyorum.", "09:34", false),
                ChatMessage(2, "Mehmet Can", "Teşekkür ederim Sheila Hanım, istediğim saç modelinin görselini çektim getireceğim.", "09:36", true)
            ),
            2 to listOf(
                ChatMessage(1, "Mason P.", "Saç kesiminiz için özel bir tarz arıyor musunuz?", "Hafta içi", false)
            )
        )
    )

    fun sendChatMessage(staffId: Int, text: String) {
        if (text.isBlank()) return
        val currentList = chatMessages.value[staffId]?.toMutableList() ?: mutableListOf()
        val nextId = currentList.size + 1
        
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
        val newMsg = ChatMessage(nextId, "Mehmet Can", text, now, true)
        currentList.add(newMsg)
        
        val updatedMap = chatMessages.value.toMutableMap()
        updatedMap[staffId] = currentList
        chatMessages.value = updatedMap

        // Stylist response simulator
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            val staffName = "Beste Berber" 
            val responseText = when {
                text.contains("merhaba", ignoreCase = true) || text.contains("selam", ignoreCase = true) -> 
                    "Merhaba Mehmet Bey! Yardımcı olabileceğim bir konu var mı?"
                text.contains("fiyat", ignoreCase = true) -> 
                    "Tabii ki, profilimizdeki Hizmetler sekmesinden güncel fiyat tarifelerimize erişebilirsiniz."
                else -> 
                    "Mesajınızı aldım! Çok teşekkürler, randevu saatinizde görüşmek üzere harika bir tarz oluşturacağız. 💇‍♂️✨"
            }
            val intermediateList = chatMessages.value[staffId]?.toMutableList() ?: mutableListOf()
            intermediateList.add(ChatMessage(intermediateList.size + 1, staffName, responseText, now, false))
            val finalMap = chatMessages.value.toMutableMap()
            finalMap[staffId] = intermediateList
            chatMessages.value = finalMap
        }
    }

    // --- CUSTOMER WALLET MANAGEMENT ---
    data class WalletTransaction(
        val id: Int,
        val type: String, // "TOP_UP" or "PAYMENT"
        val title: String,
        val amount: Double,
        val date: String
    )

    val customerWalletBalance = MutableStateFlow(800.0) // Matches screenshot 800
    val walletTransactions = MutableStateFlow<List<WalletTransaction>>(
        listOf(
            WalletTransaction(1, "TOP_UP", "Bakiye Yükleme (Kredi Kartı)", 250.0, "05.06.2026"),
            WalletTransaction(2, "PAYMENT", "Radiance Salon Ödemesi", -150.0, "02.06.2026"),
            WalletTransaction(3, "TOP_UP", "Cüzdan Açılış Hediyesi", 500.0, "15.05.2026")
        )
    )

    fun addMoneyToWallet(amount: Double) {
        val currentBalance = customerWalletBalance.value
        customerWalletBalance.value = currentBalance + amount
        
        val now = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        val transaction = WalletTransaction(
            id = walletTransactions.value.size + 1,
            type = "TOP_UP",
            title = "Bakiye Yükleme (Kredi Kartı)",
            amount = amount,
            date = now
        )
        walletTransactions.value = listOf(transaction) + walletTransactions.value
        triggerNotification("Cüzdanınıza ₺${amount.toInt()} başarıyla yüklendi!")
    }

    // Wizard active payment method
    val bookingPaymentMethod = MutableStateFlow("CASH") // "CASH", "WALLET", "CARD"

    // --- SPECIALIST/ARTIST DETAIL STATE ---
    val selectedStaffDetail = MutableStateFlow<StaffEntity?>(null)

    // --- MY COUPONS VIEW STATE ---
    data class CouponItem(val code: String, val desc: String, val discount: String)
    val myCouponsList = listOf(
        CouponItem("WELCOME50", "Mülkiyet Açılışına Özel ₺50 İndirim", "₺50"),
        CouponItem("CASHBACK20", "Tüm Hizmetlerde %20 Para İadesi", "%20"),
        CouponItem("FEST25", "Yaz Festivaline Özel %25 İndirim", "%25"),
        CouponItem("SUPER50", "Hafta içi Alımlarda Süper Net ₺50 İndirim", "₺50")
    )

    fun changeBusinessCalendarDate(days: Int) {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        try {
            cal.time = sdf.parse(businessCalendarDate.value) ?: java.util.Date()
            cal.add(java.util.Calendar.DAY_OF_YEAR, days)
            businessCalendarDate.value = sdf.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    fun calculateAvailableSlots(
        workingHours: String,
        existingAppointments: List<AppointmentEntity>,
        totalDurationMinutes: Int
    ): List<String> {
        if (workingHours.isBlank() || totalDurationMinutes <= 0) return emptyList()
        val parts = workingHours.split("-")
        if (parts.size != 2) return emptyList()
        
        val startWork = parseTimeToMinutes(parts[0].trim())
        val endWork = parseTimeToMinutes(parts[1].trim())
        
        val availableSlots = mutableListOf<String>()
        var current = startWork
        
        while (current + totalDurationMinutes <= endWork) {
            val slotStart = current
            val slotEnd = current + totalDurationMinutes
            
            val isOverlapping = existingAppointments.any { appt ->
                val apptStart = parseTimeToMinutes(appt.time)
                val apptEnd = apptStart + appt.totalDuration
                slotStart < apptEnd && apptStart < slotEnd
            }
            
            if (!isOverlapping) {
                val hour = current / 60
                val minute = current % 60
                availableSlots.add(String.format(Locale.US, "%02d:%02d", hour, minute))
            }
            
            current += 15
        }
        
        return availableSlots
    }
}

