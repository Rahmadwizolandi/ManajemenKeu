package com.sarah.travelbookingapp.uii

import com.sarah.travelbookingapp.database.HotelBooking
import com.sarah.travelbookingapp.database.TrainBooking

sealed class BookingHistoryItem {
    data class Train(val booking: TrainBooking) : BookingHistoryItem()
    data class Hotel(val booking: HotelBooking) : BookingHistoryItem()
}
