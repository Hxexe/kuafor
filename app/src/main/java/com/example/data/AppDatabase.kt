package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SalonEntity::class,
        ServiceEntity::class,
        StaffEntity::class,
        AppointmentEntity::class,
        CouponEntity::class,
        AdEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kuaforapp_database_v2"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDatabase(dao: AppDao) {
            // 1. Seed Salons
            val salons = listOf(
                SalonEntity(
                    id = 1,
                    name = "Makas & Tarak Erkek Berberi",
                    category = "ERKEK",
                    address = "Yeni Levent Cad. Çilekli Sok. No:12, Beşiktaş, İstanbul",
                    imageUrl = "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?auto=format&fit=crop&w=500&q=80",
                    rating = 4.9f,
                    reviewCount = 142,
                    workingHours = "09:00 - 21:00"
                ),
                SalonEntity(
                    id = 2,
                    name = "Süperstar Kadın Kuaförü & Güzellik",
                    category = "KADIN",
                    address = "Bağdat Cad. Selamiçeşme Sok. No:45, Kadıköy, İstanbul",
                    imageUrl = "https://images.unsplash.com/photo-1560066984-138dadb4c035?auto=format&fit=crop&w=500&q=80",
                    rating = 4.8f,
                    reviewCount = 215,
                    workingHours = "08:30 - 20:00"
                ),
                SalonEntity(
                    id = 3,
                    name = "Unisex Elegance Studio & Spa",
                    category = "UNISEX",
                    address = "Kızılırmak Mah. Ufuk Üniversitesi Cad. No:8, Çankaya, Ankara",
                    imageUrl = "https://images.unsplash.com/photo-1621605815971-fbc98d665033?auto=format&fit=crop&w=500&q=80",
                    rating = 4.7f,
                    reviewCount = 98,
                    workingHours = "10:00 - 22:00"
                ),
                SalonEntity(
                    id = 4,
                    name = "Klasik Makas Berber Ahmet",
                    category = "ERKEK",
                    address = "Karşıyaka Yalı Cad. No:110, Karşıyaka, İzmir",
                    imageUrl = "https://images.unsplash.com/photo-1605497746444-ac9dbd324ce8?auto=format&fit=crop&w=500&q=80",
                    rating = 4.6f,
                    reviewCount = 64,
                    workingHours = "08:00 - 20:00"
                )
            )
            dao.insertSalons(salons)

            // 2. Seed Services
            val services = listOf(
                // Salon 1 (Makas & Tarak)
                ServiceEntity(id = 1, salonId = 1, name = "Premium Saç Kesimi", price = 300.0, durationMinutes = 35, category = "SACH_KESIMI", description = "Model kesim, yıkama ve stil fönü"),
                ServiceEntity(id = 2, salonId = 1, name = "Köpüklü Sakal Tıraşı", price = 150.0, durationMinutes = 20, category = "TRAS", description = "Sıcak havlu kompresi, köpüklü jilet tıraşı ve balsam nemlendirici"),
                ServiceEntity(id = 3, salonId = 1, name = "Saç & Sakal Kombin", price = 400.0, durationMinutes = 50, category = "SACH_KESIMI", description = "Yıkama, fön, model kesim ve sakal tasarımı bir arada"),
                ServiceEntity(id = 4, salonId = 1, name = "Arındırıcı Cilt Maskesi", price = 120.0, durationMinutes = 15, category = "BAKIM", description = "Gözenek sıkılaştırıcı siyah kil maskesi ve buhar"),

                // Salon 2 (Süperstar Kadın)
                ServiceEntity(id = 5, salonId = 2, name = "Saç Kesimi & Yıkama", price = 450.0, durationMinutes = 45, category = "SACH_KESIMI", description = "Yıkama, saç yapısına uygun saç kesimi, fön dahil"),
                ServiceEntity(id = 6, salonId = 2, name = "Kırık Fön & Saç Şekillendirme", price = 200.0, durationMinutes = 25, category = "BAKIM", description = "Yıkama sonrası düz veya dalgalı pratik fön"),
                ServiceEntity(id = 7, salonId = 2, name = "Komple Boya & Işıltı", price = 1500.0, durationMinutes = 120, category = "BOYAMA", description = "L'Oreal veya Olaplex profesyonel organik saç boyama"),
                ServiceEntity(id = 8, salonId = 2, name = "Manikür & Pedikür", price = 300.0, durationMinutes = 45, category = "BAKIM", description = "Steril aletlerle tırnak eti temizliği, el/ayak masajı ve oje"),

                // Salon 3 (Unisex Elegance)
                ServiceEntity(id = 9, salonId = 3, name = "Unisex Saç Tasarımı", price = 400.0, durationMinutes = 40, category = "SACH_KESIMI", description = "Erkek veya kadın stil kesimi ve bakım şampuanı"),
                ServiceEntity(id = 10, salonId = 3, name = "Keratin Botoks Bakımı", price = 900.0, durationMinutes = 90, category = "BAKIM", description = "Yıpranmış telleri onaran saç dolgusu ve keratin yüklemesi"),
                ServiceEntity(id = 11, salonId = 3, name = "Gelin Başat & Profesyonel Makyaj", price = 4000.0, durationMinutes = 180, category = "BOYAMA", description = "Özel porselen gelin makyajı ve provasıyla gelin saç topuzu")
            )
            dao.insertServices(services)

            // 3. Seed Staff
            val staff = listOf(
                // Salon 1
                StaffEntity(id = 1, salonId = 1, name = "Berber Kadir", role = "Usta Saç Tasarımcısı", imageUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=150&q=80", rating = 4.9f, phone = "05551112233", referralCount = 3),
                StaffEntity(id = 2, salonId = 1, name = "Barber Samet", role = "Sakal & Cilt Uzmanı", imageUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=150&q=80", rating = 4.8f, phone = "05553334455", referralCount = 1),

                // Salon 2
                StaffEntity(id = 3, salonId = 2, name = "Melis Kaya", role = "Baş Saç Tasarımcısı", imageUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80", rating = 4.9f),
                StaffEntity(id = 4, salonId = 2, name = "Seda Ünal", role = "Renklendirme & Boya Uzmanı", imageUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&q=80", rating = 4.8f),
                StaffEntity(id = 5, salonId = 2, name = "Buse Arı", role = "Nail Art / Makyajör", imageUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80", rating = 4.7f),

                // Salon 3
                StaffEntity(id = 6, salonId = 3, name = "Uğur Polat", role = "Kreatif Direktör", imageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80", rating = 4.9f),
                StaffEntity(id = 7, salonId = 3, name = "Aslı Demir", role = "Uzman Estetisyen", imageUrl = "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=150&q=80", rating = 4.8f)
            )
            dao.insertStaffList(staff)

            // 4. Seed Appointments (to populate historical charts and show past ratings)
            val baseTime = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            val appointments = listOf(
                // Salon 1 Completed Appointments
                AppointmentEntity(
                    id = 1,
                    salonId = 1,
                    salonName = "Makas & Tarak Erkek Berberi",
                    serviceIds = "1",
                    serviceNames = "Premium Saç Kesimi",
                    staffId = 1,
                    staffName = "Berber Kadir",
                    date = "03.06.2026",
                    time = "10:30",
                    totalPrice = 300.0,
                    totalDuration = 35,
                    customerName = "Mehmet Can",
                    customerPhone = "05551112233",
                    note = "Kısa yanlar lütfen",
                    status = "COMPLETED",
                    timestamp = baseTime - 1 * dayMs,
                    isRated = true,
                    ratingStars = 5,
                    ratingComment = "Kadir Bey ellerinize sağlık, saç kesimi kusursuz oldu!"
                ),
                AppointmentEntity(
                    id = 2,
                    salonId = 1,
                    salonName = "Makas & Tarak Erkek Berberi",
                    serviceIds = "2",
                    serviceNames = "Köpüklü Sakal Tıraşı",
                    staffId = 2,
                    staffName = "Barber Samet",
                    date = "02.06.2026",
                    time = "14:00",
                    totalPrice = 150.0,
                    totalDuration = 20,
                    customerName = "Ahmet Yılmaz",
                    customerPhone = "05321112233",
                    status = "COMPLETED",
                    timestamp = baseTime - 2 * dayMs,
                    isRated = true,
                    ratingStars = 4,
                    ratingComment = "Sıcak havlu bakımı harikaydı, çok dinlendirici."
                ),
                AppointmentEntity(
                    id = 3,
                    salonId = 1,
                    salonName = "Makas & Tarak Erkek Berberi",
                    serviceIds = "3",
                    serviceNames = "Saç & Sakal Kombin",
                    staffId = 1,
                    staffName = "Berber Kadir",
                    date = "01.06.2026",
                    time = "17:30",
                    totalPrice = 400.0,
                    totalDuration = 50,
                    customerName = "Caner Şenyurt",
                    customerPhone = "05051112233",
                    status = "COMPLETED",
                    timestamp = baseTime - 3 * dayMs,
                    isRated = true,
                    ratingStars = 5,
                    ratingComment = "Hem saç hem sakal tasarımı tam istediğim gibi."
                ),
                AppointmentEntity(
                    id = 4,
                    salonId = 1,
                    salonName = "Makas & Tarak Erkek Berberi",
                    serviceIds = "1",
                    serviceNames = "Premium Saç Kesimi",
                    staffId = 1,
                    staffName = "Berber Kadir",
                    date = "31.05.2026",
                    time = "11:00",
                    totalPrice = 300.0,
                    totalDuration = 35,
                    customerName = "Gökhan Bilge",
                    customerPhone = "05441112233",
                    status = "NO_SHOW",
                    timestamp = baseTime - 4 * dayMs
                ),

                // Salon 2 Completed Appointments
                AppointmentEntity(
                    id = 5,
                    salonId = 2,
                    salonName = "Süperstar Kadın Kuaförü & Güzellik",
                    serviceIds = "7",
                    serviceNames = "Komple Boya & Işıltı",
                    staffId = 4,
                    staffName = "Seda Ünal",
                    date = "03.06.2026",
                    time = "11:00",
                    totalPrice = 1500.0,
                    totalDuration = 120,
                    customerName = "Zeynep Güler",
                    customerPhone = "05351112233",
                    status = "COMPLETED",
                    timestamp = baseTime - 1 * dayMs,
                    isRated = true,
                    ratingStars = 5,
                    ratingComment = "Saçımın rengini tam tutturdu Seda Hanım, çok sevindim!"
                ),
                AppointmentEntity(
                    id = 6,
                    salonId = 2,
                    salonName = "Süperstar Kadın Kuaförü & Güzellik",
                    serviceIds = "5,8",
                    serviceNames = "Saç Kesimi & Yıkama, Manikür & Pedikür",
                    staffId = 3,
                    staffName = "Melis Kaya",
                    date = "02.06.2026",
                    time = "15:30",
                    totalPrice = 750.0,
                    totalDuration = 90,
                    customerName = "Burcu Tunç",
                    customerPhone = "05361112233",
                    status = "COMPLETED",
                    timestamp = baseTime - 2 * dayMs,
                    isRated = true,
                    ratingStars = 5,
                    ratingComment = "Melis Hanım kesimde harikalar yaratıyor, ek olarak manikür de kusursuzdu."
                )
            )
            for (booking in appointments) {
                dao.insertAppointment(booking)
            }

            // 5. Seed Coupons
            dao.insertCoupon(CouponEntity(id = 1, salonId = 1, code = "MAKAS10", discountType = "PERCENT", value = 10.0, minAmount = 200.0))
            dao.insertCoupon(CouponEntity(id = 2, salonId = 2, code = "SARI20", discountType = "PERCENT", value = 20.0, minAmount = 500.0))
            dao.insertCoupon(CouponEntity(id = 3, salonId = 1, code = "FIRSAT50", discountType = "FIXED", value = 50.0, minAmount = 150.0))
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.appDao())
                }
            }
        }
    }
}
