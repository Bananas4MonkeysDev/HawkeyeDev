package com.example.hawkeyeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import com.example.hawkeyeapp.Model.Cliente
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CompletarRegistroActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var nameEditText: AppCompatEditText
    private lateinit var emailEditText: AppCompatEditText
    private lateinit var registerButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completar_registro)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Pasajeros")
        nameEditText = findViewById(R.id.veriText)
        emailEditText = findViewById(R.id.veriText2)
        registerButton = findViewById(R.id.buttonIngresar)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese Nombre completo", Toast.LENGTH_SHORT).show()
            } else {
                val user = auth.currentUser
                if (user != null) {
                    val cliente = Cliente(user.uid, user.phoneNumber ?: "", name, email)
                    database.child(user.uid).setValue(cliente).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Registro Completo", Toast.LENGTH_SHORT).show()
                            // Navigate to HomeActivity
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}