package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salons")
data class SalonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "KADIN", "ERKEK", "UNISEX"
    val address: String,
    val imageUrl: String,
    val rating: Float,
    val reviewCount: Int,
    val workingHours: String, // e.g., "09:00 - 21:00"
    val isApproved: Boolean = true,
    val isFavorite: Boolean = false,
    val maxGroupSize: Int = 1,
    val status: String = "APPROVED", // "PENDING", "APPROVED", "SUSPENDED"
    val ownerName: String = "Platform Ortağı",
    val ownerPhone: String = "05559998877",
    val commissionRate: Double = 10.0
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val salonId: Int,
    val name: String,
    val price: Double,
    val durationMinutes: Int,
    val category: String, // "SACH_KESIMI", "BOYAMA", "TRAS", "BAKIM", "DIGER"
    val description: String = ""
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val salonId: Int,
    val name: String,
    val role: String, // e.g., "Uzman Berber", "Usta Saç Stilisti"
    val imageUrl: String,
    val rating: Float = 4.8f,
    val workingHours: String = "09:00 - 19:00",
    val offDays: String = "Pazar", // comma separated off days
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


@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val salonId: Int,
    val salonName: String,
    val serviceIds: String, // comma-separated service IDs
    val serviceNames: String, // comma-separated service names
    val staffId: Int?, // null for "Fark Etmez"
    val staffName: String, // "Fark Etmez" or employee name
    val date: String, // "YYYY-MM-DD" or "DD.MM.YYYY"
    val time: String, // "HH:MM"
    val totalPrice: Double,
    val totalDuration: Int,
    val customerName: String,
    val customerPhone: String,
    val note: String = "",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED", "COMPLETED", "NO_SHOW"
    val timestamp: Long = System.currentTimeMillis(),
    val isRated: Boolean = false,
    val ratingStars: Int = 0,
    val ratingComment: String = ""
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val salonId: Int,
    val code: String,
    val discountType: String, // "PERCENT" or "FIXED"
    val value: Double,
    val minAmount: Double = 0.0,
    val limitCount: Int = 100,
    val usedCount: Int = 0,
    val expiryDate: String = "31.12.2026"
)
