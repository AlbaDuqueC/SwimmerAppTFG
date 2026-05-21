package com.swimming.app.presentation.tiempos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swimming.app.databinding.ItemMarcaBinding
import com.swimming.app.domain.model.MarcaDeTiempo

/**
 * Adaptador del RecyclerView que muestra las marcas de tiempo en la pantalla Tiempos.
 * Usa el formato corto del TiempoFormatter para mostrar los tiempos de forma legible.
 *
 * @param mostrarBotonEliminar si es true, se muestra el botón de papelera en cada fila.
 * @param onEliminarClick callback que se invoca cuando el usuario pulsa el botón de eliminar.
 */
class MarcasAdapter(
    private val mostrarBotonEliminar: Boolean = false,
    private val onEliminarClick: ((MarcaDeTiempo) -> Unit)? = null
) : ListAdapter<MarcaDeTiempo, MarcasAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMarcaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /** Asocia una marca de tiempo a los elementos visuales del item. */
        fun bind(marca: MarcaDeTiempo) {
            binding.tvDescripcion.text = marca.descripcion
            binding.tvTiempo.text = com.swimming.app.utils.TiempoFormatter.aFormatoCorto(marca.tiempo)
            // La letra inicial sale del primer carácter alfabético de la descripción
            // (Mariposa → "M", Crol → "C", etc.).
            binding.tvLetraInicial.text = marca.descripcion
                .firstOrNull { it.isLetter() }?.uppercase() ?: "M"

            // El botón de papelera solo se muestra si el flag está activo.
            // Al pulsarlo, se llama al callback que la pantalla decida.
            binding.btnEliminarMarca.visibility = if (mostrarBotonEliminar) View.VISIBLE else View.GONE
            binding.btnEliminarMarca.setOnClickListener {
                onEliminarClick?.invoke(marca)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMarcaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    /** Compara elementos para actualizar la lista de forma eficiente. */
    class DiffCallback : DiffUtil.ItemCallback<MarcaDeTiempo>() {
        override fun areItemsTheSame(a: MarcaDeTiempo, b: MarcaDeTiempo) = a.id == b.id
        override fun areContentsTheSame(a: MarcaDeTiempo, b: MarcaDeTiempo) = a == b
    }
}