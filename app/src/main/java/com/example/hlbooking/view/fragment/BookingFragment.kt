package com.example.hlbooking.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hlbooking.R
import com.example.hlbooking.adapter.BookingAdapter
import com.example.hlbooking.adapter.item.BookingItem
import com.example.hlbooking.controller.RetrofitClient
import com.example.hlbooking.databinding.FragmentBookingBinding
import com.example.hlbooking.model.BookingDTO
import com.example.hlbooking.response.ResponseData
import com.example.hlbooking.response.booking.BookingResponse
import com.example.hlbooking.util.SharedPreferences
import com.example.hlbooking.util.setupDateAndTimePickers
import com.google.android.material.progressindicator.CircularProgressIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.format.DateTimeFormatter


class BookingFragment : Fragment() {
    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private var isLoading = false
    private var currentPage = 1
    private var isLastPage = false

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var scrollListener: RecyclerView.OnScrollListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressIndicator = view.findViewById(R.id.progressIndicator)

        setupDateAndTimePickers(
            requireContext(),
            binding.etDate,
            binding.etStartTime,
            binding.etEndTime
        )

        setupRecyclerView()
        binding.btnFind.setOnClickListener {
            currentPage = 0
            isLastPage = false
            isLoading = false
            resetData()
            findAvailableLapangan(0)
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        binding.rvBooking.layoutManager = layoutManager
        binding.rvBooking.adapter = BookingAdapter(
            mutableListOf(),
            onBookClick = { bookingItem ->
                showConfirmationDialog(bookingItem)
            },
            onRemoveClick = {},
            showBookButton = true
        )

        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                Log.d("ScrollListener", "Checking conditions: isLoading=$isLoading, isLastPage=$isLastPage, lastVisibleItem=$lastVisibleItemPosition, totalItemCount=$totalItemCount")


                if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 1) {
                    loadMoreData()
                }
            }
        }
        binding.rvBooking.addOnScrollListener(scrollListener)
    }

    private fun loadMoreData() {
        if (!isLoading && !isLastPage) {
            isLoading = true

            Log.d("ScrollListener", "Started loading more data: currentPage1=$currentPage")

            progressIndicator.visibility = View.VISIBLE
            currentPage++
            Log.d("ScrollListener", "Started loading more data: currentPage2=$currentPage")
            findAvailableLapangan(currentPage)
        } else {
            Log.d("ScrollListener", "Did not load more data: isLoading=$isLoading, isLastPage=$isLastPage")
        }
    }

    private fun resetData() {
        val adapter = binding.rvBooking.adapter as BookingAdapter
        adapter.clearData()
    }

    private fun findAvailableLapangan(page: Int) {

        val token = SharedPreferences.getToken(requireContext())
        val date = LocalDate.parse(binding.etDate.text.toString())
        val startTime = LocalTime.parse(binding.etStartTime.text.toString() + ":00")
        val endTime = LocalTime.parse(binding.etEndTime.text.toString() + ":00")

        RetrofitClient.bookingController.findAvailableLapangan(
            "Bearer $token",
            BookingDTO(null, "", date, startTime, endTime, null, null),
            page
        ).enqueue(object : Callback<BookingResponse<BookingDTO>> {
            override fun onResponse(call: Call<BookingResponse<BookingDTO>>, response: Response<BookingResponse<BookingDTO>>) {
                isLoading = false
                progressIndicator.visibility = View.GONE

                if (response.isSuccessful) {
                    val pageResponse = response.body()
                    if (pageResponse != null && pageResponse.content.isNotEmpty()) {
                        Log.e("Test", "Is last page: ${pageResponse.last}")
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

    private fun createBooking(bookingItem: BookingItem) {
        val token = "Bearer ${SharedPreferences.getToken(requireContext())}"
        val startTime = bookingItem.jamBooking.substringBefore(" - ")
        val endTime = bookingItem.jamBooking.substringAfter(" - ")

        val bookingDTO = BookingDTO(
            idBooking = null,
            namaLapangan = bookingItem.namaLapangan,
            tanggal = LocalDate.parse(bookingItem.tanggal, DateTimeFormatter.ofPattern("d MMMM yyyy")),
            jamMulaiBooking = LocalTime.parse("$startTime"),
            jamSelesaiBooking = LocalTime.parse("$endTime"),
            member = null,
            guest = null
        )

        RetrofitClient.bookingController.createBooking(token, bookingDTO).enqueue(object : Callback<ResponseData<String>> {
            override fun onResponse(call: Call<ResponseData<String>>, response: Response<ResponseData<String>>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    if (responseData?.success == true) {
                        showBookingSuccess()
                        removeBookedField(bookingItem)
                    } else {
                        Log.e("Booking Fragment", "Failed to fetch user data")
                    }
                } else {
                    Log.e("Booking Fragment", "Failed to fetch user2 data")
                }
            }

            override fun onFailure(call: Call<ResponseData<String>>, t: Throwable) {
                Log.e("Booking Fragment", "Error fetching user data: ${t.message}")
            }
        })
    }

    private fun showConfirmationDialog(bookingItem: BookingItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Booking Confirmation")
            .setMessage("Are you sure you want to book ${bookingItem.namaLapangan}?")
            .setPositiveButton("Yes") { dialog, which ->
                createBooking(bookingItem)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showBookingSuccess() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Booking Successful")
            .setMessage("Your booking has been successfully recorded.")
            .setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateRecyclerView(lapangan: List<BookingDTO>, isLastPage: Boolean) {
        val adapter = binding.rvBooking.adapter as BookingAdapter
        val newBookingItems = lapangan.map { bookingDTO ->
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

    private fun removeBookedField(bookingItem: BookingItem) {
        val adapter = binding.rvBooking.adapter as BookingAdapter
        adapter.removeItem(bookingItem)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}