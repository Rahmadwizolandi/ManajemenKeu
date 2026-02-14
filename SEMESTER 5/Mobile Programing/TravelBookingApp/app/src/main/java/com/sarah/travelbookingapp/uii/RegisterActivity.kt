package com.sarah.travelbookingapp.uii

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sarah.travelbookingapp.database.AppDatabase
import com.sarah.travelbookingapp.database.User
import com.sarah.travelbookingapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)

        binding.btnRegister.setOnClickListener {
            val fullname = binding.etFullname.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (fullname.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val existingUser = db.appDao().getUserByUsername(username)
                    if (existingUser == null) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val currentDate = sdf.format(Date())
                        val user = User(username = username, password = password, fullname = fullname, registrationDate = currentDate)
                        db.appDao().insertUser(user)
                        Toast.makeText(this@RegisterActivity, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Nama pengguna sudah ada", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@RegisterActivity, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            }
        }
    }
}