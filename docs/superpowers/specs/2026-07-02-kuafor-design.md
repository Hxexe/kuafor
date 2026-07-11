# Kuaförüm Uygulaması Tasarım ve Özellik Geliştirme Spesifikasyonu

## 1. Yönetici Özeti
Bu belge, Kuaförüm uygulamasının kapsamlı bir şekilde güncellenmesine yönelik teknik tasarımı detaylandırmaktadır. Amaç; müşteriler, çalışanlar ve platform yöneticileri için premium, akıcı ve sürtünmesiz (frictionless) bir kullanıcı deneyimi (UX) sunmaktır. Bu kapsamda; onboarding ekranının görsel olarak yenilenmesi, telefon numarası tabanlı akıllı rol yönlendirmesi, kısıtlı çalışan paneli, çalışanlar arası liderlik tablosu, manuel randevu saat dilimi seçimi, çift taraflı müşteri referans sistemi, bölgesel harita görünümü ve genişletilmiş admin paneli ayarları tasarlanmıştır.

---

## 2. Giriş (Onboarding) Ekranı ve Telefon Tabanlı Giriş

### Giriş Ekranı Yenilenmesi
* **Görsel Tasarım:** Giriş ekranındaki mevcut dar 3 satırlı yapı kaldırılacak. Yerine yüksekliği `130.dp` olan, dikey sıralanmış **3 Büyük Rol Kartı** yerleştirilecek. Her kart şunları içerecektir:
  * Renkli ve dairesel arka plana sahip belirgin bir ikon (Müşteri için Teal, Çalışan/Sahip için Turuncu, Yönetici için Koyu Gri).
  * Kalın (Bold) başlık ve açıklayıcı alt metin.
  * Kartın tıklanabilir olduğunu vurgulayan sağa yönlü ok simgeleri.

### Telefon Numarası ile Giriş ve Rol Eşleştirmesi
Kullanıcı **Salon Sahibi & Çalışanı Girişi** kartını seçtiğinde telefon numarası giriş ekranına yönlendirilir.
* **Başlangıç Verileri ve Yönlendirme Kuralları:**
  * **`0555 999 8877`:** Aktif salonun **Salon Sahibi (İşletme Sahibi)** olarak eşleştirilir. Giriş yapıldığında tüm yetkilere sahip **Salon Yönetim Paneli** (ciro istatistikleri, tüm çalışan takvimleri, hizmet ayarları) açılır.
  * **`0555 111 2233`:** **Berber Kadir (Çalışan)** olarak eşleştirilir. Giriş yapıldığında sadece Kadir'e ait kısıtlı **Çalışan Randevu Paneli** açılır.
  * **`0555 333 4455`:** **Barber Samet (Çalışan)** olarak eşleştirilir. Giriş yapıldığında sadece Samet'e ait kısıtlı **Çalışan Randevu Paneli** açılır.
* **Giriş Yardımcıları:**
  * Giriş alanlarının altında bir **"Şifremi Unuttum"** bağlantısı yer alacak. Tıklandığında şifre sıfırlama talebinin simüle edildiği şık bir onay penceresi açılacak.
  * Tanımlanmamış veya geçersiz telefon numarası girildiğinde kırmızı renkli uyarı mesajı gösterilecek.

---

## 3. Müşteri Deneyimi Geliştirmeleri

### Bölgesel Harita ve Liste Görünümü
* Müşteri ana sayfasında **Liste Görünümü** ve **Harita Görünümü** sekmeleri bulunacak.
* **Harita Görünümü:**
  * Bölgeyi temsil eden şık ve simüle edilmiş bir harita grafiği (Canvas çizimi veya vektör görsel).
  * Harita üzerinde aktif salonların konumlarını gösteren pinler/işaretçiler.
  * Bir pin'e tıklandığında ekranın altından yukarı doğru açılan **Hızlı Ön İzleme Kartı** (Salon adı, puanı, mesafesi ve "Randevu Al" butonu). Bu karta tıklanarak doğrudan o salonun detayına gidilebilir.
