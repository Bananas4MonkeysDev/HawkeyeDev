package com.example.hawkeyeapp.fragmentos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import com.example.hawkeyeapp.Model.Estado
import com.example.hawkeyeapp.Model.Viaje
import com.example.hawkeyeapp.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.sql.Timestamp

class RegiViaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_regi_via, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonIngresar = view.findViewById<MaterialButton>(R.id.buttonIngresar)
        val aplicativoSpinner = view.findViewById<AppCompatSpinner>(R.id.veriText5)
        val placaEditText = view.findViewById<AppCompatEditText>(R.id.veriText3)
        val conductorEditText = view.findViewById<AppCompatEditText>(R.id.veriText4)
        val origenEditText = view.findViewById<AppCompatEditText>(R.id.veriText)
        val destinoEditText = view.findViewById<AppCompatEditText>(R.id.veriText2)

        // Setup the spinner for the ride application
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.aplicativos_array,
            R.layout.spinner_item_custom
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            aplicativoSpinner.adapter = adapter
        }

        buttonIngresar.setOnClickListener {
            val aplicativo = aplicativoSpinner.selectedItem.toString()
            val placa = placaEditText.text.toString()
            val nombreConductor = conductorEditText.text.toString()
            val origen = origenEditText.text.toString()
            val destino = destinoEditText.text.toString()

            if (aplicativo.isNotEmpty() && placa.isNotEmpty() && nombreConductor.isNotEmpty() && origen.isNotEmpty() && destino.isNotEmpty()) {
                registrarViaje(aplicativo, placa, nombreConductor, origen, destino)
            } else {
                Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registrarViaje(aplicativo: String, placa: String, nombreConductor: String, origen: String, destino: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val viajeId = FirebaseDatabase.getInstance().getReference("Viajes").push().key ?: return
            val estadoId = FirebaseDatabase.getInstance().getReference("Estados").push().key ?: return
            val estadoInicial = Estado(estadoId,viajeId, "Normal", Timestamp(System.currentTimeMillis()))
            val estados = hashMapOf(estadoInicial.id to true)

            val viaje = Viaje(viajeId, currentUser.uid, origen, destino, placa, aplicativo, nombreConductor, estados)

            // Guardar viaje y estado inicial
            FirebaseDatabase.getInstance().getReference("Viajes").child(viajeId).setValue(viaje)
                .addOnSuccessListener {
                    FirebaseDatabase.getInstance().getReference("Pasajeros/${currentUser.uid}/viajes").child(viajeId).setValue(true)
                    FirebaseDatabase.getInstance().getReference("Estados").child(estadoInicial.id).setValue(estadoInicial)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Viaje y estado inicial registrados correctamente", Toast.LENGTH_SHORT).show()
                            navigateToUbicacionFragment(viajeId)  // Pasar viajeId al fragmento de ubicación
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al registrar viaje", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToUbicacionFragment(viajeId: String) {
        val fragment = UbicacionFragment.newInstance(viajeId)
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.contenedor_opciones, fragment)
            ?.commit()
    }


    companion object {
        fun newInstance(param1: String, param2: String): RegiViaFragment {
            val fragment = RegiViaFragment()
            val args = Bundle()
            args.putString("param1", param1)
            args.putString("param2", param2)
            fragment.arguments = args
            return fragment
        }
    }
}
