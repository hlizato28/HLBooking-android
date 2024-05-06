package com.example.hlbooking.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hlbooking.R
import com.example.hlbooking.adapter.BookingAdapter
import com.example.hlbooking.adapter.item.BookingItem
import com.example.hlbooking.controller.RetrofitClient
import com.example.hlbooking.databinding.FragmentMyBookingBinding
import com.example.hlbooking.model.BookingDTO
import com.example.hlbooking.response.ResponseData
import com.example.hlbooking.response.booking.BookingResponse
import com.example.hlbooking.util.SharedPreferences
import com.google.android.material.progressindicator.CircularProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyBookingFragment : Fragment() {
    private var _binding: FragmentMyBookingBinding? = null
    private val binding get() = _binding!!

    private var isLoading = false
    private var currentPage = 0
    private var isLastPage = false

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var scrollListener: RecyclerView.OnScrollListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressIndicator = view.findViewById(R.id.progressIndicator)

        setupRecyclerView()
        getValidBookingById(0)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.rvMyBooking.layoutManager = layoutManager
        binding.rvMyBooking.adapter = BookingAdapter( mutableListOf(),
            onBookClick = {},
            onRemoveClick = {
                bookingItem ->
                showRemoveConfirmationDialog(bookingItem)
            },
            showBookButton = false
        )

        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 1) {
                    loadMoreData()
                }
            }
        }
        binding.rvMyBooking.addOnScrollListener(scrollListener)
    }

    private fun loadMoreData() {
        if (!isLoading && !isLastPage) {
            isLoading = true
            progressIndicator.visibility = View.VISIBLE
            currentPage++
            getValidBookingById(currentPage)
        }
    }

    private fun getValidBookingById(page: Int) {
        val token = SharedPreferences.getToken(requireContext())
        val userId = SharedPreferences.getUserId(requireContext())

        RetrofitClient.bookingController.getValidBookingById(
            "Bearer $token",
            userId,
            page
        ).enqueue(object : Callback<BookingResponse<BookingDTO>> {
            override fun onResponse(
                call: Call<BookingResponse<BookingDTO>>,
                response: Response<BookingResponse<BookingDTO>>
            ) {
                isLoading = false
                progressIndicator.visibility = View.GONE

                if (response.isSuccessful) {
                    val pageResponse = response.body()
                    if (pageResponse != null && pageResponse.content.isNotEmpty()) {
                        updateRecyclerView(pageResponse.content, pageResponse.last)
                    } else {
                        binding.tvNoData.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("BookingFragment", "Failed to fetch available fields")
                    binding.tvNoData.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<BookingResponse<BookingDTO>>, t: Throwable) {
                Log.e("BookingFragment", "Error fetching available fields: ${t.message}")
                binding.tvNoData.visibility = View.VISIBLE
                isLoading = false
                progressIndicator.visibility = View.GONE
            }
        })
    }

    private fun showRemoveConfirmationDialog(bookingItem: BookingItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Penghapusan")
            .setMessage("Apakah Anda yakin ingin menghapus booking ini?")
            .setPositiveButton("Ya") { _, _ ->
                deleteBooking(bookingItem)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteBooking(bookingItem: BookingItem) {
        val token = SharedPreferences.getToken(requireContext())
        val userId = SharedPreferences.getUserId(requireContext())
        val bookId = bookingItem.bookingId ?: return

        RetrofitClient.bookingController.deleteBooking(
            "Bearer $token",
            userId,
            bookId
        ).enqueue(object : Callback<ResponseData<String>> {
            override fun onResponse(call: Call<ResponseData<String>>, response: Response<ResponseData<String>>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData?.success == true) {
                        removeBookingItem(bookingItem)
                        showSuccessDialog()
                    } else {
                        showErrorDialog()
                    }
                } else {
                    showErrorDialog()
                }
            }

            override fun onFailure(call: Call<ResponseData<String>>, t: Throwable) {
                showErrorDialog()
            }
        })
    }

    private fun updateRecyclerView(booking: List<BookingDTO>, isLastPage: Boolean) {
        val adapter = binding.rvMyBooking.adapter as BookingAdapter
        val newBookingItems = booking.map { bookingDTO ->
            BookingItem(
                bookingDTO.idBooking,
                bookingDTO.namaLapangan,

                "${bookingDTO.tanggal.dayOfMonth} " +
                        "${bookingDTO.tanggal.month.name.lowercase().replaceFirstChar { it.uppercase() }} " +
                        "${bookingDTO.tanggal.year}",

                "${bookingDTO.jamMulaiBooking.hour}:00 - " +
                        "${bookingDTO.jamSelesaiBooking.hour}:00"
            )
        }
        adapter.addItems(newBookingItems)
        this.isLastPage = isLastPage
    }

    private fun removeBookingItem(bookingItem: BookingItem) {
        val adapter = binding.rvMyBooking.adapter as BookingAdapter
        adapter.removeItem(bookingItem)
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Berhasil")
            .setMessage("Booking berhasil dihapus.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Gagal")
            .setMessage("Gagal menghapus booking. Silakan coba lagi.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}