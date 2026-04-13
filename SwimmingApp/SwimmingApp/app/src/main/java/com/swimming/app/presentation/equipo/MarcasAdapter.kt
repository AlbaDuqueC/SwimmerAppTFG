package com.swimming.app.presentation.equipo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swimming.app.databinding.ItemEventoBinding
import com.swimming.app.domain.model.MarcaDeTiempo

/** Adaptador del RecyclerView de marcas de tiempo en la pantalla Imprimir. */
class MarcasAdapter : ListAdapter<MarcaDeTiempo, MarcasAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemEventoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(marca: MarcaDeTiempo) {
            binding.tvLetraInicial.text = marca.descripcion.firstOrNull()?.uppercase() ?: "M"
            binding.tvNombreEvento.text = marca.descripcion
            binding.tvFechaEvento.text = marca.tiempo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val resultado = ViewHolder(binding)
        return resultado
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MarcaDeTiempo>() {
        override fun areItemsTheSame(oldItem: MarcaDeTiempo, newItem: MarcaDeTiempo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MarcaDeTiempo, newItem: MarcaDeTiempo) = oldItem == newItem
    }
}
