package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Salons
    @Query("SELECT COUNT(*) FROM salons")
    suspend fun getSalonsCount(): Int

    @Query("SELECT * FROM salons WHERE isApproved = 1")
    fun getAllSalons(): Flow<List<SalonEntity>>

    @Query("SELECT * FROM salons")
    fun getAdminSalons(): Flow<List<SalonEntity>>

    @Query("SELECT * FROM salons WHERE id = :salonId")
    suspend fun getSalonById(salonId: Int): SalonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalon(salon: SalonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalons(salons: List<SalonEntity>)

    @Update
    suspend fun updateSalon(salon: SalonEntity)

    @Query("UPDATE salons SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Int, isFav: Boolean)

    // Services
    @Query("SELECT * FROM services WHERE salonId = :salonId")
    fun getServicesBySalon(salonId: Int): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE salonId = :salonId")
    suspend fun getServicesBySalonSync(salonId: Int): List<ServiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Query("DELETE FROM services WHERE id = :serviceId")
    suspend fun deleteService(serviceId: Int)

    // Staff
    @Query("SELECT * FROM staff WHERE salonId = :salonId")
    fun getStaffBySalon(salonId: Int): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE salonId = :salonId")
    suspend fun getStaffBySalonSync(salonId: Int): List<StaffEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaffList(staffList: List<StaffEntity>)

    @Query("DELETE FROM staff WHERE id = :staffId")
    suspend fun deleteStaff(staffId: Int)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY timestamp DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE salonId = :salonId ORDER BY timestamp DESC")
    fun getAppointmentsForSalon(salonId: Int): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE customerPhone = :phone ORDER BY timestamp DESC")
    fun getAppointmentsForCustomer(phone: String): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Query("UPDATE appointments SET status = :status WHERE id = :appointmentId")
    suspend fun updateAppointmentStatus(appointmentId: Int, status: String)

    @Query("UPDATE appointments SET isRated = 1, ratingStars = :stars, ratingComment = :comment WHERE id = :appointmentId")
    suspend fun rateAppointment(appointmentId: Int, stars: Int, comment: String)

    // Coupons
    @Query("SELECT * FROM coupons WHERE salonId = :salonId")
    fun getCouponsForSalon(salonId: Int): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Query("DELETE FROM coupons WHERE id = :couponId")
    suspend fun deleteCoupon(couponId: Int)

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

    @Delete
    suspend fun deleteSalon(salon: SalonEntity)


    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Int): AppointmentEntity?

    @Query("SELECT * FROM appointments WHERE salonId = :salonId")
    suspend fun getAppointmentsForSalonSync(salonId: Int): List<AppointmentEntity>
}
