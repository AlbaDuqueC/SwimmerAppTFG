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

/**
 * Implementación del repositorio de Nadador en la capa de datos.
 * Sigue el patrón "network-first con fallback a caché local":
 *   1. Si hay conexión a internet, intenta obtener los datos del servidor
 *      y los guarda en Room para futuras consultas sin conexión.
 *   2. Si la petición falla o no hay conexión, recurre a los datos guardados
 *      en la base de datos local.
 *
 * Todas las operaciones devuelven NetworkResult para que la capa superior
 * pueda distinguir entre éxito y error sin tener que lanzar excepciones.
 */
class NadadorRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: NadadorDao,
    private val networkChecker: NetworkChecker
) : NadadorRepository {

    /**
     * Obtiene un nadador por su ID combinando red y caché local.
     * Si hay conexión, consulta la API y guarda el resultado en Room.
     * Si no hay conexión o falla, recurre a los datos locales.
     */
    override suspend fun obtenerNadador(id: Int): NetworkResult<Nadador> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerNadador(id)
                if (response.isSuccessful && response.body()?.datos != null) {
                    // La API ha respondido correctamente: actualizamos la caché y devolvemos.
                    val dto = response.body()!!.datos!!
                    dao.insertar(dto.toEntity())
                    NetworkResult.Success(dto.toDomain())
                } else {
                    // La API ha respondido con error: intentamos servir desde caché local.
                    val local = dao.obtenerPorId(id)
                    if (local != null) NetworkResult.Success(local.toDomain())
                    else NetworkResult.Error(response.body()?.mensaje ?: "Error al obtener nadador")
                }
            } catch (e: Exception) {
                // Excepción de red: intentamos servir desde caché local.
                val local = dao.obtenerPorId(id)
                if (local != null) NetworkResult.Success(local.toDomain())
                else NetworkResult.Error("Sin conexión y sin datos guardados")
            }
        } else {
            // No hay conexión a internet: servimos directamente desde caché local.
            val local = dao.obtenerPorId(id)
            if (local != null) NetworkResult.Success(local.toDomain())
            else NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }

    /**
     * Obtiene un nadador por su email consultando siempre a la API.
     * Se usa al iniciar sesión, por lo que necesita datos frescos del servidor.
     */
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

    /**
     * Crea un nuevo nadador llamando a la API y guarda una copia en la caché local.
     */
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

    /**
     * Actualiza el nombre y los apellidos de un nadador existente.
     * Toma el email actual de la caché local para no perderlo en la actualización.
     */
    override suspend fun actualizarNadador(id: Int, nombre: String, apellidos: String): NetworkResult<Nadador> {
        val resultado = try {
            val local = dao.obtenerPorId(id)
            // Se envía la contraseña vacía porque no se está cambiando aquí.
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

    /**
     * Elimina lógicamente un nadador del servidor y de la caché local.
     */
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

    /**
     * Vincula la cuenta del nadador con un NadadorEquipo usando el código de 6 dígitos.
     * Si la API devuelve error, se intenta extraer el mensaje legible del cuerpo
     * para mostrárselo al usuario en lugar de un código de estado HTTP.
     */
    override suspend fun vincularNadador(idNadador: Int, codigo: Int): NetworkResult<Nadador> {
        val resultado = try {
            val response = api.vincularNadador(idNadador, VincularCodigoRequestDto(codigo))
            if (response.isSuccessful && response.body()?.datos != null) {
                val dto = response.body()!!.datos!!
                dao.insertar(dto.toEntity())
                NetworkResult.Success(dto.toDomain())
            } else {
                // Se intenta extraer el mensaje real de error que viene en el cuerpo de la respuesta.
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

    /**
     * Función auxiliar que parsea el JSON de error devuelto por la API
     * y extrae el campo "mensaje" para mostrarlo al usuario.
     * Si el parsing falla, devuelve null.
     */
    private fun extraerMensaje(json: String): String? = try {
        com.google.gson.Gson()
            .fromJson(json, com.swimming.app.data.dto.ApiResponseDto::class.java)
            ?.mensaje
    } catch (e: Exception) { null }
}

/**
 * Implementación del repositorio de Entrenador en la capa de datos.
 * Sigue el mismo patrón "network-first con fallback a caché local" que NadadorRepositoryImpl.
 */
class EntrenadorRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: EntrenadorDao,
    private val networkChecker: NetworkChecker
) : EntrenadorRepository {

    /**
     * Obtiene un entrenador por su ID combinando red y caché local.
     */
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

    /**
     * Obtiene un entrenador por su email consultando siempre a la API.
     * Se usa al iniciar sesión, por lo que necesita datos frescos del servidor.
     */
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

    /**
     * Crea un nuevo entrenador llamando a la API y guarda una copia en la caché local.
     */
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

    /**
     * Actualiza el nombre y los apellidos de un entrenador existente.
     * Toma el email actual de la caché local para no perderlo.
     */
    override suspend fun actualizarEntrenador(id: Int, nombre: String, apellidos: String): NetworkResult<Entrenador> {
        val resultado = try {
            val local = dao.obtenerPorId(id)
            // Se envía la contraseña vacía porque no se está cambiando aquí.
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

    /**
     * Elimina lógicamente un entrenador del servidor y de la caché local.
     */
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

/**
 * Implementación del repositorio de Equipo en la capa de datos.
 * Sigue el patrón "network-first con fallback a caché local".
 */
class EquipoRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: EquipoDao,
    private val networkChecker: NetworkChecker
) : EquipoRepository {

    /**
     * Obtiene un equipo por su ID combinando red y caché local.
     */
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

    /**
     * Crea un nuevo equipo en el servidor y guarda una copia en la caché local.
     * Si se pasa idEntrenador, el servidor vincula automáticamente el equipo a ese entrenador.
     */
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

    /**
     * Actualiza el nombre de un equipo existente.
     * Se envía idEntrenador como null para no volver a vincular al entrenador.
     */
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

    /**
     * Elimina lógicamente un equipo a través del endpoint correspondiente.
     */
    override suspend fun eliminarEquipo(id: Int): NetworkResult<Boolean> {
        val resultado = try {
            val response = api.eliminarEquipo(id)
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("Error al eliminar equipo")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
        return resultado
    }
}

/**
 * Implementación del repositorio de NadadorEquipo en la capa de datos.
 * Para las listas usa una estrategia "borrar y reinsertar": antes de guardar
 * la nueva lista en Room, elimina la antigua para evitar registros obsoletos.
 */
class NadadorEquipoRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: NadadorEquipoDao,
    private val networkChecker: NetworkChecker
) : NadadorEquipoRepository {

    /**
     * Obtiene todos los nadadores de un equipo combinando red y caché local.
     * Cuando llegan datos frescos del servidor, se sustituye la caché local completa
     * para mantener la coherencia (sin nadadores fantasma de listas anteriores).
     */
    override suspend fun obtenerNadadoresPorEquipo(idEquipo: Int): NetworkResult<List<NadadorEquipo>> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerNadadoresPorEquipo(idEquipo)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val lista = response.body()!!.datos!!
                    // Se vacía la caché del equipo y se inserta la nueva lista completa.
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

    /**
     * Obtiene un NadadorEquipo por su código único de 6 dígitos.
     * Esta operación solo se hace contra la API porque el código es la forma
     * en que un nadador se conecta al equipo, y no tiene sentido buscarlo en caché.
     */
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

    /**
     * Crea una nueva ficha de NadadorEquipo dentro de un equipo.
     */
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

    /**
     * Actualiza el nombre y los apellidos de un NadadorEquipo existente en el servidor.
     * El idEquipo se envía igual que el actual para no romper la vinculación.
     */
    override suspend fun actualizarNadadorEquipo(
        id: Int,
        nombre: String,
        apellidos: String,
        idEquipo: Int
    ): NetworkResult<NadadorEquipo> {
        return try {
            val response = api.actualizarNadadorEquipo(
                id,
                NadadorEquipoRequestDto(nombre, apellidos, idEquipo)
            )
            if (response.isSuccessful && response.body()?.datos != null)
                NetworkResult.Success(response.body()!!.datos!!.toDomain())
            else
                NetworkResult.Error(response.body()?.mensaje ?: "Error al actualizar")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
    }

    /**
     * Elimina lógicamente una ficha de NadadorEquipo.
     */
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

/**
 * Implementación del repositorio de Rutina en la capa de datos.
 * Usa la estrategia "borrar y reinsertar" para mantener actualizada la lista
 * de rutinas de cada usuario en la caché local.
 */
class RutinaRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: RutinaDao,
    private val networkChecker: NetworkChecker
) : RutinaRepository {

    /**
     * Obtiene todas las rutinas de un usuario combinando red y caché local.
     */
    override suspend fun obtenerRutinasPorUsuario(idUsuario: Int): NetworkResult<List<Rutina>> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerRutinasPorUsuario(idUsuario)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val lista = response.body()!!.datos!!
                    // Se vacía la caché del usuario y se inserta la nueva lista completa.
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

    /**
     * Crea una nueva rutina en el servidor.
     */
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

    /**
     * Elimina lógicamente una rutina por su ID.
     */
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

/**
 * Implementación del repositorio de MarcaDeTiempo en la capa de datos.
 * Las marcas asociadas a un NadadorEquipo se cachean en Room.
 * Las consultas por nadador siempre van al servidor porque pertenecen al usuario actual
 * y necesitan datos frescos.
 */
class MarcaDeTiempoRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val dao: MarcaDeTiempoDao,
    private val networkChecker: NetworkChecker
) : MarcaDeTiempoRepository {

    /**
     * Obtiene las marcas de un NadadorEquipo combinando red y caché local.
     * Es la consulta principal cuando el entrenador entra a ver los tiempos de un nadador.
     */
    override suspend fun obtenerMarcasPorNadadorEquipo(idNadadorEquipo: Int): NetworkResult<List<MarcaDeTiempo>> {
        val resultado = if (networkChecker.hayConexion()) {
            try {
                val response = api.obtenerMarcasPorNadadorEquipo(idNadadorEquipo)
                if (response.isSuccessful && response.body()?.datos != null) {
                    val lista = response.body()!!.datos!!
                    // Se vacía la caché y se inserta la nueva lista completa.
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

    /**
     * Obtiene las marcas registradas por un nadador concreto.
     * Esta consulta siempre va al servidor (sin fallback local)
     * porque corresponde a las marcas personales del usuario activo.
     */
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

    /**
     * Registra una nueva marca de tiempo en el servidor.
     * Si idNadador es nulo, la marca se considera asignada por el entrenador.
     */
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

    /**
     * Elimina lógicamente una marca de tiempo por su ID.
     *
     * Manejo robusto: si la API responde con un código 2xx pero el cuerpo no
     * se puede parsear, Retrofit lanza HttpException. Comprobamos el código
     * para tratar esos casos como éxito en lugar de marcarlos como error.
     */
    override suspend fun eliminarMarca(id: Int): NetworkResult<Boolean> {
        return try {
            val response = api.eliminarMarca(id)
            if (response.isSuccessful) NetworkResult.Success(true)
            else NetworkResult.Error("Error al eliminar marca (código ${response.code()})")
        } catch (e: retrofit2.HttpException) {
            // Si el código HTTP es de éxito (2xx) pero falló el parseo del body,
            // lo tratamos como éxito porque el servidor sí ha procesado la eliminación.
            if (e.code() in 200..299) NetworkResult.Success(true)
            else NetworkResult.Error("Error al eliminar marca: ${e.code()}")
        } catch (e: Exception) {
            NetworkResult.Error("Sin conexión a internet")
        }
    }
}