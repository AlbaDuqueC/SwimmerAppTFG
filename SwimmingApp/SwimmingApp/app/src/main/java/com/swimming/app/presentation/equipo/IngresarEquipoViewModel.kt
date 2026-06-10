package com.swimming.app.presentation.equipo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swimming.app.domain.model.Nadador
import com.swimming.app.domain.usecase.nadador.VincularNadadorUseCase
import com.swimming.app.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la pantalla de ingresar a un equipo.
 * Gestiona la vinculación de un nadador a un equipo mediante el código de 6 dígitos
 * que le proporciona su entrenador.
 */
@HiltViewModel
class IngresarEquipoViewModel @Inject constructor(
    private val vincularNadador: VincularNadadorUseCase
) : ViewModel() {

    // Resultado de la vinculación, observado por la UI para navegar o mostrar error.
    private val _resultado = MutableLiveData<NetworkResult<Nadador>>()
    val resultado: LiveData<NetworkResult<Nadador>> = _resultado

    /**
     * Intenta vincular al nadador con el equipo usando el código introducido.
     * Si el código no existe o ya está ocupado, la API devuelve un error
     * que se propaga al LiveData para que la UI lo muestre.
     */
    fun vincular(idNadador: Int, codigo: Int) {
        viewModelScope.launch {
            _resultado.value = NetworkResult.Loading
            _resultado.value = vincularNadador(idNadador, codigo)
        }
    }
}
