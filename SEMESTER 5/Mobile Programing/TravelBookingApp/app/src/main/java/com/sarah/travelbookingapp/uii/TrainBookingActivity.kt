package com.sarah.travelbookingapp.uii

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sarah.travelbookingapp.database.AppDatabase
import com.sarah.travelbookingapp.database.TrainBooking
import com.sarah.travelbookingapp.databinding.ActivityTrainBookingBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TrainBookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrainBookingBinding
    private val calendar = Calendar.getInstance()
    private var editingBooking: TrainBooking? = null

    private val cityToStationsMap = mapOf(
        "Jakarta" to listOf("Gambir (GMR)", "Pasar Senen (PSE)"),
        "Bandung" to listOf("Bandung (BD)", "Kiaracondong (KAC)"),
        "Yogyakarta" to listOf("Yogyakarta (YK)", "Lempuyangan (LPN)"),
        "Surabaya" to listOf("Surabaya Gubeng (SGU)", "Surabaya Pasar Turi (SBI)")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCityAutoComplete()
        setupDatePicker()
        setupTimePicker()

        val bookingId = intent.getIntExtra("EDIT_BOOKING_ID", -1)
        if (bookingId != -1) {
            loadBookingForEdit(bookingId)
        }

        val db = AppDatabase.getDatabase(this)
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        binding.btnBookTrain.setOnClickListener {
            val city = binding.actvCity.text.toString().trim()
            val origin = binding.actvOrigin.text.toString().trim()
            val destination = binding.actvDestination.text.toString().trim()
            val date = binding.etDate.text.toString()
            val time = binding.etTime.text.toString()
            val adultPassengers = binding.etAdultPassengers.text.toString().toIntOrNull() ?: 0
            val childPassengers = binding.etChildPassengers.text.toString().toIntOrNull() ?: 0
            val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)

            if (city.isNotEmpty() && origin.isNotEmpty() && destination.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty() && (adultPassengers > 0 || childPassengers > 0) && userId != -1) {
                val totalPrice = (adultPassengers * 150000) + (childPassengers * 75000).toDouble()

                lifecycleScope.launch {
                    if (editingBooking != null) {
                        val updatedBooking = editingBooking!!.copy(
                            city = city,
                            origin = origin,
                            destination = destination,
                            date = date,
                            time = time,
                            adultPassengers = adultPassengers,
                            childPassengers = childPassengers,
                            totalPrice = totalPrice
                        )
                        db.appDao().updateTrainBooking(updatedBooking)
                        Toast.makeText(this@TrainBookingActivity, "Perubahan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    } else {
                        val trainBooking = TrainBooking(
                            userId = userId,
                            city = city,
                            origin = origin,
                            destination = destination,
                            date = date,
                            time = time,
                            adultPassengers = adultPassengers,
                            childPassengers = childPassengers,
                            totalPrice = totalPrice
                        )
                        db.appDao().insertTrainBooking(trainBooking)
                        Toast.makeText(this@TrainBookingActivity, "Tiket kereta berhasil dipesan!", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
            } else {
                Toast.makeText(this@TrainBookingActivity, "Harap isi semua kolom dengan benar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBookingForEdit(bookingId: Int) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            // This is not the most efficient way, but it works for this example.
            // A better approach would be to have a dedicated `getTrainBookingById` in the DAO.
            val booking = db.appDao().getTrainBookings(getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("LOGGED_IN_USER_ID", -1)).find { it.id == bookingId }
            if (booking != null) {
                editingBooking = booking
                binding.actvCity.setText(booking.city, false)
                setupStationAutoComplete(booking.city)
                binding.actvOrigin.setText(booking.origin, false)
                binding.actvDestination.setText(booking.destination, false)
                binding.etDate.setText(booking.date)
                binding.etTime.setText(booking.time)
                binding.etAdultPassengers.setText(booking.adultPassengers.toString())
                binding.etChildPassengers.setText(booking.childPassengers.toString())
                binding.btnBookTrain.text = "Simpan Perubahan"
            }
        }
    }

    private fun setupCityAutoComplete() {
        val cities = cityToStationsMap.keys.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cities)
        binding.actvCity.setAdapter(adapter)

        binding.actvCity.setOnItemClickListener { parent, _, position, _ ->
            val selectedCity = parent.getItemAtPosition(position) as String
            setupStationAutoComplete(selectedCity)
        }
    }

    private fun setupStationAutoComplete(city: String) {
        val stations = cityToStationsMap[city] ?: listOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, stations)
        binding.actvOrigin.setAdapter(adapter)
        binding.actvDestination.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePicker() {
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            updateTimeInView()
        }

        binding.etTime.setOnClickListener {
            TimePickerDialog(
                this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.etDate.setText(sdf.format(calendar.time))
    }

    private fun updateTimeInView() {
        val myFormat = "HH:mm"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.etTime.setText(sdf.format(calendar.time))
    }
}