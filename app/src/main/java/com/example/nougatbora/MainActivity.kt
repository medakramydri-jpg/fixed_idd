package com.example.nougatbora

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nougatbora.databinding.ActivityMainBinding
import com.example.nougatbora.listmelivraison

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
   // permission pour les notification


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //counter +1
        updateCounter()


        // botton to go maps
        binding.cardstart.setOnClickListener {
            val i = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(i)
        }
        // recieve profile pic from profile activity
        val data = intent.getStringExtra("imageuri")
        if (data != null) {
            val uri = Uri.parse(data)
            binding.profilepic.setImageURI(uri)
        }
        // go to profile activity
        binding.profilepic.setOnClickListener {
            val o = Intent(this@MainActivity, profile::class.java)
            startActivity(o)
        }
        // go to list des courses
        binding.edini.setOnClickListener {
            val p = Intent(this@MainActivity, listmelivraison::class.java)
            startActivity(p)
        }
        //ask for notification permission
        askNotificationPermission()
        // go to my stock
        binding.storage.setOnClickListener {
            val o = Intent(this@MainActivity, monstock::class.java)
            startActivity(o)
        }
    }
    private fun updateCounter() {
        val count = DeliveryCounter.getCount(this)
        binding.countertext.text = count.toString()
    }
    override fun onResume() {
        super.onResume()
        // Refresh counter when coming back from DeliveryActivity
        updateCounter()
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted → show notification
            showNotification()
        } else {
            // Permission denied → handle gracefully
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Already granted
                showNotification()
            }
        } else {
            // For Android 10–12 → no runtime permission needed
            showNotification()
        }
    }

    private fun showNotification() {
        // Create notification channel if needed (API 26+)
        // Build & show your notification here
    }
}

