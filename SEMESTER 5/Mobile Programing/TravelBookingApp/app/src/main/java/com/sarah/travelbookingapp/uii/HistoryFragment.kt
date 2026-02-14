package com.sarah.travelbookingapp.uii

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sarah.travelbookingapp.database.AppDatabase
import com.sarah.travelbookingapp.databinding.FragmentHistoryBinding
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadBookingHistory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Data is now loaded in onResume, so this can be empty or used for one-time setup.
    }

    private fun loadBookingHistory() {
        val db = AppDatabase.getDatabase(requireContext())
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)

        if (userId != -1) {
            lifecycleScope.launch {
                val trainBookings = db.appDao().getTrainBookings(userId).map { BookingHistoryItem.Train(it) }
                val hotelBookings = db.appDao().getHotelBookings(userId).map { BookingHistoryItem.Hotel(it) }

                val combinedList = (trainBookings + hotelBookings).sortedByDescending {
                    when (it) {
                        is BookingHistoryItem.Train -> it.booking.id.toLong()
                        is BookingHistoryItem.Hotel -> it.booking.id.toLong()
                    }
                }

                binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
                binding.rvHistory.adapter = BookingHistoryAdapter(combinedList, 
                    onEditClick = { item ->
                        handleEditClick(item)
                    },
                    onCancelClick = { item ->
                        showCancelConfirmationDialog(item)
                    }
                )
            }
        }
    }

    private fun handleEditClick(item: BookingHistoryItem) {
        when (item) {
            is BookingHistoryItem.Train -> {
                val intent = Intent(requireContext(), TrainBookingActivity::class.java).apply {
                    putExtra("EDIT_BOOKING_ID", item.booking.id)
                }
                startActivity(intent)
            }
            is BookingHistoryItem.Hotel -> {
                val intent = Intent(requireContext(), HotelBookingActivity::class.java).apply {
                    putExtra("EDIT_BOOKING_ID", item.booking.id)
                }
                startActivity(intent)
            }
        }
    }

    private fun showCancelConfirmationDialog(item: BookingHistoryItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Pembatalan")
            .setMessage("Apakah Anda yakin ingin membatalkan pesanan ini?")
            .setPositiveButton("Ya") { _, _ ->
                cancelBooking(item)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun cancelBooking(item: BookingHistoryItem) {
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            when (item) {
                is BookingHistoryItem.Train -> db.appDao().deleteTrainBooking(item.booking.id)
                is BookingHistoryItem.Hotel -> db.appDao().deleteHotelBooking(item.booking.id)
            }
            Toast.makeText(requireContext(), "Pesanan berhasil dibatalkan", Toast.LENGTH_SHORT).show()
            loadBookingHistory() // Refresh the list
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}