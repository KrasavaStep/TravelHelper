package com.example.travelhelper.data.network.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.travelhelper.MainActivity
import com.example.travelhelper.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.registerBtn.setOnClickListener {
            binding.registerBtn.isEnabled = false

            val email = binding.emailEt.text.toString()
            val password = binding.passwordEt.text.toString()
            val username = binding.usernameEt.text.toString()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Поля не могут быть пустыми", Toast.LENGTH_SHORT).show()
                binding.registerBtn.isEnabled = true
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    runOnUiThread {
                        if (task.isSuccessful) {
                            val currentUser = auth.currentUser
                            val userId = currentUser?.uid

                            if (userId != null) {
                                val userInfo = hashMapOf(
                                    "email" to email,
                                    "username" to username
                                )

                                database.reference.child("Users").child(userId).setValue(userInfo)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Регистрация прошла успешно",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finishAffinity() // Закрывает все предыдущие экраны
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Ошибка сохранения данных: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        auth.signOut()
                                        binding.registerBtn.isEnabled = true
                                    }
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Ошибка регистрации: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.registerBtn.isEnabled = true
                        }
                    }
                }
        }
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.goToLoginActivityTv.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
