# Kuaförüm — Android + iOS Çoklu Platform Genişlemesi (Tasarım)

> **Durum:** Onaylandı (Opus proje lideri incelemesi + Halil onayı, 2026-07-21)
> **Kapsam notu:** Bu doküman tüm programın mimari çerçevesini tanımlar. Tek bir uygulama planına sığmayacak kadar büyük olduğu için alt projelere bölünmüştür (aşağıya bakın). Her alt proje kendi `writing-plans` çıktısıyla uygulanır.

## Arka Plan ve Hedef

Kuaförüm şu an tek modüllü, saf Android bir uygulama (Kotlin, Jetpack Compose, Room DB, Supabase). Halil'in nihai hedefi: uygulamanın Android **ve** iOS'ta eş zamanlı, gerçekten çalışır ve güvenlik açığı olmayan halde her iki mağazaya (App Store + Play Store) yüklenip canlıya alınması — bu bir MRR (aylık gelir) iş modeli, "en hızlı şekilde" yapılması gerekiyor.

Kısıtlar:
- Geliştiricinin (Halil) Mac/Xcode erişimi yok.
- Bütçe hassas — ücretsiz/ucuz yollar tercih ediliyor.
- Apple Developer Program üyeliği henüz yok (yıllık $99, tarayıcıdan kayıt olunabilir, onay günler sürebilir).
- GitHub reposu (`Hxexe/kuafor`) **public** — bu, GitHub Actions'ın macOS runner'larını tamamen ücretsiz ve sınırsız dakika ile kullanabileceğimiz anlamına geliyor.

## Kod Tabanı Tespiti (Opus incelemesi, doğrulandı)

Android bağımlılığı beklenenden çok daha az:
- Ağ katmanı zaten %100 multiplatform-uyumlu: Supabase-kt 3.0.1 + Ktor 3.0.1.
- Retrofit, Moshi, OkHttp (doğrudan), CameraX, Play Services Location, Firebase-AI kodda **hiç kullanılmıyordu** — 2026-07-21'de temizlendi (`f947b1e`).
- Gerçek Android bağımlılığı: Room veri katmanı (~600 satır), `AndroidViewModel` + `SharedPreferences` (tek nokta, `AppViewModel.kt`), `MainActivity`'deki Intent/Uri kullanımı, birkaç `BackHandler`.
- UI'ın 8000+ satırı zaten saf Jetpack Compose.

**Sonuç:** Sıfırdan yeniden yazma reddedildi. Mevcut kod tabanı Compose Multiplatform (CMP) migrasyonu için düşük riskli, yüksek yeniden-kullanım potansiyeli sunuyor.

## Mimari Karar: Compose Multiplatform

- KMM + native SwiftUI (ayrı iOS UI yazımı) reddedildi: 8000 satırlık UI'ı SwiftUI'da tekrar yazmak "en hızlı" hedefiyle çelişir.
- Flutter / tam native rewrite reddedildi: mevcut Kotlin/Compose/Supabase yatırımını çöpe atar.
- **Seçilen yol:** Tek Kotlin kod tabanından Android + iOS üretmek üzere projeyi `:composeApp` (commonMain/androidMain/iosMain) + `iosApp/` (ince Xcode/SwiftUI wrapper) yapısına taşımak.

### Veri Katmanı: Sunucu-Otoriter Model (Kritik Değişiklik)

Mevcut mimaride veri çoğunlukla yerel Room'dan okunuyor, Supabase senkronu kısmi. Bu, RLS politikalarını anlamsız kılar — RLS ancak Supabase tek otoriter kaynaksa koruma sağlar.

**Zorunlu değişiklik:** Supabase, tüm hassas veri için tek otoriter kaynak olacak. Room yalnızca **çevrimdışı cache** rolüne indirgenecek (source of truth değil). Bu karar aynı zamanda Room'un iOS'ta (Room 2.7 KMP desteği hâlâ kırılgan) sorun çıkarması ihtimaline karşı geri dönüşü kolaylaştırır: cache katmanı gerekirse SQLDelight ile değiştirilebilir, iş mantığı etkilenmez.

### Güvenlik Mimarisi (Zorunlu Gereksinimler)

1. Her Supabase tablosunda RLS **default-deny** açık; politikalar role göre (müşteri yalnız kendi randevusu, işletme yalnız kendi salonu, admin işlemleri ayrı).
2. `service_role` anahtarı **asla mobil uygulamada veya tarayıcıda bulunmayacak** — yalnızca Vercel admin panelinin sunucu tarafında (API route/server action) kullanılacak.
3. Anon key'in mobil `BuildConfig`'te olması sorun değil (anon key zaten public olacak şekilde tasarlanmıştır); gerçek güvenlik kapısı RLS'dir.
4. Admin paneli müşteri/işletme mobil uygulamasından tamamen ayrı: Next.js + Supabase Auth, Vercel'de barındırılır. Mobil binary içinde admin kodu taşınmaz (App Store/Play Store inceleme riskini de azaltır).
5. Kritik yazma işlemleri (randevu, ödeme öncesi) için Postgres CHECK constraint'leri + gerekirse Supabase Edge Functions ile girdi doğrulama/rate limiting.
6. RLS policy SQL'lerini Sonnet üretir (kopyala-yapıştır), Halil Supabase dashboard'undan elle uygular (bu ortamdan Supabase dashboard erişimi yok).

### CI / Dağıtım Stratejisi

