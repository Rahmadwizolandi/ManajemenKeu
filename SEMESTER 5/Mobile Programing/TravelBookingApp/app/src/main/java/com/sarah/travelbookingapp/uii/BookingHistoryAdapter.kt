package com.sarah.travelbookingapp.uii

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.sarah.travelbookingapp.databinding.ItemHotelBookingHistoryBinding
import com.sarah.travelbookingapp.databinding.ItemTrainBookingHistoryBinding
import java.text.NumberFormat
import java.util.Locale

class BookingHistoryAdapter(
    private val items: List<BookingHistoryItem>,
    private val onEditClick: (BookingHistoryItem) -> Unit,
    private val onCancelClick: (BookingHistoryItem) -> Unit
) : RecyclerView.Adapter<BookingHistoryAdapter.BookingViewHolder>() {

    companion object {
        private const val TYPE_TRAIN = 1
        private const val TYPE_HOTEL = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is BookingHistoryItem.Train -> TYPE_TRAIN
            is BookingHistoryItem.Hotel -> TYPE_HOTEL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TRAIN -> {
                val binding = ItemTrainBookingHistoryBinding.inflate(inflater, parent, false)
                TrainViewHolder(binding)
            }
            TYPE_HOTEL -> {
                val binding = ItemHotelBookingHistoryBinding.inflate(inflater, parent, false)
                HotelViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        when (holder) {
            is TrainViewHolder -> holder.bind((items[position] as BookingHistoryItem.Train).booking)
            is HotelViewHolder -> holder.bind((items[position] as BookingHistoryItem.Hotel).booking)
        }
    }

    override fun getItemCount() = items.size

    abstract class BookingViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    inner class TrainViewHolder(private val binding: ItemTrainBookingHistoryBinding) : BookingViewHolder(binding) {
        fun bind(booking: com.sarah.travelbookingapp.database.TrainBooking) {
            binding.tvTrip.text = "Perjalanan: ${booking.origin} - ${booking.destination}"
            binding.tvDatetime.text = "Waktu: ${booking.date}, ${booking.time}"
            binding.tvPassengers.text = "Penumpang: ${booking.adultPassengers} Dewasa, ${booking.childPassengers} Anak"
            binding.tvTotalPrice.text = "Total Harga: ${formatRupiah(booking.totalPrice)}"
            binding.btnEdit.setOnClickListener { onEditClick(BookingHistoryItem.Train(booking)) }
            binding.btnCancel.setOnClickListener { onCancelClick(BookingHistoryItem.Train(booking)) }
        }
    }

    inner class HotelViewHolder(private val binding: ItemHotelBookingHistoryBinding) : BookingViewHolder(binding) {
        fun bind(booking: com.sarah.travelbookingapp.database.HotelBooking) {
            binding.tvHotelName.text = "Hotel: ${booking.hotelName}"
            binding.tvCity.text = "Kota: ${booking.city}"
            binding.tvDates.text = "Menginap: ${booking.checkInDate} - ${booking.checkOutDate}"
            binding.tvGuests.text = "Tamu: ${booking.guests}"
            binding.tvTotalPrice.text = "Total Harga: ${formatRupiah(booking.totalPrice)}"
            binding.btnEdit.setOnClickListener { onEditClick(BookingHistoryItem.Hotel(booking)) }
            binding.btnCancel.setOnClickListener { onCancelClick(BookingHistoryItem.Hotel(booking)) }
        }
    }

    private fun formatRupiah(number: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(number)
    }
}