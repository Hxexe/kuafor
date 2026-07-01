package com.example.data

import io.github.jan.supabase.postgrest.from

object SupabaseRepository {

    suspend fun getSalons(): List<Salon> {
        return SupabaseClient.client.from("salons")
            .select()
            .decodeList<Salon>()
    }

    suspend fun getServices(salonId: Long): List<Service> {
        return SupabaseClient.client.from("services").select {
            filter {
                eq("salonId", salonId)
            }
        }.decodeList<Service>()
    }

    suspend fun getStaff(salonId: Long): List<Staff> {
        return SupabaseClient.client.from("staff").select {
            filter {
                eq("salonId", salonId)
            }
        }.decodeList<Staff>()
    }

    suspend fun createAppointment(appointment: Appointment): Appointment {
        return SupabaseClient.client.from("appointments").insert(appointment) {
            select()
        }.decodeSingle<Appointment>()
    }

    suspend fun getAppointmentsForSalon(salonId: Long): List<Appointment> {
        return SupabaseClient.client.from("appointments").select {
            filter {
                eq("salonId", salonId)
            }
        }.decodeList<Appointment>()
    }

    suspend fun getAppointmentsForCustomer(customerId: String): List<Appointment> {
        return SupabaseClient.client.from("appointments").select {
            filter {
                eq("customerId", customerId)
            }
        }.decodeList<Appointment>()
    }

    suspend fun updateAppointmentStatus(appointmentId: Long, status: String) {
        SupabaseClient.client.from("appointments").update(
            {
                set("status", status)
            }
        ) {
            filter {
                eq("id", appointmentId)
            }
        }
    }
}