- **Şimdi (spike, 2a):** GitHub Actions `macos-14` runner — public repo olduğu için ücretsiz ve sınırsız. Kod imzalama gerektirmeyen iOS Simulator build'i doğrulamak için yeterli.
- **Mağaza aşamasında (ileride, ayrı alt proje):** Mac'siz kod imzalama gerçek sürtünme noktası. Codemagic'in ücretsiz kotası (aylık ~500 dk, otomatik provisioning) veya Fastlane match değerlendirilecek.
- **Kritik yol:** Apple Developer Program kaydı, spike'ı beklemeden **paralelde bugün başlatılmalı** (yalnızca Halil yapabilir — dış hesap/ödeme işlemi).

## Alt Proje Sıralaması ve Go/No-Go Kapıları

Program tek bir plana sığmayacak kadar büyük; her alt proje kendi `writing-plans` çıktısıyla uygulanacak:

| # | Alt Proje | Durum |
|---|---|---|
| 1 | Admin paneli güvenlik yaması (gerçek Supabase Auth girişi) | ✅ Tamamlandı (`ab9c536`) |
| G0 | Ölü bağımlılık temizliği + Apple Developer kaydı başlatma | ✅ Kod kısmı tamamlandı (`f947b1e`); Apple kaydı Halil'de |
| 2a | CMP + iOS fizibilite kanıtı (bu doküman, aşağıda detaylı) | 🔜 Sıradaki |
| 3 | Veri/iş mantığı katmanının `commonMain`'e taşınması (sunucu-otoriter model ile) | Beklemede |
| 4 | UI ekranlarının taşınması (Customer/Business/Staff) | Beklemede |
| 5 | Ayrı Vercel admin paneli (Next.js + Supabase Auth) | Beklemede |
| 6 | Mağaza yayını (App Store + Play Store, imzalı build, RLS pen-test) | Beklemede |

**Go/No-Go Kapıları (her biri kanıtla geçilir — iddiayla değil):**
- **G1:** Resmi KMP "Hello World" şablonu CI'da derlenip iOS Simulator'da açılıyor (screenshot artifact).
- **G2:** Gerçek bir ekran + **canlı Supabase Auth login + bir DB okuması** iOS Simulator'da çalışıyor (screenshot + log artifact). → Bu kapı geçilmeden alt proje 3-4'e geçilmez.
- **G3 (migrasyon sırasında):** Her tabloda RLS default-deny aktif; `service_role` anahtarının mobil/browser'da bulunmadığı doğrulanmış.
- **G4:** İmzalı `.ipa` CI'dan üretiliyor (Apple hesabı onaylandıktan sonra).

**Kill-criteria:** G2 aşamasında Room-iOS veya Supabase-kt iOS entegrasyonu 2-3 gün içinde çözülemezse, o katman için native expect/actual köprü veya doğrudan Supabase REST fallback'ine geçilir — ancak CMP mimarisi kararı iptal edilmez (entegrasyon detayı mimariyi devirmez).

## Alt Proje 2a: Fizibilite Kanıtı (Detaylı Kapsam)

**Amaç:** Apple Developer hesabı olmadan, ücretsiz CI ile, gerçek Compose Multiplatform kodunun iOS'ta çalıştığını kanıtlamak — büyük migrasyona (3-4) geçmeden önce riski ucuza doğrulamak.

**İzolasyon:** Ayrı `ios-spike` git branch'inde çalışılır; `main` (canlı Android uygulaması) etkilenmez.

**Proje yapısı:** Mevcut tek modüllü `:app`, standart KMP şablonuna göre bölünür:
- `:composeApp` — `commonMain`, `androidMain`, `iosMain` kaynak setleri
- `iosApp/` — ince bir Xcode/SwiftUI wrapper (Compose'u host eden birkaç satır Swift)

**CI (`.github/workflows/ios-spike.yml`):**
- `macos-14` runner, tetikleyici: `ios-spike` branch push + manuel (`workflow_dispatch`).
- Adımlar: JDK 17 kurulumu → Gradle ile iOS framework derleme → `xcodebuild ... -sdk iphonesimulator build` → simulator başlatıp uygulamayı kurup açma → ekran görüntüsü + log'u artifact olarak yükleme (imza gerekmez, App Store Connect/Apple hesabı gerekmez).

**Doğrulama adımları (G1 → G2):**
1. **G1 — Araç zinciri kanıtı:** Resmi KMP "Hello World" şablonu CI'da simulator'da açılıyor mu?
2. **G2 — Gerçek entegrasyon kanıtı:** Onboarding ekranı `commonMain`'e taşınır VE bu ekrandan tetiklenen gerçek bir Supabase Auth login + bir DB okuması simulator'da çalışır. (Statik ekran + screenshot yeterli değildir — CMP'nin zor kısmı entegrasyondur, statik render değil.)

**Başarı kriteri:** G2 artifact'leri (screenshot + log) Halil'e sunulur; onaylanırsa alt proje 3'e (veri katmanının taşınması) geçilir.

## Açık Riskler / Bilinmeyenler

- Room 2.7'nin KMP/iOS desteği olgunluk açısından belirsiz — G2'de birincil olarak denenecek, kill-criteria'da SQLDelight fallback tanımlı.
- Ktor motor değişimi: Android'de `ktor-client-okhttp` kullanılıyor, iOS'ta `ktor-client-darwin` gerekecek (alt proje 2a/3 kapsamında eklenecek).
- Apple Developer Program onay süresi dış bir bağımlılık; programın kritik yolunu belirliyor.
