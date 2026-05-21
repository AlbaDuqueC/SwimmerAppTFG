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

/**
 * Pantalla principal de la app.
 * Muestra la vista semanal con los eventos del usuario distribuidos por día,
 * y debajo una lista con los próximos 10 eventos futuros ordenados por fecha.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: EventoAdapter

    // Iniciales de los días de la semana para las cabeceras de las columnas.
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

        // Observa las rutinas del usuario y actualiza la vista semanal y la lista de próximos.
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

    /** Refresca las rutinas al volver a la pantalla. */
    override fun onResume() {
        super.onResume()
        viewModel.cargarRutinas()
    }

    /**
     * Crea las 7 columnas de la vista semanal (una por día), cada una con
     * su cabecera (L, M, X...) y un contenedor donde se apilarán los eventos del día.
     */
    private fun construirSemanaVacia() {
        binding.layoutSemana.removeAllViews()
        val ctx = requireContext()
        for (i in 0..6) {
            val columna = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(4, 4, 4, 4)
            }
            // Cabecera del día (L, M, ...).
            columna.addView(TextView(ctx).apply {
                text = nombresDias[i]
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#0A2A3D"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            // Contenedor vertical donde se apilarán los eventos del día.
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

    /**
     * Inserta los eventos de la semana actual en su columna correspondiente.
     * Si hay varios eventos en un mismo día, se apilan verticalmente.
     */
    private fun rellenarSemana(todas: List<Rutina>) {
        val (inicioSemana, finSemana) = limitesSemanaActual()

        // Vaciar los contenedores antes de rellenarlos para evitar duplicados.
        for (i in 0..6) buscarContenedor(i)?.removeAllViews()

        // Filtrar los eventos de la semana actual y ordenarlos cronológicamente.
        val eventosSemana = todas
            .mapNotNull { rutina -> parsearFecha(rutina.fecha)?.let { rutina to it } }
            .filter { (_, fecha) -> !fecha.before(inicioSemana) && !fecha.after(finSemana) }
            .sortedBy { it.second }

        val ctx = requireContext()
        eventosSemana.forEach { (rutina, fecha) ->
            val cal = Calendar.getInstance().apply { time = fecha }
            // Conversión del día de la semana al índice 0..6 (Lunes=0, Domingo=6).
            val idx = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7)
            val contenedor = buscarContenedor(idx) ?: return@forEach

            val hora = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

            // Tarjeta visual del evento con la hora y el título.
            val tarjeta = TextView(ctx).apply {
                text = "$hora\n${rutina.contenido}"
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                textSize = 9f
                setPadding(4, 6, 4, 6)
                setBackgroundColor(Color.parseColor("#0A2A3D"))
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

    /** Devuelve el contenedor de eventos correspondiente al día indicado. */
    private fun buscarContenedor(index: Int): LinearLayout? {
        val columna = binding.layoutSemana.getChildAt(index) as? LinearLayout ?: return null
        return columna.getChildAt(1) as? LinearLayout
    }

    /**
     * Calcula el lunes a las 00:00 y el domingo a las 23:59:59 de la semana actual.
     * Sirve para filtrar qué eventos pertenecen a esta semana.
     */
    private fun limitesSemanaActual(): Pair<java.util.Date, java.util.Date> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        // Retroceder hasta el lunes más cercano.
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_MONTH, -1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val inicio = cal.time
        cal.add(Calendar.DAY_OF_MONTH, 7)
        cal.add(Calendar.SECOND, -1)
        val fin = cal.time
        return inicio to fin
    }

    /**
     * Parsea una fecha en alguno de los formatos típicos que devuelve la API,
     * probando varios patrones hasta encontrar uno que coincida.
     */
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
        // Último intento: usar solo los primeros 19 caracteres "yyyy-MM-ddTHH:mm:ss".
        return try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).parse(iso.take(19))
        } catch (_: Exception) { null }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Adaptador del RecyclerView de eventos en la pantalla Home.
 * Muestra una lista de próximas rutinas con su fecha formateada.
 */
class EventoAdapter : ListAdapter<Rutina, EventoAdapter.VH>(object : DiffUtil.ItemCallback<Rutina>() {
    override fun areItemsTheSame(a: Rutina, b: Rutina) = a.id == b.id
    override fun areContentsTheSame(a: Rutina, b: Rutina) = a == b
}) {
    inner class VH(private val b: ItemEventoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: Rutina) {
            b.tvLetraInicial.text = r.contenido.firstOrNull()?.uppercase() ?: "E"
            b.tvNombreEvento.text = r.contenido
            // Mostrar la fecha en formato corto y bonito.
            b.tvFechaEvento.text = formatearFechaCorta(r.fecha)
        }

        /** Convierte un ISO completo a un formato compacto "dd/MM HH:mm". */
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