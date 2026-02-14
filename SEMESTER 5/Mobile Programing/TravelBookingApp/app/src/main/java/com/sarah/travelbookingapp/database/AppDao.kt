package com.sarah.travelbookingapp.database

import androidx.room.*

@Dao
interface AppDao {

    // USER
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    // TRAIN BOOKING
    @Insert
    suspend fun insertTrainBooking(trainBooking: TrainBooking)

    @Update
    suspend fun updateTrainBooking(trainBooking: TrainBooking)

    @Query("SELECT * FROM train_bookings WHERE userId = :userId")
    suspend fun getTrainBookings(userId: Int): List<TrainBooking>

    @Query("DELETE FROM train_bookings WHERE id = :id")
    suspend fun deleteTrainBooking(id: Int)

    // HOTEL BOOKING
    @Insert
    suspend fun insertHotelBooking(hotelBooking: HotelBooking)

    @Update
    suspend fun updateHotelBooking(hotelBooking: HotelBooking)

    @Query("SELECT * FROM hotel_bookings WHERE userId = :userId")
    suspend fun getHotelBookings(userId: Int): List<HotelBooking>

    @Query("DELETE FROM hotel_bookings WHERE id = :id")
    suspend fun deleteHotelBooking(id: Int)
}
