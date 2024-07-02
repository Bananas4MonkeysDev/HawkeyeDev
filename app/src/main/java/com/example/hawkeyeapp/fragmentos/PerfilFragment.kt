package com.example.hawkeyeapp.fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.hawkeyeapp.R
import com.example.hawkeyeapp.databinding.FragmentPerfilBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
    }

    private fun setupListeners() {
        binding.ivEditNomb.setOnClickListener {
            binding.tvNombre.isEnabled = true
            binding.btnGuardarCambios.visibility = View.VISIBLE
        }

        binding.ivEditCorre.setOnClickListener {
            binding.tvCorreo.isEnabled = true
            binding.btnGuardarCambios.visibility = View.VISIBLE
        }

        binding.btnGuardarCambios.setOnClickListener {
            val updatedName = binding.tvNombre.text.toString()
            val updatedEmail = binding.tvCorreo.text.toString()
            updateUserData(auth.currentUser?.uid ?: "", updatedName, updatedEmail)
        }

        binding.layoutCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            showSnackbar("Sesión cerrada con éxito")
            activity?.let {
                val navController = Navigation.findNavController(it, R.id.nav_host_fragment)
                navController.navigate(R.id.loginFragment)
            }
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        user?.let { usuario ->
            database.getReference("Pasajeros").child(usuario.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                    val correo = snapshot.child("correo").getValue(String::class.java) ?: ""

                    // Asegúrate de que el binding no es nulo antes de acceder a las vistas
                    _binding?.let { binding ->
                        binding.tvNombre.setText(nombre)
                        binding.tvCorreo.setText(correo)
                    }
                }
                .addOnFailureListener {
                    showSnackbar("Error al cargar datos del usuario")
                }
        }
    }

    private fun updateUserData(userId: String, name: String, email: String) {
        val updates = mapOf(
            "nombre" to name,
            "correo" to email
        )
        database.getReference("Pasajeros").child(userId).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showSnackbar("Datos actualizados correctamente")
                    binding.tvNombre.isEnabled = false
                    binding.tvCorreo.isEnabled = false
                    binding.btnGuardarCambios.visibility = View.GONE
                } else {
                    showSnackbar("Error al actualizar los datos")
                }
            }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        fun newInstance(param1: String, param2: String): PerfilFragment {
            val fragment = PerfilFragment()
            val args = Bundle()
            args.putString("param1", param1)
            args.putString("param2", param2)
            fragment.arguments = args
            return fragment
        }
    }
}
