package com.swimming.app.di

import com.swimming.app.data.network.ApiService
import com.swimming.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth

/**
 * Módulo de Hilt que proporciona las dependencias relacionadas con la red:
 * el cliente HTTP (OkHttp), la instancia de Retrofit, el servicio de la API
 * y la instancia de Firebase Authentication.
 *
 * Todas estas dependencias se registran como @Singleton para reutilizar
 * la misma instancia durante toda la vida de la aplicación, lo que mejora
 * el rendimiento al no recrear estos objetos en cada petición.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Crea el cliente HTTP OkHttp con un interceptor de logging
     * y timeouts amplios.
     *
     * Los timeouts de 60 segundos son necesarios porque la API está desplegada
     * en Render con plan gratuito, que pone la instancia a dormir tras 15 minutos
     * de inactividad y tarda hasta 50 segundos en "despertar" en la primera petición.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // Interceptor que registra en el log las peticiones y respuestas HTTP.
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val resultado = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)  // Tiempo máximo para establecer conexión.
            .readTimeout(60, TimeUnit.SECONDS)     // Tiempo máximo para recibir la respuesta.
            .writeTimeout(60, TimeUnit.SECONDS)    // Tiempo máximo para enviar la petición.
            .build()
        return resultado
    }

    /**
     * Crea la instancia de Retrofit configurada con la URL base de la API
     * y el conversor Gson para serializar/deserializar JSON automáticamente.
     */
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val resultado = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return resultado
    }

    /**
     * Crea la implementación dinámica de la interfaz ApiService usando Retrofit.
     * A partir de aquí, el resto de la app puede inyectar ApiService y llamar
     * directamente a sus métodos como si fueran funciones normales.
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        val resultado = retrofit.create(ApiService::class.java)
        return resultado
    }

    /**
     * Proporciona la instancia única de Firebase Authentication para gestionar
     * el registro, el inicio de sesión y la verificación de email de los usuarios.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val resultado = FirebaseAuth.getInstance()
        return resultado
    }
}