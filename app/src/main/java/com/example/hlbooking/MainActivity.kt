package com.example.hlbooking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.hlbooking.controller.RetrofitClient
import com.example.hlbooking.databinding.ActivityMainBinding
import com.example.hlbooking.model.UserDTO
import com.example.hlbooking.response.ResponseData
import com.example.hlbooking.util.SharedPreferences
import com.example.hlbooking.view.LoginActivity
import com.example.hlbooking.view.fragment.BookingFragment
import com.example.hlbooking.view.fragment.MyBookingFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.appBarMain.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_booking -> replaceFragment(BookingFragment(), "Booking")
                R.id.nav_my_booking -> replaceFragment(MyBookingFragment(), "My Booking")
                R.id.nav_logout -> logout()
            }
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        fetchUserDataAndUpdateNavHeader()

        handleIntentFragment()
    }

    private fun fetchUserDataAndUpdateNavHeader() {

        val userId = SharedPreferences.getUserId(this)
        val token = SharedPreferences.getToken(this)

        RetrofitClient.userController.getUserById("Bearer $token", userId).enqueue(object :
            Callback<ResponseData<UserDTO>> {
            override fun onResponse(call: Call<ResponseData<UserDTO>>, response: Response<ResponseData<UserDTO>>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    updateNavHeader(user?.data?.username ?: "", user?.data?.noTelepon ?: "")
                } else {
                    Log.e("MainActivity", "Failed to fetch user data")
                }
            }

            override fun onFailure(call: Call<ResponseData<UserDTO>>, t: Throwable) {
                Log.e("MainActivity", "Error fetching user data: ${t.message}")
            }
        })
    }

    private fun updateNavHeader(username: String, phone: String) {
        val headerView = binding.navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.username)
        val phoneTextView = headerView.findViewById<TextView>(R.id.phone)
        usernameTextView.text = username
        phoneTextView.text = "+$phone"
    }

    private fun handleIntentFragment() {
        val openFragment = intent.getStringExtra("openFragment")
        if (openFragment == "BookingFragment") {
            replaceFragment(BookingFragment(), "Booking")
            binding.navView.setCheckedItem(R.id.nav_booking)
        }
    }


    private fun replaceFragment(fragment: Fragment, title: String) {
        supportActionBar?.title = title
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_container, fragment)
        fragmentTransaction.commit()
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("token")
            apply()
        }

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}