package com.example.nougatbora

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nougatbora.databinding.ActivityListmelivraisonBinding

class listmelivraison : AppCompatActivity() {
    lateinit var binding: ActivityListmelivraisonBinding
    private lateinit var viewModel: DeliveryViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListmelivraisonBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(DeliveryViewModel::class.java)
        val adapter = Adapterta3na(this, arrayListOf())
        binding.recyclerviewta3na.adapter = adapter


        val manager = LinearLayoutManager(this)
        binding.recyclerviewta3na.layoutManager = manager

        // Observe data from ViewModel (LiveData or Flow)
        viewModel.allDeliveries.observe(this) { deliveries ->
            adapter.updateData(deliveries) // you’ll add updateData() in adapter
        }

        binding.retour.setOnClickListener {
            val p = Intent(this@listmelivraison, MainActivity::class.java)
            startActivity(p)
        }

    }
}