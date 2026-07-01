package com.example

import com.example.data.AppointmentEntity
import com.example.ui.AppViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AppointmentSlotTest {

    private fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }

    private fun calculateAvailableSlots(
        workingHours: String,
        existingAppointments: List<AppointmentEntity>,
        totalDurationMinutes: Int
    ): List<String> {
        if (workingHours.isBlank() || totalDurationMinutes <= 0) return emptyList()
        val parts = workingHours.split("-")
        if (parts.size != 2) return emptyList()
        
        val startWork = parseTimeToMinutes(parts[0].trim())
        val endWork = parseTimeToMinutes(parts[1].trim())
        
        val availableSlots = mutableListOf<String>()
        var current = startWork
        
        while (current + totalDurationMinutes <= endWork) {
            val slotStart = current
            val slotEnd = current + totalDurationMinutes
            
            val isOverlapping = existingAppointments.any { appt ->
                val apptStart = parseTimeToMinutes(appt.time)
                val apptEnd = apptStart + appt.totalDuration
                slotStart < apptEnd && apptStart < slotEnd
            }
            
            if (!isOverlapping) {
                val hour = current / 60
                val minute = current % 60
                availableSlots.add(String.format(Locale.US, "%02d:%02d", hour, minute))
            }
            
            current += 15
        }
        
        return availableSlots
    }

    @Test
    fun testEmptyDaySlots() {
        val slots = calculateAvailableSlots("09:00 - 10:00", emptyList(), 30)
        assertEquals(3, slots.size)
        assertEquals("09:00", slots[0])
        assertEquals("09:15", slots[1])
        assertEquals("09:30", slots[2])
    }

    @Test
    fun testWithOverlappingAppointments() {
        val appts = listOf(
            AppointmentEntity(
                salonId = 1,
                salonName = "Salon",
                serviceIds = "1",
                serviceNames = "Service",
                staffId = 1,
                staffName = "Staff",
                date = "04.06.2026",
                time = "09:30",
                totalPrice = 100.0,
                totalDuration = 30, // 09:30 to 10:00
                customerName = "Cust",
                customerPhone = "123"
            )
        )
        val slots = calculateAvailableSlots("09:00 - 11:00", appts, 60)
        assertTrue(slots.contains("10:00"))
        assertTrue(!slots.contains("09:00"))
        assertTrue(!slots.contains("09:30"))
    }
}
