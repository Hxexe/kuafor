package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDao) {

    suspend fun getSalonsCount(): Int {
        return dao.getSalonsCount()
    }

    // Salons
    val allApprovedSalons: Flow<List<SalonEntity>> = dao.getAllSalons()
    val allAdminSalons: Flow<List<SalonEntity>> = dao.getAdminSalons()

    suspend fun getSalonById(salonId: Int): SalonEntity? {
        return dao.getSalonById(salonId)
    }

    suspend fun insertSalon(salon: SalonEntity): Long {
        return dao.insertSalon(salon)
    }

    suspend fun updateSalon(salon: SalonEntity) {
        dao.updateSalon(salon)
    }

    suspend fun toggleFavorite(salonId: Int, isFav: Boolean) {
        dao.toggleFavorite(salonId, isFav)
    }

    // Services
    fun getServicesBySalon(salonId: Int): Flow<List<ServiceEntity>> {
        return dao.getServicesBySalon(salonId)
    }

    suspend fun getServicesBySalonSync(salonId: Int): List<ServiceEntity> {
        return dao.getServicesBySalonSync(salonId)
    }

    suspend fun insertService(service: ServiceEntity) {
        dao.insertService(service)
    }

    suspend fun deleteService(serviceId: Int) {
        dao.deleteService(serviceId)
    }

    // Staff
    fun getStaffBySalon(salonId: Int): Flow<List<StaffEntity>> {
        return dao.getStaffBySalon(salonId)
    }

    suspend fun getStaffBySalonSync(salonId: Int): List<StaffEntity> {
        return dao.getStaffBySalonSync(salonId)
    }

    suspend fun insertStaff(staff: StaffEntity) {
        dao.insertStaff(staff)
    }

    suspend fun deleteStaff(staffId: Int) {
        dao.deleteStaff(staffId)
    }

    // Appointments
    val allAppointments: Flow<List<AppointmentEntity>> = dao.getAllAppointments()

    fun getAppointmentsForSalon(salonId: Int): Flow<List<AppointmentEntity>> {
        return dao.getAppointmentsForSalon(salonId)
    }

    fun getAppointmentsForCustomer(phone: String): Flow<List<AppointmentEntity>> {
        return dao.getAppointmentsForCustomer(phone)
    }

    suspend fun insertAppointment(appointment: AppointmentEntity): Long {
        return dao.insertAppointment(appointment)
    }

    suspend fun updateAppointmentStatus(appointmentId: Int, status: String) {
        dao.updateAppointmentStatus(appointmentId, status)
    }

    suspend fun rateAppointment(appointmentId: Int, stars: Int, comment: String) {
        dao.rateAppointment(appointmentId, stars, comment)
    }

    // Coupons
    fun getCouponsForSalon(salonId: Int): Flow<List<CouponEntity>> {
        return dao.getCouponsForSalon(salonId)
    }

    suspend fun getCouponByCode(code: String): CouponEntity? {
        return dao.getCouponByCode(code)
    }

    suspend fun insertCoupon(coupon: CouponEntity) {
        dao.insertCoupon(coupon)
    }

    suspend fun deleteCoupon(couponId: Int) {
        dao.deleteCoupon(couponId)
    }

    suspend fun getStaffByPhone(phone: String): StaffEntity? {
        return dao.getStaffByPhone(phone)
    }

    suspend fun incrementStaffReferral(staffId: Int) {
        dao.incrementStaffReferral(staffId)
    }

    fun getAllAdsFlow(): Flow<List<AdEntity>> {
        return dao.getAllAdsFlow()
    }

    suspend fun insertAd(ad: AdEntity) {
        dao.insertAd(ad)
    }

    suspend fun deleteAd(ad: AdEntity) {
        dao.deleteAd(ad)
    }

    suspend fun deleteSalon(salon: SalonEntity) {
        dao.deleteSalon(salon)
    }


    suspend fun getAppointmentById(id: Int): AppointmentEntity? {
        return dao.getAppointmentById(id)
    }

    suspend fun getAppointmentsForSalonSync(salonId: Int): List<AppointmentEntity> {
        return dao.getAppointmentsForSalonSync(salonId)
    }
}