* **Liste Görünümü:**
  * En üstte yatayda kaydırılabilen sponsorlu **"Öne Çıkan Salonlar"** şeridi.
  * Admin panelinden yönetilen aktif reklamların gösterildiği bir **Reklam Banner** alanı.
* **Konum Filtreleme:** Üst kısımda İlçe Seçici Dropdown (Beşiktaş, Kadıköy, Çankaya) ve kullanıcıya en yakın salonları mesafeye göre (örn. 800m) listeleyen "GPS / Konumumu Kullan" hızlı butonu.

### Sürtünmesiz Hizmet Seçimi
* Salon detay sayfasındaki hizmet listesinde yer alan kartlar **doğrudan tıklanabilir** olacak.
* Tıklandığında kartın etrafında teal renkli çerçeve (`BorderStroke(2.dp, Color(0xFF185C5C))`), hafif teal arka plan ve sağda bir onay işareti (Checkmark) belirecek. Çoklu seçim yapılabilecek.
* Ekranın en altında yapışkan (sticky) bir **"Seçilen Hizmetler Özet Çubuğu"** bulunacak. Toplam fiyat ve süre anlık güncellenerek kullanıcının aşağı kaydırmasına gerek kalmadan "Randevu Saatini Seç" butonuna basması sağlanacak.

### Değerlendirme ve Yorum Sistemi
* Müşteriler, tamamlanan randevuları için 1-5 arası yıldız puanı verebilecek ve yazılı yorum bırakabilecek.
* Gönderilen yorumlar veritabanına kaydedilecek ve salonun detay sayfasındaki **"Yorumlar & Değerlendirmeler"** sekmesinde listelenerek salonun ortalama puanını dinamik güncelleyecek.

### Çift Taraflı Müşteri Referans Programı
* Müşteriler kendi davet linklerini (`kuafor.app/kayit?ref=CUST_[TELEFON]`) paylaşabilecek.
* Yeni bir müşteri bu link ile üye olduğunda **çift taraflı ödül** tetiklenecek:
  * Davet eden müşteriye **%15 indirim kuponu** tanımlanacak.
  * Davet edilen yeni üyeye ilk rezervasyonunda kullanmak üzere **%10 hoş geldin kuponu** tanımlanacak.

---

## 4. İşletme ve Çalışan (Personel) Yönetim Panelleri

### İşletme Sahibi Görünümü (Tam Yetki)
* **Randevu Odaklı Ana Ekran:** Ana ekran doğrudan **"Bugünün Randevuları"** listesini gösterecek. Tüm çalışanların randevuları listelenecek ve Onayla, Reddet, Tamamlandı, Gitmedi butonları yer alacak.
* **Raporların Taşınması:** Grafiklerin ve ciro analizlerinin yer aldığı dashboard ekranı, alt menüde **"Raporlar & Analiz"** adında ayrı bir sekmeye taşınacak.

### Kısıtlı Çalışan Paneli (Personel Görünümü)
* Çalışanlar kendi telefon numaralarıyla girdiğinde sadece **kendi randevularını** görebilecek.
* Alt menüde sadece **"Takvim (Kendi Randevuları)"** ve **"Profilim"** sekmelerine erişebilecekler. Finansal analizleri veya diğer çalışanların listesini göremeyecekler.
* **Salon İçi Liderlik Tablosu (Leaderboard):**
  * Profil sekmesinde tüm çalışanların performanslarının listelendiği lig tablosu yer alacak.
  * Sıralama kriteri: **1. Sisteme kazandırılan üye sayısı**, ve **2. Müşterilerden alınan ortalama puan**.
  * Kazanılan bröveler/rozetler (örn: "Bronz Üye Kazandırıcı", "Altın Berber") isimlerin yanında sergilenecek.

### 14 Günlük Takvim Şeridi ve Tarih Seçici
* Takvim ekranının üstünde yatayda kaydırılabilen **14 Günlük Kart Şeridi** (gün adı ve numarası ile, örn: "Sal 2") yer alacak. Kartlara tıklandığında randevular anında filtrelenecek.
* Şeridin yanındaki takvim simgesiyle yerleşik **DatePicker** açılacak ve 14 günün dışındaki ileri tarihler de kolayca seçilebilecek.

---

