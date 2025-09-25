package com.example.nougatbora

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.nougatbora.databinding.ActivityMonstockBinding
import com.example.nougatbora.databinding.ActivitySingupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class monstock : AppCompatActivity() {
    lateinit var binding: ActivityMonstockBinding
    private lateinit var dao: QuantitiesDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonstockBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //back to main
        binding.welilah.setOnClickListener {
            val w = Intent(this@monstock, MainActivity::class.java)
            startActivity(w)
        }


        val db = AppDatabase.DatabaseProvider.getDb(this)
        dao = db.quantitiesDao()

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var stock = dao.getQuantities()
            if (stock == null) {
                // Insert default values if DB empty
                stock = Quantities(
                    id = 1,
                    produita = 0,
                    produitb = 0,
                    produitc = 0,
                    produitd = 0,
                    produite = 0
                )
                dao.insertQuantities(stock)
                stock = dao.getQuantities() ?: stock
            }


            withContext(kotlinx.coroutines.Dispatchers.Main) {

                binding.t1.text = stock.produita.toString()
                binding.t2.text = stock.produitb.toString()
                binding.t3.text = stock.produitc.toString()
                binding.t4.text = stock.produitd.toString()
                binding.t5.text = stock.produite.toString()
            }
        }


        }
    }

