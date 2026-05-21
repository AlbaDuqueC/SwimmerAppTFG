package com.swimming.app.presentation.equipo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swimming.app.databinding.ItemEventoBinding
import com.swimming.app.domain.model.MarcaDeTiempo

/**
 * Adaptador del RecyclerView que muestra la lista de marcas de tiempo
 * en la pantalla de impresión.
 *
 * Hereda de ListAdapter, que utiliza DiffUtil para actualizar de forma eficiente
 * solo los elementos que han cambiado entre dos listas.
 */
class MarcasAdapter : ListAdapter<MarcaDeTiempo, MarcasAdapter.ViewHolder>(DiffCallback()) {

    /** Representa cada fila visible de la lista y vincula los datos a la vista. */
    inner class ViewHolder(private val binding: ItemEventoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /** Asocia una MarcaDeTiempo a los elementos visuales del item. */
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

    /** Compara elementos para actualizar eficientemente la lista al recibir nuevos datos. */
    class DiffCallback : DiffUtil.ItemCallback<MarcaDeTiempo>() {
        override fun areItemsTheSame(oldItem: MarcaDeTiempo, newItem: MarcaDeTiempo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MarcaDeTiempo, newItem: MarcaDeTiempo) = oldItem == newItem
    }
}