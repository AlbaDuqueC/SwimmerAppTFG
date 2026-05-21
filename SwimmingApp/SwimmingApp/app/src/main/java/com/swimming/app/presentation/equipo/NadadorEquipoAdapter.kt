package com.swimming.app.presentation.equipo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swimming.app.databinding.ItemEventoBinding
import com.swimming.app.domain.model.NadadorEquipo

/**
 * Adaptador del RecyclerView que muestra la lista de nadadores de un equipo.
 *
 * @param mostrarBotones si es true, se muestran los botones de editar y eliminar.
 * @param onClick callback al pulsar sobre el item completo.
 * @param onEditarClick callback al pulsar el botón de editar.
 * @param onEliminarClick callback al pulsar el botón de eliminar.
 */
class NadadorEquipoAdapter(
    private val mostrarBotones: Boolean = false,
    private val onClick: ((NadadorEquipo) -> Unit)? = null,
    private val onEditarClick: ((NadadorEquipo) -> Unit)? = null,
    private val onEliminarClick: ((NadadorEquipo) -> Unit)? = null
) : ListAdapter<NadadorEquipo, NadadorEquipoAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemEventoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(nadador: NadadorEquipo) {
            binding.tvLetraInicial.text = nadador.nombre.firstOrNull()?.uppercase() ?: "N"
            binding.tvNombreEvento.text = "${nadador.nombre} ${nadador.apellidos}"
            binding.tvFechaEvento.text = "Código: ${nadador.codigo}"

            binding.root.setOnClickListener { onClick?.invoke(nadador) }

            // Botones de editar y eliminar: solo visibles si el flag está activo.
            val visibilidadBotones = if (mostrarBotones) View.VISIBLE else View.GONE
            binding.btnEditar.visibility = visibilidadBotones
            binding.btnEliminar.visibility = visibilidadBotones

            binding.btnEditar.setOnClickListener { onEditarClick?.invoke(nadador) }
            binding.btnEliminar.setOnClickListener { onEliminarClick?.invoke(nadador) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<NadadorEquipo>() {
        override fun areItemsTheSame(oldItem: NadadorEquipo, newItem: NadadorEquipo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NadadorEquipo, newItem: NadadorEquipo) = oldItem == newItem
    }
}