package com.sarah.travelbookingapp.uii

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sarah.travelbookingapp.database.AppDatabase
import com.sarah.travelbookingapp.database.HotelBooking
import com.sarah.travelbookingapp.databinding.ActivityHotelBookingBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class HotelBookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHotelBookingBinding
    private var checkInCalendar = Calendar.getInstance()
    private var checkOutCalendar = Calendar.getInstance()
    private var editingBooking: HotelBooking? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotelBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCityAutoComplete()
        setupDatePicker(binding.etCheckInDate, checkInCalendar)
        setupDatePicker(binding.etCheckOutDate, checkOutCalendar)

        val bookingId = intent.getIntExtra("EDIT_BOOKING_ID", -1)
        if (bookingId != -1) {
            loadBookingForEdit(bookingId)
        }

        val db = AppDatabase.getDatabase(this)
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        binding.btnBookHotel.setOnClickListener {
            val city = binding.actvCity.text.toString().trim()
            val hotelName = binding.etHotelName.text.toString().trim()
            val checkInDate = binding.etCheckInDate.text.toString()
            val checkOutDate = binding.etCheckOutDate.text.toString()
            val guests = binding.etGuests.text.toString().toIntOrNull() ?: 0
            val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)

            if (city.isNotEmpty() && hotelName.isNotEmpty() && checkInDate.isNotEmpty() && checkOutDate.isNotEmpty() && guests > 0 && userId != -1) {
                val nights = getNightDifference(checkInDate, checkOutDate)
                if (nights <= 0) {
                    Toast.makeText(this, "Tanggal check-out harus setelah tanggal check-in", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val totalPrice = nights * guests * 500000.0 // Assume 500k per guest per night

                lifecycleScope.launch {
                    if (editingBooking != null) {
                        val updatedBooking = editingBooking!!.copy(
                            city = city,
                            hotelName = hotelName,
                            checkInDate = checkInDate,
                            checkOutDate = checkOutDate,
                            guests = guests,
                            totalPrice = totalPrice
                        )
                        db.appDao().updateHotelBooking(updatedBooking)
                        Toast.makeText(this@HotelBookingActivity, "Perubahan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    } else {
                        val hotelBooking = HotelBooking(
                            userId = userId,
                            city = city,
                            hotelName = hotelName,
                            checkInDate = checkInDate,
                            checkOutDate = checkOutDate,
                            guests = guests,
                            totalPrice = totalPrice
                        )
                        db.appDao().insertHotelBooking(hotelBooking)
                        Toast.makeText(this@HotelBookingActivity, "Hotel berhasil dipesan!", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
            } else {
                Toast.makeText(this@HotelBookingActivity, "Harap isi semua kolom dengan benar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBookingForEdit(bookingId: Int) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            // This is not the most efficient way, but it works for this example.
            // A better approach would be to have a dedicated `getHotelBookingById` in the DAO.
            val booking = db.appDao().getHotelBookings(getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("LOGGED_IN_USER_ID", -1)).find { it.id == bookingId }
            if (booking != null) {
                editingBooking = booking
                binding.actvCity.setText(booking.city, false)
                binding.etHotelName.setText(booking.hotelName)
                binding.etCheckInDate.setText(booking.checkInDate)
                binding.etCheckOutDate.setText(booking.checkOutDate)
                binding.etGuests.setText(booking.guests.toString())
                binding.btnBookHotel.text = "Simpan Perubahan"
            }
        }
    }

    private fun setupCityAutoComplete() {
        val cities = arrayOf("Jakarta", "Bandung", "Yogyakarta", "Surabaya", "Semarang", "Bali")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        binding.actvCity.setAdapter(adapter)
    }

    private fun setupDatePicker(editText: EditText, calendar: Calendar) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView(editText, calendar)
        }

        editText.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateInView(editText: EditText, calendar: Calendar) {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        editText.setText(sdf.format(calendar.time))
    }

    private fun getNightDifference(checkIn: String, checkOut: String): Long {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        try {
            val date1 = sdf.parse(checkIn)
            val date2 = sdf.parse(checkOut)
            val diff = date2.time - date1.time
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            return 0
        }
    }
}