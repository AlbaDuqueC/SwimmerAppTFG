package com.swimming.app.presentation.home

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swimming.app.databinding.FragmentHomeBinding
import com.swimming.app.databinding.ItemEventoBinding
import com.swimming.app.domain.model.Rutina
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: EventoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = EventoAdapter()
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventos.adapter = adapter
        viewModel.rutinas.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> adapter.submitList(result.data)
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
        viewModel.cargarRutinas()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class EventoAdapter : ListAdapter<Rutina, EventoAdapter.VH>(object : DiffUtil.ItemCallback<Rutina>() {
    override fun areItemsTheSame(a: Rutina, b: Rutina) = a.id == b.id
    override fun areContentsTheSame(a: Rutina, b: Rutina) = a == b
}) {
    inner class VH(private val b: ItemEventoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: Rutina) {
            b.tvLetraInicial.text = r.contenido.firstOrNull()?.uppercase() ?: "E"
            b.tvNombreEvento.text = r.contenido
            b.tvFechaEvento.text = r.fecha
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
