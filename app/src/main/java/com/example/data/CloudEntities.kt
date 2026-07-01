package com.example.data

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String,
    val phone: String,
    val role: String
)

@Serializable
data class Salon(
    val id: Long,
    val ownerId: String? = null,
    val name: String,
    val category: String,
    val address: String,
    val imageUrl: String,
    val rating: Float,
    val reviewCount: Int,
    val workingHours: String,
    val isApproved: Boolean
)

@Serializable
data class Service(
    val id: Long,
    val salonId: Long,
    val name: String,
    val price: Double,
    val durationMinutes: Int,
    val category: String,
    val description: String
)

@Serializable
data class Staff(
    val id: Long,
    val salonId: Long,
    val name: String,
    val role: String,
    val imageUrl: String,
    val rating: Float,
    val workingHours: String,
    val offDays: String
)

@Serializable
data class Appointment(
    val id: Long? = null,
    val customerId: String? = null,
    val salonId: Long,
    val salonName: String,
    val serviceIds: String,
    val serviceNames: String,
    val staffId: Long? = null,
    val staffName: String,
    val date: String,
    val time: String,
    val totalPrice: Double,
    val totalDuration: Int,
    val customerName: String,
    val customerPhone: String,
    val note: String,
    val status: String = "PENDING",
    val timestamp: Long
)
