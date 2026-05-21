package com.swimming.app.data.network

import com.swimming.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit que define todos los endpoints de la API REST.
 * Cada método representa una llamada HTTP y Retrofit genera automáticamente
 * la implementación que serializa los parámetros, realiza la petición
 * y deserializa la respuesta en formato JSON.
 *
 * Todas las respuestas vienen envueltas en ApiResponseDto<T> para que
 * el cliente pueda saber si la operación fue exitosa y leer el mensaje.
 */
interface ApiService {

    // ─── Nadador ──────────────────────────────────────────────────────────────

    /** Crea un nuevo nadador en el servidor. */
    @POST("nadador")
    suspend fun crearNadador(@Body dto: NadadorRequestDto): Response<ApiResponseDto<NadadorResponseDto>>

    /** Obtiene un nadador por su ID. */
    @GET("nadador/{id}")
    suspend fun obtenerNadador(@Path("id") id: Int): Response<ApiResponseDto<NadadorResponseDto>>

    /** Obtiene un nadador por su correo electrónico (usado al iniciar sesión). */
    @GET("nadador/email/{email}")
    suspend fun obtenerNadadorPorEmail(@Path("email") email: String): Response<ApiResponseDto<NadadorResponseDto>>

    /** Actualiza los datos de un nadador existente. */
    @PUT("nadador/{id}")
    suspend fun actualizarNadador(@Path("id") id: Int, @Body dto: NadadorRequestDto): Response<ApiResponseDto<NadadorResponseDto>>

