package com.swimming.app.presentation.equipo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swimming.app.databinding.ItemEventoBinding
import com.swimming.app.domain.model.NadadorEquipo

/** Adaptador del RecyclerView de nadadores en la pantalla de Equipo. */
class NadadorEquipoAdapter : ListAdapter<NadadorEquipo, NadadorEquipoAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemEventoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(nadador: NadadorEquipo) {
            binding.tvLetraInicial.text = nadador.nombre.firstOrNull()?.uppercase() ?: "N"
            binding.tvNombreEvento.text = "${nadador.nombre} ${nadador.apellidos}"
            binding.tvFechaEvento.text = "Código: ${nadador.codigo}"
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

    class DiffCallback : DiffUtil.ItemCallback<NadadorEquipo>() {
        override fun areItemsTheSame(oldItem: NadadorEquipo, newItem: NadadorEquipo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NadadorEquipo, newItem: NadadorEquipo) = oldItem == newItem
    }
}
