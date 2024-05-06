package com.example.hlbooking.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import android.widget.TextView
import com.example.hlbooking.MainActivity
import com.example.hlbooking.R
import com.example.hlbooking.controller.RetrofitClient
import com.example.hlbooking.model.LoginDTO
import com.example.hlbooking.response.AuthResponse
import com.example.hlbooking.response.error.LoginError
import com.example.hlbooking.util.HideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var rootView: View
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        rootView = findViewById<View>(android.R.id.content)

        val usernameEditText = findViewById<TextInputEditText>(R.id.username)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLinkTextView = findViewById<TextView>(R.id.registerLink)

        loginButton.setOnClickListener {
            HideKeyboard.hideKeyboard(this)
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Snackbar.make(rootView, "Please enter both username and password", Snackbar.LENGTH_LONG).show()
            }
        }

        registerLinkTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(username: String, password: String) {
        email = username
        val loginDTO = LoginDTO(username, password)

        RetrofitClient.authController.loginUser(loginDTO).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseData = response.body()?.data
                    if (responseData is Map<*, *>) {
                        val token = responseData["token"] as? String
                        val id = responseData["id"] as? Double

                        if (token != null && id != null) {
                            saveUser(token, id)
                            navigateToHomePage()
                        }
                        printSharedPreferences()
                    } else {
                        Snackbar.make(rootView, "Failed to retrieve the authentication token.", Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Snackbar.make(rootView, "Network Error: ${t.message}", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun saveUser(token: String, id: Double) {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("token", token)
            putLong("id", id.toLong())
            apply()
        }
    }

    fun printSharedPreferences() {
        val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val allEntries = prefs.all
        for (entry in allEntries.entries) {
            Log.d("SharedPreferences", "${entry.key}: ${entry.value}")
        }
    }

    private fun navigateToHomePage() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("openFragment", "BookingFragment")
        startActivity(intent)
        finish()
    }

    private fun handleErrorResponse(response: Response<AuthResponse>) {
        val gson = Gson()
        val errorBody = response.errorBody()?.string() ?: gson.toJson(response.body()?.message)
        try {
            val authError = gson.fromJson(errorBody, LoginError::class.java)
            val errorCode = authError?.errorCode

            if (errorCode == "FV01006") {
                navigateToVerificationPage(email)
            } else {
                val errorMessage = authError?.message ?: "Login Failed: Unknown error"
                Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_LONG).show()
            }
        } catch (e: JsonSyntaxException) {
            Snackbar.make(rootView, "Error response format: ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(rootView, "Login Failed: Error parsing error message", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun navigateToVerificationPage(email: String) {
        val intent = Intent(this, VerificationActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
    }

}