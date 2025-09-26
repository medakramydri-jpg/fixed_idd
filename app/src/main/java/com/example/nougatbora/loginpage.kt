package com.example.nougatbora

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.nougatbora.databinding.ActivityLoginpageBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class loginpage : AppCompatActivity() {
    private lateinit var binding: ActivityLoginpageBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Check if user is already logged in
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedToken = sharedPrefs.getString("jwt_token", null)
        if (savedToken != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = AuthRepository(ApiClient.authApi)
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        binding.connect.setOnClickListener {
            val email = binding.identifiant.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isNotEmpty() && password.length >= 8) {
                viewModel.login(LoginRequest(email, password))
            } else {
                // 🔹 Simple validation
                binding.inpasswordred.visibility =
                    if (password.length < 8) View.VISIBLE else View.INVISIBLE
                binding.identifiantred.visibility =
                    if (email.isEmpty()) View.VISIBLE else View.INVISIBLE
            }
        }

        // 🔹 Observe login result
        viewModel.authResult.observe(this) { result ->
            result.onSuccess { authResponse ->
                val editor = sharedPrefs.edit()
                editor.putString("jwt_token", authResponse.token)
                editor.putString("user_id", authResponse.user.UserId)
                editor.putString("user_role", authResponse.user.role)
                editor.commit()

                val userId = authResponse.user.UserId
                val role = authResponse.user.role

                if (role == "driver" && authResponse.driver == null) {
                    // Fetch driver details from API
                    lifecycleScope.launch {
                        try {
                            val driver = withContext(Dispatchers.IO) {
                                repository.getDriverById(userId, authResponse.token)
                            }

                            // Convert products list to JSON string
                            val productsJson = Gson().toJson(driver.products)

                            // Start infodelivreur2 with products
                            val intent = Intent(this@loginpage, infodelivreur2::class.java)
                            intent.putExtra("driver_name", driver.name)
                            intent.putExtra("products_json", productsJson)
                            startActivity(intent)
                            finish()

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@loginpage,
                                "Failed to fetch driver info: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            goToMain()
                        }
                    }
                } else {
                    goToMain()
                        }

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            }

            result.onFailure { e ->
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.inpasswordred.visibility = View.VISIBLE
            }
        }

        binding.creercompte.setOnClickListener {
            startActivity(Intent(this, singup::class.java))
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
