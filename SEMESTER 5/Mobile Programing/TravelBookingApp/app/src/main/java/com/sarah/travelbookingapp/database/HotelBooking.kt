package com.sarah.travelbookingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hotel_bookings")
data class HotelBooking(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val city: String,
    val hotelName: String,
    val checkInDate: String,
    val checkOutDate: String,
    val guests: Int, // Number of guests
    val totalPrice: Double
)
