package com.example.hawkeyeapp.fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hawkeyeapp.R
import com.example.hawkeyeapp.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var currentFragment: Fragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigationView(binding.bottomNavigation)

        // Restaurar el fragmento actual basado en el ítem seleccionado
        savedInstanceState?.let {
            val currentItem = binding.bottomNavigation.selectedItemId
            binding.bottomNavigation.selectedItemId = currentItem
        } ?: run {
            binding.bottomNavigation.selectedItemId =
                R.id.miviaje // Selecciona 'Mi Viaje' por defecto
        }
    }

    private fun setupBottomNavigationView(bottomNavigationView: BottomNavigationView) {
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
        childFragmentManager.beginTransaction()
            .replace(R.id.contenedor_opciones, fragment)
            .commit()
        currentFragment = fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
