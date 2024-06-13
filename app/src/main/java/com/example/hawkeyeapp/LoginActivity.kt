package com.example.hawkeyeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.hawkeyeapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val btnContinuar = findViewById<Button>(R.id.buttonIngresar)
        btnContinuar.setOnClickListener(){navigateConfi()}
    }
    fun navigateConfi(){
        val intent = Intent(this, ConfirmaActivity::class.java)
        startActivity(intent)
    }
}