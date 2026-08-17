# Kuaförüm Uygulaması Özellikleri Uygulama Planı

> **Ajan Çalışanlar İçin:** GEREKLİ ALT YETENEK: Bu planı görev görev (task-by-task) uygulamak için `superpowers:subagent-driven-development` veya `superpowers:executing-plans` yeteneğini kullanın. Adımlar takip için onay kutusu (`- [ ]`) sözdizimini kullanır.

**Hedef:** Kuaförüm uygulamasının onboarding, akıllı giriş, rol ayrımı, sürtünmesiz müşteri deneyimi, kısıtlı çalışan paneli, liderlik tablosu, zaman dilimli manuel randevu ve admin reklam/komisyon yönetimini içeren tüm eksikliklerinin tamamlanması.

**Mimari:** Room veri modelleri (`StaffEntity` ve `AdEntity`) güncellenecek. `AppViewModel` üzerinde giriş, rol kontrolü, komisyon ve reklam yönetim akışları tasarlanacak. UI katmanı (`MainActivity`, `CustomerScreens`, `BusinessScreens`, `AdminScreens`) Jetpack Compose kullanılarak yeni tasarımlara göre güncellenecek.

**Teknoloji Yığını:** Kotlin, Jetpack Compose, Room Database, StateFlow, Coroutines.

---

### Görev 1: Veri Modellerinin ve DAO Sınıflarının Güncellenmesi

