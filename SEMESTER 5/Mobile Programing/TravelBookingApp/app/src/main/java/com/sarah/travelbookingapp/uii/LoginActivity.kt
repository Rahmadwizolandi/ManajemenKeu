package com.sarah.travelbookingapp.uii

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sarah.travelbookingapp.database.AppDatabase
import com.sarah.travelbookingapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // animasi fade-in
        binding.root.alpha = 0f
        binding.root.animate().alpha(1f).setDuration(1000)

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val db = AppDatabase.getDatabase(this)
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val user = db.appDao().login(username, password)
                    if (user != null) {
                        // Save user ID to SharedPreferences
                        with(sharedPreferences.edit()) {
                            putInt("LOGGED_IN_USER_ID", user.id)
                            apply()
                        }

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Nama pengguna atau kata sandi salah", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@LoginActivity, "Harap masukkan nama pengguna dan kata sandi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}