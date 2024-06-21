package com.example.hawkeyeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        auth = FirebaseAuth.getInstance()

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        setupBottomNavigationView()

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.miviaje // Selecciona 'Mi Viaje' por defecto
        } else {
            // Restaurar el fragmento actual basado en el ítem seleccionado
            val currentItem = bottomNavigationView.selectedItemId
            bottomNavigationView.selectedItemId = currentItem // Esto volverá a cargar el fragmento correcto
        }
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.miviaje -> RegiViaFragment.newInstance("", "")
                R.id.historial -> ViajesFragment.newInstance("", "")
                R.id.page_2 -> PerfilFragment.newInstance("", "")
                else -> null
            }
            fragment?.let {
                loadFragment(it)
                return@setOnNavigationItemSelectedListener true
            } ?: false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        if (fragment.javaClass != currentFragment?.javaClass) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor_opciones, fragment)
                .commit()
            currentFragment = fragment
        }
    }
}