**Dosyalar:**
* Güncelle: [Entities.kt](file:///c:/Users/AOSB/OneDrive%20-%20Adana%20Hac%C4%B1%20Sabanc%C4%B1%20Organize%20Sanayi%20B%C3%B6lgesi%20B%C3%B6lge%20M%C3%BCd%C3%BCrl%C3%BC%C4%9F%C3%BC/Masa%C3%BCst%C3%BC/hk/ku/app/src/main/java/com/example/data/Entities.kt)
* Güncelle: [AppDao.kt](file:///c:/Users/AOSB/OneDrive%20-%20Adana%20Hac%C4%B1%20Sabanc%C4%B1%20Organize%20Sanayi%20B%C3%B6lgesi%20B%C3%B6lge%20M%C3%BCd%C3%BCrl%C3%BC%C4%9F%C3%BC/Masa%C3%BCst%C3%BC/hk/ku/app/src/main/java/com/example/data/AppDao.kt)
* Güncelle: [AppDatabase.kt](file:///c:/Users/AOSB/OneDrive%20-%20Adana%20Hac%C4%B1%20Sabanc%C4%B1%20Organize%20Sanayi%20B%C3%B6lgesi%20B%C3%B6lge%20M%C3%BCd%C3%BCrl%C3%BC%C4%9F%C3%BC/Masa%C3%BCst%C3%BC/hk/ku/app/src/main/java/com/example/data/AppDatabase.kt)

- [x] **Adım 1: StaffEntity ve AdEntity modellerini Entities.kt dosyasına ekleyin**
  
  `Entities.kt` içindeki `StaffEntity` sınıfını güncelleyin ve yeni `AdEntity` sınıfını ekleyin:
  ```kotlin
  @Entity(tableName = "staff")
  data class StaffEntity(
      @PrimaryKey(autoGenerate = true) val id: Int = 0,
      val salonId: Int,
      val name: String,
      val role: String,
      val imageUrl: String,
      val rating: Float = 4.8f,
      val workingHours: String = "09:00 - 19:00",
      val offDays: String = "Pazar",
      val phone: String = "", // Telefon giriş eşleştirmesi için
      val referralCount: Int = 0 // Liderlik tablosu üye puanı için
  )

  @Entity(tableName = "ads")
  data class AdEntity(
      @PrimaryKey(autoGenerate = true) val id: Int = 0,
      val title: String,
      val imageUrl: String,
      val targetSalonId: Int?,
      val isActive: Boolean = true
  )
  ```

- [x] **Adım 2: AppDatabase.kt içindeki veritabanı sürümünü ve tablolarını güncelleyin**
  
  `AppDatabase.kt` dosyasına `AdEntity` sınıfını ekleyin ve başlangıç verilerinde (initializer) çalışanların telefon numaralarını tanımlayın:
  ```kotlin
  // Entities listesine AdEntity::class ekleyin.
  // Başlangıç verisi eklenen yerde Kadir ve Samet için telefon numarası ekleyin:
  // StaffEntity(id = 1, salonId = 1, name = "Berber Kadir", ..., phone = "05551112233", referralCount = 3)
  // StaffEntity(id = 2, salonId = 1, name = "Barber Samet", ..., phone = "05553334455", referralCount = 1)
  ```

- [x] **Adım 3: AppDao.kt içine yeni sorguları ekleyin**
  
  ```kotlin
  @Query("SELECT * FROM staff WHERE phone = :phone LIMIT 1")
  suspend fun getStaffByPhone(phone: String): StaffEntity?

  @Query("UPDATE staff SET referralCount = referralCount + 1 WHERE id = :staffId")
  suspend fun incrementStaffReferral(staffId: Int)

  @Query("SELECT * FROM ads")
  fun getAllAdsFlow(): Flow<List<AdEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAd(ad: AdEntity)

  @Delete
  suspend fun deleteAd(ad: AdEntity)
  ```

- [x] **Adım 4: Projenin derlendiğini doğrulayın**
  
  PowerShell terminalinden derleme komutunu çalıştırın:
  `$env:JAVA_HOME = "C:\Users\AOSB\AppData\Local\Temp\jdk17\jdk-17.0.11+9"; & "$env:JAVA_HOME\bin\java.exe" -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain compileDebugKotlin`
  
  Beklenen çıktı: `BUILD SUCCESSFUL`

- [x] **Adım 5: Commit yapın**
  
  `git add app/src/main/java/com/example/data/*`
  `git commit -m "database: veritabanı tabloları ve dao sorguları güncellendi"`

---

### Görev 2: AppViewModel İş Mantığı ve Rol Kontrolü Güncellemeleri

**Dosyalar:**
* Güncelle: [AppViewModel.kt](file:///c:/Users/AOSB/OneDrive%20-%20Adana%20Hac%C4%B1%20Sabanc%C4%B1%20Organize%20Sanayi%20B%C3%B6lgesi%20B%C3%B6lge%20M%C3%BCd%C3%BCrl%C3%BC%C4%9F%C3%BC/Masa%C3%BCst%C3%BC/hk/ku/app/src/main/java/com/example/ui/AppViewModel.kt)

- [x] **Adım 1: Yeni StateFlow ve Değişkenleri Tanımlayın**
  
  `AppViewModel` sınıfına giriş rolleri, reklamlar ve referans yönetimi için gerekli akışları ekleyin:
  ```kotlin
  val businessAds = repository.getAllAdsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val loggedInStaff = MutableStateFlow<StaffEntity?>(null)
  
  // Müşteri referans sayacı
  val customerReferralCount = MutableStateFlow(0)
  ```

- [x] **Adım 2: Telefon Numarası ile Giriş/Rol Doğrulama Metodunu Yazın**
  
  ```kotlin
  fun loginBusinessOrStaff(phone: String, onFail: (String) -> Unit) {
      viewModelScope.launch {
          if (phone == "05559998877") {
              // Salon Sahibi
              loggedInStaff.value = null
              navigateTo("BUSINESS")
          } else {
              val staff = repository.getStaffByPhone(phone.replace(" ", ""))
              if (staff != null) {
                  loggedInStaff.value = staff
                  activeBusinessSalonId.value = staff.salonId
                  navigateTo("STAFF_PANEL")
              } else {
                  onFail("Telefon numarası sistemde kayıtlı bir salon sahibine veya çalışana ait değil!")
              }
          }
      }
  }
  ```

- [x] **Adım 3: Çift Taraflı Kupon Tanımlama İş Mantığını Ekleyin**
  
  ```kotlin
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
  ```

- [x] **Adım 4: Reklam ve Özel Komisyon Kaydetme Fonksiyonlarını Ekleyin**
  
  ```kotlin
  fun saveCustomCommissionRate(salonId: Int, rate: Double) {
      viewModelScope.launch {
          val currentRates = salonCommissionRates.value.toMutableMap()
          currentRates[salonId] = rate
          salonCommissionRates.value = currentRates
          _notificationMessage.emit("Salon komisyon oranı %$rate olarak güncellendi.")
      }
  }

  fun addNewAd(title: String, imageUrl: String, targetSalonId: Int?) {
      viewModelScope.launch {
          val ad = AdEntity(title = title, imageUrl = imageUrl, targetSalonId = targetSalonId, isActive = true)
          repository.insertAd(ad)
          _notificationMessage.emit("Yeni reklam afişi başarıyla eklendi.")
      }
  }
  ```

- [x] **Adım 5: Commit yapın**
  
  `git add app/src/main/java/com/example/ui/AppViewModel.kt`
  `git commit -m "viewmodel: giriş rol kontrolü, çift taraflı referans kuponları ve admin fonksiyonları eklendi"`

---

### Görev 3: Giriş (Onboarding) Ekranı Redesign & Navigasyon

**Dosyalar:**
* Güncelle: [MainActivity.kt](file:///c:/Users/AOSB/OneDrive%20-%20Adana%20Hac%C4%B1%20Sabanc%C4%B1%20Organize%20Sanayi%20B%C3%B6lgesi%20B%C3%B6lge%20M%C3%BCd%C3%BCrl%C3%BC%C4%9F%C3%BC/Masa%C3%BCst%C3%BC/hk/ku/app/src/main/java/com/example/MainActivity.kt)
* Güncelle: [BusinessScreens.kt](file:///c:/Users/AOSB/OneDrive%20-%20Adana%20Hac%C4%B1%20Sabanc%C4%B1%20Organize%20Sanayi%20B%C3%B6lgesi%20B%C3%B6lge%20M%C3%BCd%C3%BCrl%C3%BC%C4%9F%C3%BC/Masa%C3%BCst%C3%BC/hk/ku/app/src/main/java/com/example/ui/BusinessScreens.kt)

- [x] **Adım 1: Onboarding Rol Kartlarını Dikey ve Ferah Hale Getirin**
  
  `MainActivity.kt` içindeki rol kartı tanımlarını dikey, 130dp yüksekliğinde ve modern tasarımla güncelleyin. İkonlara dairesel renkli arka planlar verin.
  
- [x] **Adım 2: Salon Girişi İçin Telefon Giriş Ekranını Ekleyin**
  
  Onboarding ekranından "Salon Sahibi & Çalışanı" kartına tıklandığında açılacak telefon numarası girme sayfasını tasarlayın:
  * "Şifremi Unuttum" butonu ekleyin (Tıklandığında "Şifre sıfırlama linki kayıtlı numaranıza iletildi" mesajı göstersin).
  * `viewModel.loginBusinessOrStaff` fonksiyonunu çağırıp başarılı yönlendirme yapın.
  
- [x] **Adım 3: MainActivity Navigasyon Yönlendirmelerini Ekleyin**
  
  `MainActivity.kt` içindeki navigation `screen` kontrolüne `STAFF_PANEL` rolünü ekleyerek `StaffMainScreen(appViewModel)` bileşenini çağırın.

- [x] **Adım 4: Kısıtlı Çalışan Paneli Ekranını Tasarlayın**
  
  `BusinessScreens.kt` dosyasına `StaffMainScreen` Composable fonksiyonunu ekleyin:
  * Sadece "Takvim" ve "Profilim" sekmeleri (bottom navigation) yer alsın.
  * Takvim sekmesinde sadece `viewModel.loggedInStaff` nesnesinin ID'sine ait randevular görüntülensin.
  * Çalışan profil sekmesinde isim/fotoğraf güncellemesi ve **Salon İçi Liderlik Tablosu** (referans sayısı ve puan sıralamalı liste) yer alsın.
  
- [x] **Adım 5: Commit yapın**
  
  `git add app/src/main/java/com/example/MainActivity.kt app/src/main/java/com/example/ui/BusinessScreens.kt`
  `git commit -m "ui: onboarding kartları güncellendi, telefon giriş ekranı ve çalışan paneli eklendi"`

---

### Görev 4: Müşteri Paneli Harita Görünümü, Hızlı Hizmet Seçimi ve Yorumlar

**Dosyalar:**
* Güncelle: [CustomerScreens.kt](file:///c:/Users/AOSB/OneDrive%20-%20Adana%20Hac%C4%B1%20Sabanc%C4%B1%20Organize%20Sanayi%20B%C3%B6lgesi%20B%C3%B6lge%20M%C3%BCd%C3%BCrl%C3%BC%C4%9F%C3%BC/Masa%C3%BCst%C3%BC/hk/ku/app/src/main/java/com/example/ui/CustomerScreens.kt)

- [x] **Adım 1: Harita ve Liste Görünüm Sekmelerini Tasarlayın**
  
  * Customer main screen üzerine "Liste" ve "Harita" sekmelerini yerleştirin.
  * Harita görünümünü şık bir `Canvas` veya görsel yardımıyla simüle edin. Haritada salonların konumlarını işaretleyen butonlar (pinler) yerleştirin.
  * Bir pin'e tıklandığında ekranın altında salon adı, puanı, mesafesi ve "Randevu Al" butonunu içeren şık bir kart yukarı doğru açılsın.

- [x] **Adım 2: Liste Görünümüne Öne Çıkanlar Carousel'i ve Reklam Alanı Ekleyin**
  
  * Liste görünümünün en üstüne "Öne Çıkan Salonlar" adı altında yatay kaydırılabilen salonlar ekleyin.
  * `viewModel.businessAds` akışından aktif reklamları okuyan bir reklam banner alanı yerleştirin. Banner tıklandığında ilgili salon sayfasına gitsin.

- [x] **Adım 3: Sürtünmesiz Hizmet Kartı Seçimi ve Sticky Bottom Bar Yapısı**
  
  * Salon detaylarında hizmet kartlarının doğrudan tıklanabilir olmasını sağlayın. Seçilen kartlarda teal çerçeve ve onay ikonu gösterin.
  * Ekranın altına yapışık bir özet çubuğu (`Column` içinde toplam fiyat, toplam süre ve "Devam Et" butonu) yerleştirin.

- [x] **Adım 4: Yorum Yapma / Listeleme & Müşteri Profili Referans Kodu**
  
  * Tamamlanmış randevular için yıldız (1-5) ve yorum girme popup penceresi ekleyin.
  * Salon detay sayfasında yorumları sekmeyle listeleyin.
  * Müşteri profil sekmesine "Arkadaşını Davet Et" alanı ekleyin; davet linki ve referans kodu (`CUST_[TELEFON]`) kopyalama butonu yerleştirin. Davet kodu girme alanını müşteri giriş ekranına dahil edin.

- [x] **Adım 5: Commit yapın**
  
  `git add app/src/main/java/com/example/ui/CustomerScreens.kt`
  `git commit -m "ui: müşteri harita görünümü, hızlı hizmet seçimi, yorumlama ve referans kodu alanları eklendi"`

---

### Görev 5: Takvim Geliştirmeleri, Manuel Zaman Dilimi Seçicisi ve Davet Linki

**Dosyalar:**
* Güncelle: [BusinessScreens.kt](file:///c:/Users/AOSB/OneDrive%20-%20Adana%20Hac%C4%B1%20Sabanc%C4%B1%20Organize%20Sanayi%20B%C3%B6lgesi%20B%C3%B6lge%20M%C3%BCd%C3%BCrl%C3%BC%C4%9F%C3%BC/Masa%C3%BCst%C3%BC/hk/ku/app/src/main/java/com/example/ui/BusinessScreens.kt)

- [x] **Adım 1: Yatay 14 Günlük Takvim Şeridi ve DatePicker**
  
  * Takvim ekranının üstündeki gün oklarını kaldırıp yerine yatayda kayan 14 günlük kart şeridi yerleştirin.
  * Şeridin yanına ekleyeceğiniz takvim simgesiyle yerel `DatePickerDialog` bileşenini tetikleyin.

- [x] **Adım 2: Manuel Randevu Formunda Saat Dilimi Grid Yapısı**
  
  * "Telefonla Rezervasyon Al" formundaki saat yazma kutusunu kaldırın.
  * Seçilen gün ve personelin halihazırda dolu olan saatlerini hesaplayan ve dolu olanları grileşip tıklanamayan, boş olanları ise seçilebilen tıklanabilir **Zaman Slotları Grid (Kutucukları)** yapısını tasarlayın.

- [x] **Adım 3: Manuel Randevu Formu Doğrulama ve SMS/WhatsApp Davet Şablonu**
  
  * İsim, telefon, saat slotu seçilmeden "Takvime Ekle" butonunu pasifleştirin. Boş bırakılan zorunlu alanların altına kırmızı renkli hata yazıları ekleyin.
  * Randevu başarıyla oluşturulduğunda bir davet mesajı popup'ı gösterin: *"Merhaba [Müşteri], [Salon] randevunuz [Tarih] saat [Saat] için oluşturulmuştur. Kayıt olup indirim kazanmak için: `kuafor.app/kayit?ref=STAFF_[CALISAN_ID]`"*

- [x] **Adım 4: Commit yapın**
  
  `git add app/src/main/java/com/example/ui/BusinessScreens.kt`
  `git commit -m "ui: takvim şeridi, manuel randevu saat slotları ve doğrulama akışları eklendi"`

---

### Görev 6: Platform Admin Onay, Komisyon ve Reklam Yönetimi

**Dosyalar:**
* Güncelle: [AdminScreens.kt](file:///c:/Users/AOSB/OneDrive%20-%20Adana%20Hac%C4%B1%20Sabanc%C4%B1%20Organize%20Sanayi%20B%C3%B6lgesi%20B%C3%B6lge%20M%C3%BCd%C3%BCrl%C3%BC%C4%9F%C3%BC/Masa%C3%BCst%C3%BC/hk/ku/app/src/main/java/com/example/ui/AdminScreens.kt)

- [x] **Adım 1: Kayıt Başvurularını ve Aktif Salonları Sekmelerle Ayrıştırın**
  
  * Admin "Onaylar & İşyerleri" sekmesini "Başvurular (Onay Bekleyenler)", "Aktif Salonlar (Yayında)" ve "Kısıtlı Salonlar" olarak 3 alt sekmeye bölün.

- [x] **Adım 2: Salon Düzenleme ve Özel Komisyon Belirleme Penceresi**
  
  * Aktif Salonlar listesindeki her salona "Düzenle" butonu ekleyin.
  * Tıklandığında açılan popup penceresinde salon bilgilerini düzenleme alanı ve en önemlisi o salona özel **Komisyon Oranı (%)** değiştirme alanı yerleştirin. Kaydedildiğinde `viewModel.saveCustomCommissionRate` fonksiyonunu çağırın.

- [x] **Adım 3: Reklam Yönetim Arayüzü**
  
  * Admin paneline tamamen yeni **"Reklam Yönetimi"** sekmesi ekleyin.
  * Adminin reklam başlığı, reklam görseli (hazır şablonlardan seçilebilecek) ve bu reklamın tıklanıldığında yönlendirileceği hedef salonu belirleyebileceği bir arayüz geliştirin.

- [x] **Adım 4: Commit yapın**
  
  `git add app/src/main/java/com/example/ui/AdminScreens.kt`
  `git commit -m "ui: admin onay sekmeleri, özel komisyon ayarı ve reklam yönetim paneli eklendi"`

---

### Görev 7: Derleme, Doğrulama ve Testlerin Çalıştırılması

- [ ] **Adım 1: Uygulamayı Derleyin ve Hata Olmadığını Doğrulayın**
  
  `$env:JAVA_HOME = "C:\Users\AOSB\AppData\Local\Temp\jdk17\jdk-17.0.11+9"; & "$env:JAVA_HOME\bin\java.exe" -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain compileDebugKotlin`
  
  Herhangi bir derleme hatası olması durumunda kodları düzeltin.
  
  > ⚠️ Not (2026-07-21): Son başarılı APK derlemesi 2026-07-02/03 tarihli; en son kod commit'i (`3d30f27`, UI redesign) bundan sonra (2026-07-11) geldiği için mevcut derleme güncel kod ile doğrulanmamış durumda. Bu adım tekrar çalıştırılmalı.
  >
  > ⛔ **Bloke (2026-07-21):** Bu kasanın çalıştığı makinede (`G:\Drive'ım\HLLOBS\YZ`) JDK/Android SDK kurulu değil — sistem genelinde `java.exe` bulunamadı ve plandaki JDK yolu (`...Temp\jdk17\jdk-17.0.11+9`) artık mevcut değil. Derleme, Android Studio + JDK kurulu bir makineden (önceki oturumlarda kullanılan `C:\Users\AOSB\OneDrive - ...\Masaüstü\hk\ku` yolu) elle çalıştırılmalı.

- [ ] **Adım 2: Yeni APK Dosyasını Derleyin ve Kopyalayın**
  
  `$env:JAVA_HOME = "C:\Users\AOSB\AppData\Local\Temp\jdk17\jdk-17.0.11+9"; & "$env:JAVA_HOME\bin\java.exe" -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain assembleDebug`
  `Copy-Item -Path "app/build/outputs/apk/debug/app-debug.apk" -Destination "APK_DOWNLOAD/kuaforum_v1.0_debug.apk" -Force`

- [x] **Adım 3: Değişiklikleri Git Deposuna Push Edin**
  
  `git push origin master` (veya ilgili geliştirme dalı)
