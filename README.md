# Kuaförüm - Yerel Geliştirme Kılavuzu (Local Development Guide)

Bu kılavuz, **Kuaförüm** (Kuaför & Berber Online Randevu Platformu) projesini kendi bilgisayarınızda (yerel ortamınızda) açıp çalıştırmanız ve geliştirmeye devam etmeniz için gereken tüm adımları ve bilgileri içermektedir.

Proje, modern Android standartları (Kotlin, Jetpack Compose, Room Database ve MVVM Mimarisi) ile geliştirilmiştir.

---

## 🛠️ Ön Gereksinimler

Kendi bilgisayarınızda projeyi çalıştırabilmek için aşağıdaki yazılımların yüklü olması gerekir:

1. **Android Studio**: En son kararlı sürümü (Ladybug veya üstü tavsiye edilir) indirip kurun. [Android Studio İndir](https://developer.android.com/studio)
2. **Java Development Kit (JDK)**: Proje **JDK 17** veya **JDK 21** sürümünü gerektirir. Android Studio kurulduğunda içerisinde uyumlu bir JDK sürümü paket olarak gelir ve otomatik olarak seçilir.
3. **Android Cihaz veya Emülatör**: uygulamayı test etmek için:
   - Gerçek bir Android telefon (Geliştirici Seçenekleri ve USB Hata Ayıklama açık durumda usb ile bağlı).
   - Veya Android Studio içindeki **Device Manager** aracılığıyla oluşturulmuş bir Sanal Cihaz (Avd Emulator).

---

## 📂 Projeyi Bilgisayara İndirme ve Açma Adımları

### Adım 1: Projeyi Bilgisayarınıza İndirin
Google AI Studio arayüzündeki sağ üst panelden veya ayarlar menüsünden **Export** (Dışa Aktar) seçeneğini kullanarak projeyi bir **ZIP dosyası** olarak bilgisayarınıza indirin ve boş bir klasöre çıkartın.

### Adım 2: Android Studio ile Projeyi Açın
1. **Android Studio** uygulamasını başlatın.
2. Hoş geldiniz ekranında **"Open"** (veya üst menüden **File > Open...**) seçeneğini tıklayın.
3. Projeyi bilgisayarınızda çıkarttığınız ana dizini seçin (içinde `settings.gradle.kts` ve `build.gradle.kts` dosyalarının bulunduğu kök klasör) ve **OK / Open** butonuna basın.
4. Android Studio projeyi tanıyacak ve Gradle yapılandırmasını kurmaya başlayacaktır.

### Adım 3: Gradle Senkronizasyonunun Tamamlanmasını Bekleyin
- Sağ alttaki durum çubuğunda "Gradle syncing..." veya "Importing..." yazısını göreceksiniz.
- İlk açılışta gerekli tüm Android kütüphaneleri ve Gradle bağımlılıkları internetten otomatik olarak indirilir. Bu işlem internet hızınıza bağlı olarak birkaç dakika sürebilir.
- Sol tarafta **Project** görünümünde `app` modülünü gördükten ve senkronizasyon hatasız bittikten sonra projeniz hazır demektir.

---

## 🚀 Uygulamayı Çalıştırma

1. Bilgisayarınıza USB kablosuyla bağladığınız Android telefonunuzu veya sanal emülatörünüzü başlatın.
2. Android Studio'nun üst araç çubuğundaki cihaz seçim listesinde cihazınızın seçili olduğundan emin olun.
3. Hemen yanındaki yeşil **"Run 'app'"** (Oynat / Play ▷) butonuna basın (ya da klavyeden `Shift + F10` tuşlarına basın).
4. Uygulama derlenecek ve cihazınızda otomatik olarak kurulup açılacaktır.

---

## 📦 Yerel Terminalden APK Çıktısı Alma

Projede otomatik yapılandırılmış yerel Gradle sarmalayıcısı (wrapper) bulunmaktadır. Android Studio terminalini kullanarak veya komut satırından doğrudan aşağıdaki komutlarla APK dosyaları üretebilirsiniz:

- **Hata Ayıklama (Debug) APK'sı oluşturmak için:**
  ```bash
  ./gradlew assembleDebug
  ```
  *(Windows için: `gradlew.bat assembleDebug`)*

- **İmzalı/Release APK'sı oluşturmak için:**
  ```bash
  ./gradlew assembleRelease
  ```
  *(Windows için: `gradlew.bat assembleRelease`)*

- **Üretilen APK dosyalarının konumu:**
  `app/build/outputs/apk/` dizini altında yer alır.

---

## 📁 Proje Dosya Yapısı ve Kod Düzenleme

Kod kalitesini korumak ve geliştirmeye devam etmek için önemli dosyaların yerleri aşağıda listelenmiştir:

* **Ana Giriş Noktası & Ekran Yönlendirmeleri:**
  * `app/src/main/java/com/example/MainActivity.kt`: Uygulamanın başlangıç noktasıdır. Ekranlar arası geçişler (Routing) ve rol tabanlı giriş kontrolleri burada yer alır.
* **Kullanıcı Arayüzü Ekranları (UI):**
  * `app/src/main/java/com/example/ui/CustomerScreens.kt`: Müşterilere yönelik salon arama, randevu alma sihirbazı (Booking Wizard), kuaför detayları, değerlendirme sistemi ve kişiye özel kupon & cüzdan sayfaları.
  * `app/src/main/java/com/example/ui/BusinessScreens.kt`: Kuaför salonu ve işletme sahipleri için randevu yönetim paneli, kazanç grafikleri, hizmet listesi ekleme/çıkarma, çalışma saatleri düzenleme sayfaları.
  * `app/src/main/java/com/example/ui/AdminScreens.kt`: Platform yöneticisi (Albay/Yönetici) ekranı. Kuaför onaylama, ciro/komisyon ayarları ve yedekleme sistemleri buradadır.
* **Merkezi Veri Yönetimi / State (MVVM):**
  * `app/src/main/java/com/example/ui/AppViewModel.kt`: Uygulamanın tüm iş mantığını (Business Logic) yöneten merkezi ViewModel. Veri akışları (Flows), cüzdan harcamaları, randevu güncellemeleri, bildirim tetiklemeleri ve cihaz hafızasında kalıcı kılınan ayarlar (`SharedPreferences` senkronizasyonu) buradadır.
* **Veritabanı & Yerel Depolama (Room DB):**
  * `app/src/main/java/com/example/data/Entities.kt`: Salon, Randevu, Çalışan, İnceleme, Kupon gibi tüm veritabanı tablolarının modelleridir.
  * `app/src/main/java/com/example/data/AppDao.kt`: SQLite sorgularını ve veritabanı işlem fonksiyonlarını barındırır.
  * `app/src/main/java/com/example/data/AppDatabase.kt`: İlk kurulumda veritabanını örnek verilerle dolduran (seed database) güvenli mekanizmayı yönetir.

---

## ✨ Bilmeniz Gereken Önemli Geliştirici İpuçları

1. **Çevrimdışı Bellek & Kalıcılık (Offline Cache):** Uygulamadaki son kaldığınız ekran, müşteri isim ve telefon bilgileri, komisyon oranları vb. yerel cihazın `SharedPreferences` belleğinde otomatik olarak saklanır. Uygulamayı tamamen kapatıp açsanız dahi kaldığınız yerden devam edebilirsiniz.
2. **Kütüphane Yönetimi (Version Catalog):** Projedeki kütüphaneler modern `gradle/libs.versions.toml` dosyası üzerinden yönetilmektedir. Bir kütüphane güncellemek isterseniz bu dosyaya bakabilirsiniz.
3. **Sorun Giderme:** Eğer Android Studio'da kırmızı hata çizgileri görürseniz üst menüden **Build > Clean Project** yapıp ardından **Build > Rebuild Project** seçeneğini kullanın. Bu işlem önbelleği temizleyip kodlarınızı sıfırdan sorunsuzca bağlayacaktır.

Bol şans ve keyifli geliştirmeler! Projeniz yerel bilgisayarınızda çalışmak için tamamen hazırdır! 🚀
