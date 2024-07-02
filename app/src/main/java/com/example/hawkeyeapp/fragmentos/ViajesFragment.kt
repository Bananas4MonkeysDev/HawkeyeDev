package com.example.hawkeyeapp.fragmentos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hawkeyeapp.databinding.FragmentViajesBinding
import com.example.hawkeyeapp.Model.Viaje
import com.example.hawkeyeapp.adapters.ViajeAdapter
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViajesFragment : Fragment() {

    private var _binding: FragmentViajesBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var viajeAdapter: ViajeAdapter
    private var viajesList = mutableListOf<Viaje>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentViajesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadViajes()
    }

    private fun setupRecyclerView() {
        viajeAdapter = ViajeAdapter(viajesList)
        binding.rvViajes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viajeAdapter
        }
    }

    private fun loadViajes() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance().getReference("Pasajeros/$uid/viajes")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = mutableListOf<Task<DataSnapshot>>()
                for (postSnapshot in snapshot.children) {
                    val viajeId = postSnapshot.key ?: continue
                    tasks.add(FirebaseDatabase.getInstance().getReference("Viajes/$viajeId").get())
                }
                Tasks.whenAllSuccess<DataSnapshot>(tasks).addOnSuccessListener { results ->
                    results.forEach {
                        val viaje = it.getValue(Viaje::class.java)
                        viaje?.let { viajesList.add(it) }
                    }
                    viajeAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ViajesFragment", "Error loading viajes: ${databaseError.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(param1: String, param2: String): ViajesFragment {
            val fragment = ViajesFragment()
            val args = Bundle()
            args.putString("param1", param1)
            args.putString("param2", param2)
            fragment.arguments = args
            return fragment
        }
    }
}