## 5. Manuel Randevu Oluşturma (Walk-in)

* **Saat Dilimi Seçimi:** Saatlerin elle yazılması yerine **Zaman Dilimi Grid yapısı** (30 dakikalık aralıklarla: 09:00, 09:30 ... 19:30) kullanılacak.
  * Seçilen gün ve çalışanın dolu olan saatleri gridde **pasif/gri** olarak görünecek ve çift randevu engellenecek.
* **Form Doğrulama:** İsim, telefon, en az bir hizmet ve saat seçimi yapılmadan "Takvime Ekle" butonu aktif olmayacak ve eksik alanların altında kırmızı uyarılar çıkacak.
* **Müşteri Kayıt ve Davet Bağlantısı:** Manuel randevu eklendiğinde müşteriye şu SMS/WhatsApp şablonu simüle edilecek:
  * *"Merhaba [Müşteri Adı], [Salon Adı] salonundaki randevunuz [Tarih] [Saat] için oluşturulmuştur. Kayıt olup indirim kazanmak için tıklayın: `kuafor.app/kayit?ref=STAFF_[CALISAN_ID]`"*
  * Müşteri bu referans ile üye olduğunda ilk randevusu için **%10 hoş geldin kuponu** kazanacak, randevuyu giren çalışana ise liderlik tablosu için **+1 üye puanı** eklenecek.

---

## 6. Platform Yöneticisi (Admin) Ayarları

### Kayıt Başvuruları ve Aktif Salonlar Sekmeleri
* "Onaylar & İşyerleri" tabı 3 alt sekmeye ayrılacak:
  1. **Başvurular (Bekleyenler):** Onay bekleyen yeni salon başvuruları listelenecek ve yanlarında "Onayla & Yayınla" butonu yer alacak.
  2. **Aktif Salonlar:** Yayındaki salonlar listelenecek ve yanlarında "Yayından Çek" ile "Düzenle" butonları olacak.
  3. **Kısıtlı Salonlar:** Askıya alınan salonlar listelenecek ve yeniden yayına alma seçeneği olacak.
* **İşyeri Düzenleme Penceresi (Edit Popup):**
  * Salon adı, adresi ve kategorisi güncellenebilecek.
  * Her salon için özel **"Komisyon Oranı (%)"** (örn: %5, %8) belirlenebilecek ve bu oran o salonun tamamlanan randevularında geçerli olacak.

### Reklam Yönetimi (Ad Management)
* Admin panelinde yeni **"Reklam Yönetimi"** sekmesi eklenecek.
* Admin; Reklam Başlığı, Reklam Görseli (hazır şablonlar veya URL), Reklam Durumu (Aktif/Pasif) ve **"Yönlendirilecek Salon"** seçerek yeni reklamlar tanımlayabilecek.
* Müşteri ana sayfasında bu reklamlar görünecek ve tıklandığında ilgili salona yönlendirecek.

---

## 7. Veri Modeli Değişiklikleri

### `StaffEntity` (Çalışan Tablosu)
* `phone: String` (Giriş için telefon alanı - Kadir ve Samet için varsayılan numaralar atanacak).
* `referralCount: Int` (Kazandırılan üye sayısı - Liderlik tablosu için).

### `AdEntity` (Reklam Tablosu - Yeni)
* `id: Int` (Birincil Anahtar)
* `title: String` (Reklam Başlığı)
* `imageUrl: String` (Görsel Adresi)
* `targetSalonId: Int?` (Yönlendirilecek Salon ID)
* `isActive: Boolean` (Aktiflik Durumu)

---

## 8. Doğrulama ve Test Planı
* **Robolectric Testleri:**
  1. Telefon numarasına göre rol bazlı yönlendirmenin (sahip vs. çalışan) doğruluğu.
  2. Hizmet kartları seçildiğinde sepet toplamlarının doğru güncellenmesi.
  3. Referans linkiyle üye olunduğunda hem davet edene hem edilene kupon tanımlanması.
  4. Dolu saat dilimlerinin manuel randevu gridinde pasifleşmesi.
* **Manuel Doğrulama:** Uygulama cihaz üzerinde derlenip tüm senaryolar uçtan uca test edilecek.
