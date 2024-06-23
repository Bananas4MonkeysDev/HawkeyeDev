package com.example.hawkeyeapp.fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hawkeyeapp.databinding.FragmentCompletarRegistroBinding
import com.example.hawkeyeapp.Model.Cliente
import com.example.hawkeyeapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
class CompletarRegistroFragment : Fragment() {
    private var _binding: FragmentCompletarRegistroBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletarRegistroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Pasajeros")

        binding.buttonIngresar.setOnClickListener {
            val name = binding.veriText.text.toString().trim()
            val email = binding.veriText2.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(context, "Por favor, ingrese Nombre completo", Toast.LENGTH_SHORT).show()
            } else {
                val user = auth.currentUser
                if (user != null) {
                    val cliente = Cliente(user.uid, user.phoneNumber ?: "", name, email)
                    database.child(user.uid).setValue(cliente).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Registro Completo", Toast.LENGTH_SHORT).show()
                            navigateToHomeFragment()
                        } else {
                            Toast.makeText(context, "Error al registrar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToHomeFragment() {
        findNavController().navigate(R.id.action_completarRegistroFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
