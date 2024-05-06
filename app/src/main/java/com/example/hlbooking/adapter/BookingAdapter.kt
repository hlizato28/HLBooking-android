package com.example.hlbooking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hlbooking.R
import com.example.hlbooking.adapter.item.BookingItem

class BookingAdapter (
    private val book: MutableList<BookingItem>,
    private val onBookClick: (BookingItem) -> Unit,
    private val onRemoveClick: (BookingItem) -> Unit,
    private val showBookButton: Boolean
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    fun addItems(newItems: List<BookingItem>) {
        val startPosition = book.size
        book.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val book = book[position]
        holder.bind(book)
    }

    override fun getItemCount(): Int = book.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaLapangan: TextView = itemView.findViewById(R.id.tvNamaLapangan)
        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        private val tvJamBooking: TextView = itemView.findViewById(R.id.tvJamBooking)
        private val btnBook: Button = itemView.findViewById(R.id.btnBook)
        private val btnRemove: Button = itemView.findViewById(R.id.btnRemove)


        fun bind(item: BookingItem) {
            btnBook.visibility = if (showBookButton) View.VISIBLE else View.GONE
            btnRemove.visibility = if (showBookButton) View.GONE else View.VISIBLE
            tvNamaLapangan.text = item.namaLapangan
            tvTanggal.text = item.tanggal
            tvJamBooking.text = item.jamBooking
        }

        init {
            btnBook.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val bookingItem = book[position]
                    onBookClick(bookingItem)
                }
            }

            btnRemove.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val bookingItem = book[position]
                    onRemoveClick(bookingItem)
                }
            }
        }
    }

    fun removeItem(bookingItem: BookingItem) {
        val position = book.indexOf(bookingItem)
        if (position != -1) {
            book.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clearData() {
        book.clear()
        notifyDataSetChanged()
    }
}