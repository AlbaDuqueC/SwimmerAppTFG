package com.swimming.app.data.network

import com.swimming.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("nadador") suspend fun crearNadador(@Body dto: NadadorRequestDto): Response<ApiResponseDto<NadadorResponseDto>>
    @GET("nadador/{id}") suspend fun obtenerNadador(@Path("id") id: Int): Response<ApiResponseDto<NadadorResponseDto>>
    @PUT("nadador/{id}") suspend fun actualizarNadador(@Path("id") id: Int, @Body dto: NadadorRequestDto): Response<ApiResponseDto<NadadorResponseDto>>
    @DELETE("nadador/{id}") suspend fun eliminarNadador(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>

    @POST("entrenador") suspend fun crearEntrenador(@Body dto: EntrenadorRequestDto): Response<ApiResponseDto<EntrenadorResponseDto>>
    @GET("entrenador/{id}") suspend fun obtenerEntrenador(@Path("id") id: Int): Response<ApiResponseDto<EntrenadorResponseDto>>
    @PUT("entrenador/{id}") suspend fun actualizarEntrenador(@Path("id") id: Int, @Body dto: EntrenadorRequestDto): Response<ApiResponseDto<EntrenadorResponseDto>>
    @DELETE("entrenador/{id}") suspend fun eliminarEntrenador(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>

    @POST("equipo") suspend fun crearEquipo(@Body dto: EquipoRequestDto): Response<ApiResponseDto<EquipoResponseDto>>
    @GET("equipo/{id}") suspend fun obtenerEquipo(@Path("id") id: Int): Response<ApiResponseDto<EquipoResponseDto>>

    @GET("nadadorequipo/equipo/{idEquipo}") suspend fun obtenerNadadoresPorEquipo(@Path("idEquipo") idEquipo: Int): Response<ApiResponseDto<List<NadadorEquipoResponseDto>>>
    @GET("nadadorequipo/codigo/{codigo}") suspend fun obtenerPorCodigo(@Path("codigo") codigo: Int): Response<ApiResponseDto<NadadorEquipoResponseDto>>
    @POST("nadadorequipo") suspend fun crearNadadorEquipo(@Body dto: NadadorEquipoRequestDto): Response<ApiResponseDto<NadadorEquipoResponseDto>>
    @DELETE("nadadorequipo/{id}") suspend fun eliminarNadadorEquipo(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>

    @GET("rutina/usuario/{idUsuario}") suspend fun obtenerRutinasPorUsuario(@Path("idUsuario") idUsuario: Int): Response<ApiResponseDto<List<RutinaResponseDto>>>
    @POST("rutina") suspend fun crearRutina(@Body dto: RutinaRequestDto): Response<ApiResponseDto<RutinaResponseDto>>
    @DELETE("rutina/{id}") suspend fun eliminarRutina(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>

    @GET("marcadetiempo/nadadorequipo/{id}") suspend fun obtenerMarcasPorNadadorEquipo(@Path("id") id: Int): Response<ApiResponseDto<List<MarcaDeTiempoResponseDto>>>
    @POST("marcadetiempo") suspend fun crearMarca(@Body dto: MarcaDeTiempoRequestDto): Response<ApiResponseDto<MarcaDeTiempoResponseDto>>
    @DELETE("marcadetiempo/{id}") suspend fun eliminarMarca(@Path("id") id: Int): Response<ApiResponseDto<Boolean>>
}
