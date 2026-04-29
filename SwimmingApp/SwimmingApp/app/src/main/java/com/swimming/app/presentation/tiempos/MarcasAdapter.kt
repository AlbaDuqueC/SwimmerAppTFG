package com.swimming.app.presentation.tiempos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swimming.app.databinding.ItemMarcaBinding
import com.swimming.app.domain.model.MarcaDeTiempo

class MarcasAdapter : ListAdapter<MarcaDeTiempo, MarcasAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMarcaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(marca: MarcaDeTiempo) {
            binding.tvDescripcion.text = marca.descripcion
            binding.tvTiempo.text = marca.tiempo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMarcaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<MarcaDeTiempo>() {
        override fun areItemsTheSame(a: MarcaDeTiempo, b: MarcaDeTiempo) = a.id == b.id
        override fun areContentsTheSame(a: MarcaDeTiempo, b: MarcaDeTiempo) = a == b
    }
}