package com.example.nougatbora

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.nougatbora.databinding.ActivitySingupBinding
import com.google.gson.Gson
import android.util.Log


class singup : AppCompatActivity() {
    lateinit var binding: ActivitySingupBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // init ViewModel
        val repository = AuthRepository(ApiClient.authApi)
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // click on create account button
        binding.bonjour.setOnClickListener {
            val name = binding.tx1.text.toString().trim()
            val email = binding.tx2.text.toString().trim()
            val phone = binding.tx3.text.toString().trim()
            val password = binding.tx5.text.toString().trim()

            var isValid = true

            // local validation
            if (name.length < 6) {
                binding.nomred.visibility = View.VISIBLE
                isValid = false
            } else binding.nomred.visibility = View.INVISIBLE

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.mailred.visibility = View.VISIBLE
                isValid = false
            } else binding.mailred.visibility = View.INVISIBLE

            if (phone.length != 10) {
                binding.phonered.visibility = View.VISIBLE
                isValid = false
            } else binding.phonered.visibility = View.INVISIBLE

            if (password.length < 8) {
                binding.passwordred.visibility = View.VISIBLE
                isValid = false
            } else binding.passwordred.visibility = View.INVISIBLE

            if (isValid) {
                val request = RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    role = "driver",
                    phoneNumber = phone
                )
                Log.d("SignupRequest", Gson().toJson(request))
                viewModel.register(request)
            }
        }

        // observe API result
        viewModel.authResult.observe(this) { result ->
            result.onSuccess {
                // ✅ show success dialog only after account is created
                val d = Dialog(this)
                d.setContentView(R.layout.alertdialogsignup)
                d.window!!.setBackgroundDrawableResource(R.color.transparent)
                d.window!!.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                d.show()

                binding.created.visibility = View.VISIBLE
                binding.nomred.visibility = View.INVISIBLE
                binding.mailred.visibility = View.INVISIBLE
                binding.phonered.visibility = View.INVISIBLE
                binding.passwordred.visibility = View.INVISIBLE
            }
            result.onFailure { e ->
                // ✅ show API/network error as Toast instead of red error
                Toast.makeText(
                    this,
                    "Signup failed: ${e.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // back arrow
        binding.weli.setOnClickListener {
            val i = Intent(this@singup, loginpage::class.java)
            startActivity(i)
        }
    }
}
