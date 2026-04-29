package com.swimming.app.presentation.home

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.swimming.app.R
import com.swimming.app.databinding.FragmentHomeBinding
import com.swimming.app.databinding.ItemEventoBinding
import com.swimming.app.domain.model.Rutina
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: EventoAdapter

    private val nombresDias = listOf("L", "M", "X", "J", "V", "S", "D")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventoAdapter()
        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEventos.adapter = adapter

        construirSemanaVacia()

        viewModel.rutinas.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val ahora = Calendar.getInstance().time
                    val futuros = result.data.filter { parsearFecha(it.fecha)?.after(ahora) ?: false }
                    adapter.submitList(futuros.take(10))
                    rellenarSemana(result.data)
                }
                is NetworkResult.Error -> Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }

        viewModel.cargarRutinas()


    }

    override fun onResume() {
        super.onResume()
        viewModel.cargarRutinas()
    }

    /** Crea 7 columnas con su cabecera (L, M, X...) y un contenedor vacío que apila los eventos del día. */
    private fun construirSemanaVacia() {
        binding.layoutSemana.removeAllViews()
        val ctx = requireContext()
        for (i in 0..6) {
            val columna = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(4, 4, 4, 4)
            }
            // Cabecera del día (L, M, ...)
            columna.addView(TextView(ctx).apply {
                text = nombresDias[i]
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#0A2A3D"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            // Contenedor vertical donde se apilarán los eventos del día
            columna.addView(LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                tag = "contenedor_$i"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })
            binding.layoutSemana.addView(columna)
        }
    }


    /** Pone los eventos de la semana actual en su columna correspondiente. Apila múltiples eventos del mismo día. */
    private fun rellenarSemana(todas: List<Rutina>) {
        val (inicioSemana, finSemana) = limitesSemanaActual()

        // Limpiar contenedores previos
        for (i in 0..6) buscarContenedor(i)?.removeAllViews()

        // Filtrar y ordenar los eventos de esta semana por fecha (los más tempranos primero)
        val eventosSemana = todas
            .mapNotNull { rutina -> parsearFecha(rutina.fecha)?.let { rutina to it } }
            .filter { (_, fecha) -> !fecha.before(inicioSemana) && !fecha.after(finSemana) }
            .sortedBy { it.second }

        val ctx = requireContext()
        eventosSemana.forEach { (rutina, fecha) ->
            val cal = Calendar.getInstance().apply { time = fecha }
            val idx = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) // L=0..D=6
            val contenedor = buscarContenedor(idx) ?: return@forEach

            val hora = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

            val tarjeta = TextView(ctx).apply {
                text = "$hora\n${rutina.contenido}"
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                textSize = 9f
                setPadding(4, 6, 4, 6)
                setBackgroundColor(Color.parseColor("#0A2A3D"))
                // Margen entre tarjetas del mismo día
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 0, 0, 4)
                layoutParams = lp
            }
            contenedor.addView(tarjeta)
        }
    }

    private fun buscarContenedor(index: Int): LinearLayout? {
        val columna = binding.layoutSemana.getChildAt(index) as? LinearLayout ?: return null
        return columna.getChildAt(1) as? LinearLayout
    }

    private fun limitesSemanaActual(): Pair<java.util.Date, java.util.Date> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        // Ir al lunes 00:00:00
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_MONTH, -1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val inicio = cal.time
        cal.add(Calendar.DAY_OF_MONTH, 7)
        cal.add(Calendar.SECOND, -1)
        val fin = cal.time
        return inicio to fin
    }

    /** Parsea fechas en formatos típicos de la API (con o sin Z, con o sin fracción de segundo). */
    private fun parsearFecha(iso: String): java.util.Date? {
        val intentos = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (patron in intentos) {
            try {
                return java.text.SimpleDateFormat(patron, java.util.Locale.US).parse(iso)
            } catch (_: Exception) {}
        }
        // Último intento: solo los 19 primeros caracteres "yyyy-MM-ddTHH:mm:ss"
        return try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).parse(iso.take(19))
        } catch (_: Exception) { null }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class EventoAdapter : ListAdapter<Rutina, EventoAdapter.VH>(object : DiffUtil.ItemCallback<Rutina>() {
    override fun areItemsTheSame(a: Rutina, b: Rutina) = a.id == b.id
    override fun areContentsTheSame(a: Rutina, b: Rutina) = a == b
}) {
    inner class VH(private val b: ItemEventoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: Rutina) {
            b.tvLetraInicial.text = r.contenido.firstOrNull()?.uppercase() ?: "E"
            b.tvNombreEvento.text = r.contenido
            // Mostrar fecha bonita: "dd/MM HH:mm"
            b.tvFechaEvento.text = formatearFechaCorta(r.fecha)
        }
        private fun formatearFechaCorta(iso: String): String = try {
            val fecha = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                .parse(iso.take(19))
            java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(fecha!!)
        } catch (_: Exception) { iso }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemEventoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}