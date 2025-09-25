package com.example.nougatbora

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.example.nougatbora.databinding.ActivityProfileBinding
import androidx.activity.result.contract.ActivityResultContracts


class profile : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    private lateinit var pickimagelauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // cases of clicking on enregistrer botton
        binding.register.setOnClickListener {
            // text saying u didnt change anything
            if (binding.tx1.text.toString() == binding.tx1.hint.toString() && binding.tx3.text.toString() == binding.tx3.hint.toString()) {
                binding.ri.visibility = View.VISIBLE
            } else { binding.ri.visibility = View.INVISIBLE}
            // username has to be > 6
            if (binding.tx1.text.length < 6) {
            binding.nomred.visibility = View.VISIBLE
            } else { binding.nomred.visibility = View.INVISIBLE}
            // phone number has to be = 10
            if ( binding.tx3.text.length != 10){
            binding.phonered.visibility = View.VISIBLE
           }else { binding.phonered.visibility = View.INVISIBLE}
            // if change my password is already clicked say that password must be equals and >6
            if ( binding.cont1.visibility == View.VISIBLE && binding.cont2.visibility == View.VISIBLE) {

                if (binding.tx5.text.length < 6 || binding.tx6.text.length < 6) {
                    binding.passwordred.visibility = View.VISIBLE
                } else {
                    binding.passwordred.visibility = View.INVISIBLE
                }

                if (binding.tx5.text != binding.tx6.text) {
                    binding.passwordrepeated.visibility = View.VISIBLE
                } else {
                    binding.passwordrepeated.visibility = View.INVISIBLE
                }
            }
              // infos really changed case
            if( (binding.tx1.text.toString() != binding.tx1.hint.toString() && binding.tx1.text.length > 6) ||
                (binding.tx3.text.toString() != binding.tx3.hint.toString() && binding.tx3.text.length == 10)){
              // show dialog
                val d = Dialog(this)
                d.setContentView(R.layout.passwordupdated)
                d.window!!.setBackgroundDrawableResource(R.color.transparent)
                d.window!!.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                d.show()
                binding.tx1.hint = binding.tx1.text.toString()
                binding.tx3.hint = binding.tx3.text.toString()
            }
            // infos and password changed
            if (binding.cont1.visibility == View.VISIBLE) {
                val tx1Valid = binding.tx1.text.toString() != binding.tx1.hint.toString() && binding.tx1.text.length > 6
                val tx3Valid = binding.tx3.text.toString() != binding.tx3.hint.toString() && binding.tx3.text.length == 10
                val passwordsValid = binding.tx5.text.length > 6 &&
                        binding.tx6.text.length > 6 &&
                        binding.tx5.text.toString() == binding.tx6.text.toString()

                // Allow when:
                // 1) Passwords alone are valid
                // 2) Passwords valid + tx1 or tx3 valid
                if (passwordsValid && (tx1Valid || tx3Valid || true)) {
                    val a = Dialog(this)
                    a.setContentView(R.layout.infosupdated)
                    a.window!!.setBackgroundDrawableResource(R.color.transparent)
                    a.window!!.setLayout(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    a.show()

                    // Update hints so same value is not counted as "new" next time
                    binding.tx1.hint = binding.tx1.text.toString()
                    binding.tx3.hint = binding.tx3.text.toString()
                }
            }

        }
        // want to change password botton
        binding.pass.setOnClickListener {
            if (binding.cont1.visibility == View.VISIBLE && binding.cont1.visibility == View.VISIBLE) {
                // Hide them
                binding.cont1.visibility = View.INVISIBLE
                binding.cont2.visibility = View.INVISIBLE
                binding.change.text = "Changer le mot de pass?"
                binding.tx5.text.clear()
                binding.tx6.text.clear()
            } else {
                // Show them
                binding.cont1.visibility = View.VISIBLE
                binding.cont2.visibility = View.VISIBLE
                binding.change.text = "Non Je Change pas le mot de pass"
            }
        }
        // back botton and send pic with it
        binding.weli.setOnClickListener {
            val i = Intent(this@profile, MainActivity::class.java)
            i.putExtra("imageuri", selectedImageUri.toString())
            startActivity(i)

        }
        // take image from gallery



        binding.deconnect.setOnClickListener {
            val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val token = sharedPrefs.getString("jwt_token", null)

            if (token != null) {
                // (Optional) Call API logout
                val repository = AuthRepository(ApiClient.authApi)
                // You can launch coroutine here if you want to call repository.logout(token)

                // 🔹 Clear local token
                sharedPrefs.edit().remove("jwt_token").apply()

                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

                // Go back to login
                startActivity(Intent(this, loginpage::class.java))
                finish()
            } else {
                Toast.makeText(this, "No active session", Toast.LENGTH_SHORT).show()
            }
        }






    }
}
