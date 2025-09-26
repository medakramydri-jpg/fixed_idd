package com.example.nougatbora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nougatbora.databinding.ActivityInfodelivreur2Binding
import com.google.gson.Gson
import kotlinx.coroutines.launch

class infodelivreur2 : AppCompatActivity() {
    private lateinit var binding: ActivityInfodelivreur2Binding
    private lateinit var adapter: productsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfodelivreur2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("infodelivreur2", "=== ACTIVITY STARTED ===")

        // Get place name from intent
        val place = intent.extras?.get("place_name")
        binding.textView25.text = place?.toString() ?: "Unknown Place"
        Log.d("infodelivreur2", "Place name: $place")

        // Setup adapter with empty list initially
        adapter = productsAdapter(this, ArrayList())
        binding.recyclerproduct.adapter = adapter
        binding.recyclerproduct.layoutManager = LinearLayoutManager(this)

        // Check if we have products from login flow
        val productsJson = intent.getStringExtra("products_json")
        if (productsJson != null) {
            Log.d("infodelivreur2", "✅ Got products from login flow")
            loadProductsFromIntent(productsJson)
        } else {
            Log.d("infodelivreur2", "⚠️ No products from login, loading from API")
            loadProductsFromAPI()
        }

        // Setup text change listeners
        binding.editText.addTextChangedListener { updateButtonVisibility() }
        binding.editText1.addTextChangedListener { updateButtonVisibility() }
        binding.editText2.addTextChangedListener { updateButtonVisibility() }
        binding.editText3.addTextChangedListener { updateButtonVisibility() }
    }

    private fun loadProductsFromIntent(productsJson: String) {
        try {
            val productArray = Gson().fromJson(productsJson, Array<ProductResponse>::class.java)
            val productList = ArrayList(productArray.toList())

            Log.d("infodelivreur2", "✅ Loaded ${productList.size} products from intent")

            adapter.updateList(productList)
            setupAdapterCallback()
            updateButtonVisibility()

            Toast.makeText(this, "✅ Products loaded from login!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("infodelivreur2", "❌ Error parsing products from intent", e)
            Toast.makeText(this, "Error loading products from login", Toast.LENGTH_SHORT).show()
            loadProductsFromAPI() // Fallback to API
        }
    }

    private fun loadProductsFromAPI() {
        // Get credentials from SharedPreferences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        val userId = prefs.getString("user_id", null)
        val userRole = prefs.getString("user_role", null)

        Log.d("infodelivreur2", "=== DEBUGGING CREDENTIALS ===")
        Log.d("infodelivreur2", "Token exists: ${!token.isNullOrEmpty()}")
        Log.d("infodelivreur2", "Token preview: ${token?.take(30)}...")
        Log.d("infodelivreur2", "User ID: '$userId'")
        Log.d("infodelivreur2", "User Role: '$userRole'")

        // 🔥 SHOW DEBUG TOAST
        val debugInfo = """
            🔍 DEBUG INFO:
            UserID: $userId
            Role: $userRole
            API: api/drivers/$userId
        """.trimIndent()

        Toast.makeText(this, debugInfo, Toast.LENGTH_LONG).show()

        if (token.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Log.e("infodelivreur2", "❌ Missing credentials")
            Toast.makeText(this, "Missing credentials, going to login", Toast.LENGTH_SHORT).show()

            prefs.edit().clear().apply()
            startActivity(Intent(this, loginpage::class.java))
            finish()
            return
        }

        // 🔥 CHECK IF USER ROLE IS ACTUALLY "driver"
        if (userRole != "driver") {
            Log.e("infodelivreur2", "❌ USER IS NOT A DRIVER! Role: '$userRole'")
            Toast.makeText(this, "Error: User role is '$userRole', not 'driver'!", Toast.LENGTH_LONG).show()
            showRoleErrorDialog(userRole)
            return
        }

        // Test the API endpoint with different ID formats
        testApiEndpoint(userId, token)

        try {
            val repository = AuthRepository(ApiClient.authApi)
            val factory = ProductsVMFactory(repository)
            val viewModel = ViewModelProvider(this, factory)[ProductsVM::class.java]

            Toast.makeText(this, "Loading products from API...", Toast.LENGTH_SHORT).show()

            viewModel.getProducts(userId, token)

            // Observe products
            viewModel.products.observe(this) { products ->
                Log.d("infodelivreur2", "✅ SUCCESS! Received ${products.size} products")
                adapter.updateList(ArrayList(products))
                setupAdapterCallback()
                updateButtonVisibility()

                Toast.makeText(this, "✅ Loaded ${products.size} products from API!", Toast.LENGTH_SHORT).show()
            }

            // Observe errors
            viewModel.errorMessage.observe(this) { error ->
                if (error != null) {
                    Log.e("infodelivreur2", "❌ API Error: $error")

                    // 🔥 SHOW DETAILED ERROR TOAST
                    val errorInfo = """
                        ❌ API ERROR:
                        $error
                        
                        UserID: $userId
                        URL: api/drivers/$userId
                    """.trimIndent()

                    Toast.makeText(this, errorInfo, Toast.LENGTH_LONG).show()

                    // If driver not found, suggest solutions
                    if (error.contains("Driver profile not found")) {
                        showDriverNotFoundDialog(userId, userRole)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("infodelivreur2", "❌ Setup error", e)
            Toast.makeText(this, "Setup error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // 🔥 MANUAL API TEST
    private fun testApiEndpoint(userId: String, token: String) {
        lifecycleScope.launch {
            try {
                Log.d("infodelivreur2", "=== TESTING API ENDPOINT ===")

                // Test different ID variations
                val testIds = listOf(
                    userId,                    // Original
                    userId.lowercase(),        // Lowercase
                    userId.uppercase(),        // Uppercase
                    userId.trim(),            // Trimmed
                    "driver_$userId",         // With "driver_" prefix
                    userId.replace("-", ""),  // Remove dashes
                    userId.replace("_", "")   // Remove underscores
                )

                val repository = AuthRepository(ApiClient.authApi)

                for (testId in testIds) {
                    try {
                        Log.d("infodelivreur2", "🧪 Testing ID: '$testId'")
                        val driver = repository.getDriverById(testId, token)

                        Log.d("infodelivreur2", "✅ SUCCESS with ID: '$testId'")
                        Toast.makeText(this@infodelivreur2, "✅ FOUND WORKING ID: '$testId'", Toast.LENGTH_LONG).show()
                        return@launch // Found working ID!

                    } catch (e: retrofit2.HttpException) {
                        Log.d("infodelivreur2", "❌ Failed with ID '$testId': HTTP ${e.code()}")
                    } catch (e: Exception) {
                        Log.d("infodelivreur2", "❌ Failed with ID '$testId': ${e.message}")
                    }
                }

                Log.e("infodelivreur2", "❌ ALL TEST IDs FAILED!")
                Toast.makeText(this@infodelivreur2, "❌ No working ID found. Driver profile definitely missing!", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                Log.e("infodelivreur2", "API test failed", e)
            }
        }
    }

    private fun showRoleErrorDialog(userRole: String?) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Wrong User Role")
            .setMessage("""
                Your account role is '$userRole' but this screen requires 'driver' role.
                
                Please contact support or register a new driver account.
            """.trimIndent())
            .setPositiveButton("Go to Login") { _, _ ->
                getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply()
                startActivity(Intent(this, loginpage::class.java))
                finish()
            }
            .setNegativeButton("Go to Main") { _, _ ->
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showDriverNotFoundDialog(userId: String?, userRole: String?) {
        val message = """
            🚫 DRIVER PROFILE NOT FOUND
            
            User ID: $userId
            Role: $userRole
            
            POSSIBLE CAUSES:
            • User registered but driver profile not created
            • Backend didn't create driver record
            • User ID format mismatch
            • Database sync issue
            
            SOLUTIONS:
            1. Check backend database
            2. Re-register as driver
            3. Contact app admin
            
            Try the URL manually:
            https://warehouse-backend-ru6r.onrender.com/api/drivers/$userId
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Driver Profile Missing")
            .setMessage(message)
            .setPositiveButton("Go to Login") { _, _ ->
                // Clear session and go to login
                getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply()
                startActivity(Intent(this, loginpage::class.java))
                finish()
            }
            .setNegativeButton("Go to Main") { _, _ ->
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .setNeutralButton("Try Again") { _, _ ->
                // Retry loading
                loadProductsFromAPI()
            }
            .setCancelable(false)
            .show()
    }

    private fun setupAdapterCallback() {
        adapter.onProductCountChanged = {
            updateButtonVisibility()
        }
    }

    private fun updateButtonVisibility() {
        val text1NotEmpty = !binding.editText.text.isNullOrBlank()
        val text2NotEmpty = !binding.editText1.text.isNullOrBlank()
        val text3NotEmpty = !binding.editText2.text.isNullOrBlank()
        val text4NotEmpty = !binding.editText3.text.isNullOrBlank()

        // Check if any products are selected
        val atLeastOneProductSelected = if (::adapter.isInitialized) {
            adapter.currentData.any { it.count > 0 }
        } else {
            false
        }

        val shouldShowScan = text1NotEmpty && text2NotEmpty && text3NotEmpty && text4NotEmpty && atLeastOneProductSelected

        binding.scan.visibility = if (shouldShowScan) View.VISIBLE else View.GONE

        Log.d("infodelivreur2", "Button visibility: $shouldShowScan")
        Log.d("infodelivreur2", "  Fields filled: $text1NotEmpty, $text2NotEmpty, $text3NotEmpty, $text4NotEmpty")
        Log.d("infodelivreur2", "  Products selected: $atLeastOneProductSelected")
    }
}