package com.sarah.travelbookingapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "train_bookings")
data class TrainBooking(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val city: String,
    val origin: String,
    val destination: String,
    val date: String,
    val time: String,
    val adultPassengers: Int,
    val childPassengers: Int,
    val totalPrice: Double
)
