package com.example.hlbooking.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.hlbooking.R
import com.example.hlbooking.controller.RetrofitClient
import com.example.hlbooking.model.VerifyDTO
import com.example.hlbooking.response.AuthResponse
import com.example.hlbooking.response.error.LoginError
import com.example.hlbooking.util.HideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificationActivity : AppCompatActivity() {
    private lateinit var rootView: View
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        rootView = findViewById<View>(android.R.id.content)
        email = intent.getStringExtra("email") ?: ""

        val etVerifCode = findViewById<TextInputEditText>(R.id.etVerifCode)
        val btnVerif = findViewById<Button>(R.id.btnVerif)
        val backBtn = findViewById<RelativeLayout>(R.id.backGroup)

        etVerifCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) {
                    HideKeyboard.hideKeyboard(this@VerificationActivity)
                }
            }
        })

        btnVerif.setOnClickListener {
            val verificationCode = etVerifCode.text.toString().trim()
            if (verificationCode.isNotEmpty() && verificationCode.length == 6) {
                verifyAccount(email, verificationCode)
            } else {
                Snackbar.make(rootView, "Please enter a valid 6-digit verification code", Snackbar.LENGTH_LONG).show()
            }
        }
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun verifyAccount(email: String, verificationCode: String) {
        val verifyDTO = VerifyDTO(email, verificationCode)

        RetrofitClient.authController.verifyAccount(verifyDTO).enqueue(object :
            Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    showVerificationSuccessDialog()
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Snackbar.make(rootView, "Network Error: ${t.message}", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun showVerificationSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Verifikasi Berhasil")
            .setMessage("Akun Anda terverifikasi. Silahkan lakukan Login ulang.")
            .setPositiveButton("OK") { _, _ ->
                navigateToLoginPage()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun handleErrorResponse(response: Response<AuthResponse>) {
        val gson = Gson()
        val errorBody = response.errorBody()?.string() ?: gson.toJson(response.body()?.message)
        try {
            val authError = gson.fromJson(errorBody, LoginError::class.java)
            val errorMessage = authError?.message ?: "Verification Failed: Unknown error"
            Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_LONG).show()
        } catch (e: JsonSyntaxException) {
            Snackbar.make(rootView, "Error response format: ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(rootView, "Verification Failed: Error parsing error message", Snackbar.LENGTH_LONG).show()
        }
    }
}