    /** Elimina lógicamente un nadador por su ID. */
    @DELETE("nadador/{id}")
    suspend fun eliminarNadador(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>

    // ─── Entrenador ───────────────────────────────────────────────────────────

    /** Crea un nuevo entrenador en el servidor. */
    @POST("entrenador")
    suspend fun crearEntrenador(@Body dto: EntrenadorRequestDto): Response<ApiResponseDto<EntrenadorResponseDto>>

    /** Obtiene un entrenador por su ID. */
    @GET("entrenador/{id}")
    suspend fun obtenerEntrenador(@Path("id") id: Int): Response<ApiResponseDto<EntrenadorResponseDto>>

    /** Obtiene un entrenador por su correo electrónico (usado al iniciar sesión). */
    @GET("entrenador/email/{email}")
    suspend fun obtenerEntrenadorPorEmail(@Path("email") email: String): Response<ApiResponseDto<EntrenadorResponseDto>>

    /** Actualiza los datos de un entrenador existente. */
    @PUT("entrenador/{id}")
    suspend fun actualizarEntrenador(@Path("id") id: Int, @Body dto: EntrenadorRequestDto): Response<ApiResponseDto<EntrenadorResponseDto>>

    /** Elimina lógicamente un entrenador por su ID. */
    @DELETE("entrenador/{id}")
    suspend fun eliminarEntrenador(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>

    // ─── Equipo ───────────────────────────────────────────────────────────────

    /** Crea un nuevo equipo. Si dto.idEntrenador no es nulo, queda vinculado a ese entrenador. */
    @POST("equipo")
    suspend fun crearEquipo(@Body dto: EquipoRequestDto): Response<ApiResponseDto<EquipoResponseDto>>

    /** Obtiene un equipo por su ID. */
    @GET("equipo/{id}")
    suspend fun obtenerEquipo(@Path("id") id: Int): Response<ApiResponseDto<EquipoResponseDto>>

    /** Actualiza el nombre de un equipo existente. */
    @PUT("equipo/{id}")
    suspend fun actualizarEquipo(@Path("id") id: Int, @Body dto: EquipoRequestDto): Response<ApiResponseDto<EquipoResponseDto>>

    /** Elimina lógicamente un equipo por su ID. */
    @DELETE("equipo/{id}")
    suspend fun eliminarEquipo(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>

    // ─── NadadorEquipo ────────────────────────────────────────────────────────

    /** Obtiene todos los nadadores (fichas) registrados en un equipo concreto. */
    @GET("nadadorequipo/equipo/{idEquipo}")
    suspend fun obtenerNadadoresPorEquipo(@Path("idEquipo") idEquipo: Int): Response<ApiResponseDto<List<NadadorEquipoResponseDto>>>

    /**
     * Obtiene un NadadorEquipo a partir de su código de 6 dígitos.
     * Se usa cuando el nadador introduce el código que le ha dado el entrenador.
     */
    @GET("nadadorequipo/codigo/{codigo}")
    suspend fun obtenerPorCodigo(@Path("codigo") codigo: Int): Response<ApiResponseDto<NadadorEquipoResponseDto>>

    /** Crea una nueva ficha de nadador dentro de un equipo. Solo lo puede hacer el entrenador. */
    @POST("nadadorequipo")
    suspend fun crearNadadorEquipo(@Body dto: NadadorEquipoRequestDto): Response<ApiResponseDto<NadadorEquipoResponseDto>>

    /** Elimina lógicamente una ficha de NadadorEquipo por su ID. */
    @DELETE("nadadorequipo/{id}")
    suspend fun eliminarNadadorEquipo(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>

    /**
     * Vincula la cuenta del nadador con un NadadorEquipo del equipo
     * utilizando el código de 6 dígitos que le proporciona el entrenador.
     */
    @POST("nadador/{id}/vincular")
    suspend fun vincularNadador(
        @Path("id") id: Int,
        @Body body: VincularCodigoRequestDto
    ): Response<ApiResponseDto<NadadorResponseDto>>

    /** Actualiza el nombre y apellidos de un NadadorEquipo existente. */
    @PUT("NadadorEquipo/{id}")
    suspend fun actualizarNadadorEquipo(
        @Path("id") id: Int,
        @Body request: NadadorEquipoRequestDto
    ): Response<ApiResponseDto<NadadorEquipoResponseDto>>

    // ─── Rutina ───────────────────────────────────────────────────────────────

    /** Obtiene todas las rutinas asociadas a un usuario concreto. */
    @GET("rutina/usuario/{idUsuario}")
    suspend fun obtenerRutinasPorUsuario(@Path("idUsuario") idUsuario: Int): Response<ApiResponseDto<List<RutinaResponseDto>>>

    /** Crea una nueva rutina para un usuario. */
    @POST("rutina")
    suspend fun crearRutina(@Body dto: RutinaRequestDto): Response<ApiResponseDto<RutinaResponseDto>>

    /** Elimina lógicamente una rutina por su ID. */
    @DELETE("rutina/{id}")
    suspend fun eliminarRutina(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>

    // ─── MarcaDeTiempo ────────────────────────────────────────────────────────

    /** Obtiene todas las marcas de tiempo asociadas a un NadadorEquipo concreto. */
    @GET("marcadetiempo/nadadorequipo/{id}")
    suspend fun obtenerMarcasPorNadadorEquipo(@Path("id") id: Int): Response<ApiResponseDto<List<MarcaDeTiempoResponseDto>>>

    /** Obtiene todas las marcas de tiempo registradas por un nadador concreto. */
    @GET("marcadetiempo/nadador/{id}")
    suspend fun obtenerMarcasPorNadador(@Path("id") id: Int): Response<ApiResponseDto<List<MarcaDeTiempoResponseDto>>>

    /** Crea una nueva marca de tiempo (registrada por el nadador o asignada por el entrenador). */
    @POST("marcadetiempo")
    suspend fun crearMarca(@Body dto: MarcaDeTiempoRequestDto): Response<ApiResponseDto<MarcaDeTiempoResponseDto>>

    /** Elimina lógicamente una marca de tiempo por su ID. */
    @DELETE("marcadetiempo/{id}")
    suspend fun eliminarMarca(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>
}