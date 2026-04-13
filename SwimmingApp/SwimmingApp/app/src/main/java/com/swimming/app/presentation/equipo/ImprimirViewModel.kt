package com.swimming.app.presentation.equipo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.MarcaDeTiempo
import com.swimming.app.domain.usecase.marcadetiempo.ObtenerMarcasUseCase
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel de la pantalla imprimir. Carga todas las marcas del equipo. */
@HiltViewModel
class ImprimirViewModel @Inject constructor(
    private val obtenerMarcas: ObtenerMarcasUseCase
) : ViewModel() {

    private val _marcas = MutableLiveData<NetworkResult<List<MarcaDeTiempo>>>()
    val marcas: LiveData<NetworkResult<List<MarcaDeTiempo>>> = _marcas

    fun cargarMarcas(idNadadorEquipo: Int) {
        viewModelScope.launch {
            _marcas.value = NetworkResult.Loading
            val resultado = obtenerMarcas(idNadadorEquipo)
            _marcas.value = resultado
        }
    }
}
