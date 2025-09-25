/*package com.example.nougatbora

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.nougatbora.AppDatabase.DatabaseProvider
import com.example.nougatbora.databinding.ActivityInfodelivreurBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Infodelivreur : AppCompatActivity() {
    lateinit var binding: ActivityInfodelivreurBinding
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private lateinit var dao: QuantitiesDAO

    private val textWatcher = object : TextWatcher {
        // Correct implementation with all required methods
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No action needed
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // No action needed
        }

        override fun afterTextChanged(s: Editable?) {
            activatescan() // This is where we trigger our logic
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1
        private const val CAMERA_REQUEST_CODE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInfodelivreurBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val place = intent.extras!!.get("place_name")
        binding.textView25.text = place.toString()
        // + and - bottons logic

        // control appearing of scan botton
        // pay attention here !!!!!
        binding.editText.addTextChangedListener(textWatcher)
        binding.editText1.addTextChangedListener(textWatcher)


        //go to cam clicking on scan, and hide it and show the other 2 bottons
        binding.scan.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                openCamera()
            }
            else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE

                )
            }
            binding.scan.visibility = View.INVISIBLE
            binding.slide.visibility = View.VISIBLE
        }

        val pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                binding.recu.setImageURI(uri) // preview
            }
        }

        binding.recu.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val db = AppDatabase.DatabaseProvider.getDb(this)
        dao = db.quantitiesDao()

        // jump to main activity when sliding
        binding.slide.setOnClickListener {
            // Get all values from the UI
            val number1 = binding.t1.text.toString().toInt()
            val number2 = binding.t2.text.toString().toInt()
            val number3 = binding.t3.text.toString().toInt()
            val number4 = binding.t4.text.toString().toInt()
            val number5 = binding.t5.text.toString().toInt()
            val kolechInput = number1 + number2 + number3 + number4 + number5
            val currentDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            // Create the delivery object (but don't save it yet!)
            val delivery = Model(
                localisationn = binding.textView25.text.toString(),
                groceryy = binding.editText.text.toString(),
                recunumberr = binding.editText1.text.toString(),
                kolechh = kolechInput.toString(),
                produit1 = binding.t1.text.toString(),
                produitb2 = binding.t2.text.toString(),
                produitc3 = binding.t3.text.toString(),
                produitd4 = binding.t4.text.toString(),
                produit5 = binding.t5.text.toString(),
                recuu = selectedImageUri.toString(),
                dateTime = currentDateTime
            )



            lifecycleScope.launch(Dispatchers.IO) {
                // Get the current stock ONCE
                val currentStock = dao.getQuantities() ?: return@launch

                // Try to process the delivery and update stock
                val wasSuccessful = dao.processDeliveryAndUpdateStock(
                    number1, number2, number3, number4, number5, currentStock
                )

                withContext(Dispatchers.Main) {
                    if (wasSuccessful) {
                        //  ONLY NOW save the delivery record and navigate
                        lifecycleScope.launch(Dispatchers.IO) {
                            DatabaseProvider.getDb(this@Infodelivreur)
                                .deliveryDao()
                                .insert(delivery)
                        }
                        DeliveryCounter.increment(this@Infodelivreur)
                        // Then proceed with your navigation and success logic
                        Handler(Looper.getMainLooper()).postDelayed({
                            val i = Intent(this@Infodelivreur, MainActivity::class.java)
                            startActivity(i)
                        }, 1500)

                    } else {
                    // CANCEL EVERYTHING
                    // Show an error message, do NOT navigate, do NOT save the delivery

                    // 1. Inflate the custom layout FIRST
                    val dialogView = layoutInflater.inflate(R.layout.stockinsuffisant, null)

                    // 2. Find the button in the INFLATED VIEW (this is safe)
                    val btn = dialogView.findViewById<LinearLayout>(R.id.ok) // Or Button, if it's a Button
                    btn.setOnClickListener {
                        val i = Intent(this@Infodelivreur, monstock::class.java)
                        startActivity(i)
                    }

                    // 3. Now build the dialog with the already-prepared view
                    val dialog = AlertDialog.Builder(this@Infodelivreur)
                        .setView(dialogView) // Use the inflated view here
                        .setCancelable(false) // Prevent dismissing by tapping outside
                        .create()

                    dialog.show()
                }
                // The activity stays open, the user can correct the quantities
                }
            }
        }
        // make je suis arrivé disappear when all conters are 0
        if (binding.t1.text == "0" && binding.t2.text == "0" && binding.t3.text == "0" &&
            binding.t4.text == "0" && binding.t5.text == "0"){
            binding.slide.visibility = View.INVISIBLE
        }

    }

    // control appearing of scan botton
    private fun activatescan() {
        if (binding.editText.text.isNotEmpty() && binding.editText1.text.isNotEmpty()) {
            if (binding.t1.text.toString() != "0" || binding.t2.text.toString() != "0" || binding.t3.text.toString() != "0"
                || binding.t4.text.toString() != "0" || binding.t5.text.toString() != "0"
            ) {
                binding.scan.visibility = View.VISIBLE
            } else {
                binding.scan.visibility = View.INVISIBLE
            }
        } else {
            binding.scan.visibility = View.INVISIBLE
        }
    }
    //CAM permisssion
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(this,
                    "oops vous avez désactiver la permission de la camera." +
                "Vous pouvez activez la permission de la camera dans les parametres",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun openCamera() {
        val photoFile = File.createTempFile("receipt_", ".jpg", cacheDir)
        cameraImageUri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedImageUri = cameraImageUri
            binding.recu.setImageURI(selectedImageUri)
        }


    }
    private fun updateArriveButtonVisibility() {
        val q1 = binding.t1.text.toString().toIntOrNull() ?: 0
        val q2 = binding.t2.text.toString().toIntOrNull() ?: 0
        val q3 = binding.t3.text.toString().toIntOrNull() ?: 0
        val q4 = binding.t4.text.toString().toIntOrNull() ?: 0
        val q5 = binding.t5.text.toString().toIntOrNull() ?: 0

        val total = q1 + q2 + q3 + q4 + q5

        // Only show the button if there is at least one product
        if (total == 0) {
            binding.slide.visibility = View.INVISIBLE
        }
    }

} */