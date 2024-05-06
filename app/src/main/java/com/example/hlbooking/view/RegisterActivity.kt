package com.example.hlbooking.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hlbooking.R
import com.example.hlbooking.controller.RetrofitClient
import com.example.hlbooking.model.RegisDTO
import com.example.hlbooking.response.AuthResponse
import com.example.hlbooking.response.error.RegisterError
import com.example.hlbooking.util.HideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        rootView = findViewById<View>(android.R.id.content)

        val emailEditText = findViewById<TextInputEditText>(R.id.email)
        val usernameEditText = findViewById<TextInputEditText>(R.id.username)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password)
        val nameEditText = findViewById<TextInputEditText>(R.id.nama)
        val phoneEditText = findViewById<TextInputEditText>(R.id.noTelepon)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val backToLogin = findViewById<RelativeLayout>(R.id.backToLoginGroup)

        registerButton.setOnClickListener {
            HideKeyboard.hideKeyboard(this)
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && phone.isNotEmpty()) {
                registerUser(email, username, password, name, phone)
            } else {
                Snackbar.make(rootView, "All fields are required", Snackbar.LENGTH_LONG).show()
            }
        }

        backToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(email: String, username: String, password: String, name: String, phone: String) {
        val regisDTO = RegisDTO(email, username, password, name, phone)
        RetrofitClient.authController.registerUser(regisDTO).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    showRegistrationSuccessDialog(email)
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Log.e("RetrofitError", "Call failed: " + t.message)
                Snackbar.make(rootView, "Network Error: ${t.message}", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun showRegistrationSuccessDialog(email: String) {
        AlertDialog.Builder(this)
            .setTitle("Registrasi Berhasil")
            .setMessage("Silahkan verifikasi akun Anda")
            .setPositiveButton("OK") { _, _ ->
                navigateToVerificationPage(email)
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToVerificationPage(email: String) {
        val intent = Intent(this, VerificationActivity::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }

    private fun handleErrorResponse(response: Response<AuthResponse>) {
        val gson = Gson()
        val errorBody = response.errorBody()?.string() ?: gson.toJson(response.body()?.message)
        try {
            val registerError = gson.fromJson(errorBody, RegisterError::class.java)
            val specificError = registerError.registerSubErrors.find { it.field in listOf("username", "noTelepon", "email", "password", "nama") }
            val errorMessage = specificError?.message ?: "Gagal Registrasi: Kesalahan tidak diketahui"
            Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_LONG).show()
        } catch (e: JsonSyntaxException) {
            Snackbar.make(rootView, "Kesalahan format respons: ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(rootView, "Gagal Registrasi: Kesalahan saat memparsing pesan kesalahan", Snackbar.LENGTH_LONG).show()
        }
    }
}
