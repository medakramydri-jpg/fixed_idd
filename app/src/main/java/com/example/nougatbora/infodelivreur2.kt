package com.example.nougatbora

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nougatbora.databinding.ActivityInfodelivreur2Binding
import com.google.gson.Gson

class infodelivreur2 : AppCompatActivity() {
    private lateinit var binding: ActivityInfodelivreur2Binding
    private lateinit var VM: ProductsVM
    private lateinit var adapter: productsAdapter  // ✅ global adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfodelivreur2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Place name from intent
        val place = intent.extras?.get("place_name")
        binding.textView25.text = place?.toString() ?: "Unknown Place"

        // ✅ Setup adapter once (convert to ArrayList!)
        val productsJson = intent.getStringExtra("products_json")
        val productList: ArrayList<ProductResponse> = if (productsJson != null) {
            val arr = Gson().fromJson(productsJson, Array<ProductResponse>::class.java)
            ArrayList(arr.toList())   // ✅ now an ArrayList<ProductResponse>
        } else {
            ArrayList()
        }

        adapter = productsAdapter(this, productList)
        binding.recyclerproduct.adapter = adapter
        binding.recyclerproduct.layoutManager = LinearLayoutManager(this)

        // ✅ Get saved credentials
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        val userId = prefs.getString("user_id", null)

        if (token != null && userId != null) {
            // ✅ Init ViewModel and load products for this driver
            VM = ViewModelProvider(this)[ProductsVM::class.java]
            VM.getProducts(userId, token)

            VM.products.observe(this) { data ->
                adapter.updateList(ArrayList(data))  // ✅ convert List -> ArrayList
                updateButtonVisibility()
                adapter.onProductCountChanged = { updateButtonVisibility() }
            }
        } else {
            Toast.makeText(this, "Missing token or userId", Toast.LENGTH_SHORT).show()
        }

        // ✅ Watch text fields for scan button visibility
        binding.editText.addTextChangedListener { updateButtonVisibility() }
        binding.editText1.addTextChangedListener { updateButtonVisibility() }
        binding.editText2.addTextChangedListener { updateButtonVisibility() }
        binding.editText3.addTextChangedListener { updateButtonVisibility() }
    }

    private fun updateButtonVisibility() {
        val text1NotEmpty = !binding.editText.text.isNullOrBlank()
        val text2NotEmpty = !binding.editText1.text.isNullOrBlank()
        val text3NotEmpty = !binding.editText2.text.isNullOrBlank()
        val text4NotEmpty = !binding.editText3.text.isNullOrBlank()

        // ✅ Ask adapter for product counts
        val atLeastOneProductSelected = adapter.currentData.any { it.count > 0 }

        binding.scan.visibility = if (
            text1NotEmpty && text2NotEmpty && text3NotEmpty && text4NotEmpty && atLeastOneProductSelected
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}
