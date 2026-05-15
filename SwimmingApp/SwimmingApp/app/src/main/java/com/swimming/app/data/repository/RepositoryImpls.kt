package com.swimming.app.data.repository

import com.swimming.app.data.dto.*
import com.swimming.app.data.local.dao.*
import com.swimming.app.data.mapper.*
import com.swimming.app.data.network.ApiService
import com.swimming.app.domain.model.*
import com.swimming.app.domain.repository.*
import com.swimming.app.utils.NetworkChecker
import com.swimming.app.utils.NetworkResult
import javax.inject.Inject

class NadadorRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: NadadorDao,
    private val networkChecker: NetworkChecker
) : NadadorRepository {

    override suspend fun obtenerNadador(id: Int): NetworkResult<Nadador> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerNadador(id)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val dto = response.body()!!.datos!!
                    dao.insertar(dto.toEntity())
                    NetworkResult.Success(dto.toDomain())
                } else {
                    val local = dao.obtenerPorId(id)
                    if (local != null) NetworkResult.Success(local.toDomain())
                    else NetworkResult.Error(response.body()?.mensaje ?: "Error al obtener nadador")
                }
            } catch (e: Exception) {
                val local = dao.obtenerPorId(id)
                if (local != null) NetworkResult.Success(local.toDomain())
                else NetworkResult.Error("Sin conexión y sin datos guardados")
            }
        } else {
            val local = dao.obtenerPorId(id)
            if (local != null) NetworkResult.Success(local.toDomain())
            else NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }
    override suspend fun obtenerNadadorPorEmail(email: String): NetworkResult<Nadador> {
        val resultado = try {
            val response = api.obtenerNadadorPorEmail(email)
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                NetworkResult.Error(response.body()?.mensaje ?: "Nadador no encontrado")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión al servidor")
        }
        return resultado
    }



    override suspend fun crearNadador(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Nadador> {
        val resultado = try {
            val response = api.crearNadador(NadadorRequestDto(nombre, apellidos, email, password))
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                NetworkResult.Error(response.body()?.mensaje ?: "Error al crear nadador")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun actualizarNadador(id: Int, nombre: String, apellidos: String): NetworkResult<Nadador> {
        val resultado = try {
            val local = dao.obtenerPorId(id)
            val response = api.actualizarNadador(id, NadadorRequestDto(nombre, apellidos, local?.email ?: "", ""))
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                NetworkResult.Error(response.body()?.mensaje ?: "Error al actualizar")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun eliminarNadador(id: Int): NetworkResult<Boolean> {
        val resultado = try {
            val response = api.eliminarNadador(id)
            if (response.isSuccessful) {
                dao.eliminar(id)
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("Error al eliminar nadador")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun vincularNadador(idNadador: Int, codigo: Int): NetworkResult<Nadador> {
        val resultado = try {
            val response = api.vincularNadador(idNadador, VincularCodigoRequestDto(codigo))
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                // Intentar leer el mensaje del error del cuerpo
                val mensaje = response.body()?.mensaje
                    ?: response.errorBody()?.string()?.let { extraerMensaje(it) }
                    ?: "Código incorrecto o no disponible"
                NetworkResult.Error(mensaje)
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión al servidor")
        }
        return resultado
    }

    // Helper: extrae "mensaje" del JSON de error que devuelve el API
    private fun extraerMensaje(json: String): String? = try {
        com.google.gson.Gson()
            .fromJson(json, com.swimming.app.data.dto.ApiResponseDto::class.java)
            ?.mensaje
    } catch (e: Exception) { null }
}

class EntrenadorRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: EntrenadorDao,
    private val networkChecker: NetworkChecker
) : EntrenadorRepository {

    override suspend fun obtenerEntrenador(id: Int): NetworkResult<Entrenador> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerEntrenador(id)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val dto = response.body()!!.datos!!
                    dao.insertar(dto.toEntity())
                    NetworkResult.Success(dto.toDomain())
                } else {
                    val local = dao.obtenerPorId(id)
                    if (local != null) NetworkResult.Success(local.toDomain())
                    else NetworkResult.Error("Error al obtener entrenador")
                }
            } catch (e: Exception) {
                val local = dao.obtenerPorId(id)
                if (local != null) NetworkResult.Success(local.toDomain())
                else NetworkResult.Error("Sin conexión y sin datos guardados")
            }
        } else {
            val local = dao.obtenerPorId(id)
            if (local != null) NetworkResult.Success(local.toDomain())
            else NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun obtenerEntrenadorPorEmail(email: String): NetworkResult<Entrenador> {
        val resultado = try {
            val response = api.obtenerEntrenadorPorEmail(email)
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                NetworkResult.Error(response.body()?.mensaje ?: "Entrenador no encontrado")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión al servidor")
        }
        return resultado
    }

    override suspend fun crearEntrenador(nombre: String, apellidos: String, email: String, password: String): NetworkResult<Entrenador> {
        val resultado = try {
            val response = api.crearEntrenador(EntrenadorRequestDto(nombre, apellidos, email, password))
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                NetworkResult.Error(response.body()?.mensaje ?: "Error al crear entrenador")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun actualizarEntrenador(id: Int, nombre: String, apellidos: String): NetworkResult<Entrenador> {
        val resultado = try {
            val local = dao.obtenerPorId(id)
            val response = api.actualizarEntrenador(id, EntrenadorRequestDto(nombre, apellidos, local?.email ?: "", ""))
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                NetworkResult.Error(response.body()?.mensaje ?: "Error al actualizar")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun eliminarEntrenador(id: Int): NetworkResult<Boolean> {
        val resultado = try {
            val response = api.eliminarEntrenador(id)
            if (response.isSuccessful) {
                dao.eliminar(id)
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error("Error al eliminar entrenador")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }
}

class EquipoRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: EquipoDao,
    private val networkChecker: NetworkChecker
) : EquipoRepository {

    override suspend fun obtenerEquipo(id: Int): NetworkResult<Equipo> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerEquipo(id)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val dto = response.body()!!.datos!!
                    dao.insertar(dto.toEntity())
                    NetworkResult.Success(dto.toDomain())
                } else {
                    val local = dao.obtenerPorId(id)
                    if (local != null) NetworkResult.Success(local.toDomain())
                    else NetworkResult.Error("Error al obtener equipo")
                }
            } catch (e: Exception) {
                val local = dao.obtenerPorId(id)
                if (local != null) NetworkResult.Success(local.toDomain())
                else NetworkResult.Error("Sin conexión y sin datos guardados")
            }
        } else {
            val local = dao.obtenerPorId(id)
            if (local != null) NetworkResult.Success(local.toDomain())
            else NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun crearEquipo(nombre: String, idEntrenador: Int?): NetworkResult<Equipo> {
        val resultado = try {
            val response = api.crearEquipo(EquipoRequestDto(nombre, idEntrenador))
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                NetworkResult.Error(response.body()?.mensaje ?: "Error al crear equipo")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun actualizarEquipo(id: Int, nombre: String): NetworkResult<Equipo> {
        val resultado = try {
            val response = api.actualizarEquipo(id, EquipoRequestDto(nombre, null))
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                NetworkResult.Error(response.body()?.mensaje ?: "Error al actualizar equipo")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun eliminarEquipo(id: Int): NetworkResult<Boolean> {
        val resultado = try {
            val response = api.eliminarEquipo(id)  // ✅ ARREGLADO: ahora llama al endpoint correcto
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("Error al eliminar equipo")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }
}

class NadadorEquipoRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: NadadorEquipoDao,
    private val networkChecker: NetworkChecker
) : NadadorEquipoRepository {

    override suspend fun obtenerNadadoresPorEquipo(idEquipo: Int): NetworkResult<List<NadadorEquipo>> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerNadadoresPorEquipo(idEquipo)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val lista = response.body()!!.datos!!
                    dao.eliminarPorEquipo(idEquipo)
                    dao.insertarTodos(lista.map { it.toEntity() })
                    NetworkResult.Success(lista.map { it.toDomain() })
                } else {
                    val local = dao.obtenerPorEquipo(idEquipo)
                    NetworkResult.Success(local.map { it.toDomain() })
                }
            } catch (e: Exception) {
                val local = dao.obtenerPorEquipo(idEquipo)
                NetworkResult.Success(local.map { it.toDomain() })
            }
        } else {
            val local = dao.obtenerPorEquipo(idEquipo)
            NetworkResult.Success(local.map { it.toDomain() })
        }
        return resultado
    }

    override suspend fun obtenerPorCodigo(codigo: Int): NetworkResult<NadadorEquipo> {
        val resultado = try {
            val response = api.obtenerPorCodigo(codigo)
            if (response.isSuccessful && response.body()?.datos != null)
                NetworkResult.Success(response.body()!!.datos!!.toDomain())
            else NetworkResult.Error("Código no encontrado")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun crearNadadorEquipo(nombre: String, apellidos: String, idEquipo: Int): NetworkResult<NadadorEquipo> {
        val resultado = try {
            val response = api.crearNadadorEquipo(NadadorEquipoRequestDto(nombre, apellidos, idEquipo))
            if (response.isSuccessful && response.body()?.datos != null)
                NetworkResult.Success(response.body()!!.datos!!.toDomain())
            else NetworkResult.Error(response.body()?.mensaje ?: "Error al crear")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun eliminarNadadorEquipo(id: Int): NetworkResult<Boolean> {
        val resultado = try {
            val response = api.eliminarNadadorEquipo(id)
            if (response.isSuccessful) {
                NetworkResult.Success(true)
            } else {
                NetworkResult.Error(response.body()?.mensaje ?: "Error al eliminar")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión al servidor")
        }
        return resultado
    }
}

class RutinaRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: RutinaDao,
    private val networkChecker: NetworkChecker
) : RutinaRepository {

    override suspend fun obtenerRutinasPorUsuario(idUsuario: Int): NetworkResult<List<Rutina>> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerRutinasPorUsuario(idUsuario)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val lista = response.body()!!.datos!!
                    dao.eliminarPorUsuario(idUsuario)
                    dao.insertarTodas(lista.map { it.toEntity() })
                    NetworkResult.Success(lista.map { it.toDomain() })
                } else {
                    val local = dao.obtenerPorUsuario(idUsuario)
                    NetworkResult.Success(local.map { it.toDomain() })
                }
            } catch (e: Exception) {
                val local = dao.obtenerPorUsuario(idUsuario)
                NetworkResult.Success(local.map { it.toDomain() })
            }
        } else {
            val local = dao.obtenerPorUsuario(idUsuario)
            NetworkResult.Success(local.map { it.toDomain() })
        }
        return resultado
    }

    override suspend fun crearRutina(contenido: String, fecha: String, mostrar: Boolean, idUsuario: Int): NetworkResult<Rutina> {
        val resultado = try {
            val response = api.crearRutina(RutinaRequestDto(contenido, fecha, mostrar, idUsuario))
            if (response.isSuccessful && response.body()?.datos != null)
                NetworkResult.Success(response.body()!!.datos!!.toDomain())
            else NetworkResult.Error(response.body()?.mensaje ?: "Error al crear rutina")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun eliminarRutina(id: Int): NetworkResult<Boolean> {
        val resultado = try {
            val response = api.eliminarRutina(id)
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("Error al eliminar rutina")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }
}

class MarcaDeTiempoRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: MarcaDeTiempoDao,
    private val networkChecker: NetworkChecker
) : MarcaDeTiempoRepository {

    override suspend fun obtenerMarcasPorNadadorEquipo(idNadadorEquipo: Int): NetworkResult<List<MarcaDeTiempo>> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerMarcasPorNadadorEquipo(idNadadorEquipo)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val lista = response.body()!!.datos!!
                    dao.eliminarPorNadadorEquipo(idNadadorEquipo)
                    dao.insertarTodas(lista.map { it.toEntity() })
                    NetworkResult.Success(lista.map { it.toDomain() })
                } else {
                    val local = dao.obtenerPorNadadorEquipo(idNadadorEquipo)
                    NetworkResult.Success(local.map { it.toDomain() })
                }
            } catch (e: Exception) {
                val local = dao.obtenerPorNadadorEquipo(idNadadorEquipo)
                NetworkResult.Success(local.map { it.toDomain() })
            }
        } else {
            val local = dao.obtenerPorNadadorEquipo(idNadadorEquipo)
            NetworkResult.Success(local.map { it.toDomain() })
        }
        return resultado
    }

    override suspend fun obtenerMarcasPorNadador(idNadador: Int): NetworkResult<List<MarcaDeTiempo>> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerMarcasPorNadador(idNadador)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val lista = response.body()!!.datos!!
                    NetworkResult.Success(lista.map { it.toDomain() })
                } else {
                    NetworkResult.Error(response.body()?.mensaje ?: "Error al obtener marcas")
                }
            } catch (e: Exception) {
                NetworkResult.Error("Sin conexión al servidor")
            }
        } else {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun crearMarca(tiempo: String, descripcion: String, idNadadorEquipo: Int?, idNadador: Int?): NetworkResult<MarcaDeTiempo> {
        val resultado = try {
            val response = api.crearMarca(MarcaDeTiempoRequestDto(tiempo, descripcion, idNadadorEquipo, idNadador))
            if (response.isSuccessful && response.body()?.datos != null)
                NetworkResult.Success(response.body()!!.datos!!.toDomain())
            else NetworkResult.Error(response.body()?.mensaje ?: "Error al crear marca")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    override suspend fun eliminarMarca(id: Int): NetworkResult<Boolean> {
        val resultado = try {
            val response = api.eliminarMarca(id)
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("Error al eliminar marca")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }
}
