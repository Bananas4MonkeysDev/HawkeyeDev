package com.example.hawkeyeapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hawkeyeapp.Model.Viaje
import com.example.hawkeyeapp.R
import com.example.hawkeyeapp.databinding.ItemViajeBinding

class ViajeAdapter(private var viajes: List<Viaje>) : RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViajeViewHolder {
        val binding = ItemViajeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViajeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
        val viaje = viajes[position]
        holder.bind(viaje)
    }

    override fun getItemCount(): Int = viajes.size

    class ViajeViewHolder(private val binding: ItemViajeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(viaje: Viaje) {
            binding.tvOrigen.text = viaje.ubi_ini
            binding.tvDestino.text = viaje.ubi_desti
            binding.placid.text = viaje.placa
            binding.conducid.text = viaje.nombreCondu

            // Ajustar la imagen según el aplicativo
            val context = binding.root.context
            val imageResId = when (viaje.aplicativo) {
                "Sonrisas" -> R.drawable.sonrisas
                "Yango" -> R.drawable.descarga
                "Uber" -> R.drawable.uber
                "inDriver" -> R.drawable.indrive
                else -> R.drawable.viaje // Icono por defecto si no hay coincidencia
            }
            binding.tipoSer.setImageResource(imageResId)
            binding.tipoSer.visibility = View.VISIBLE
        }
    }
}


