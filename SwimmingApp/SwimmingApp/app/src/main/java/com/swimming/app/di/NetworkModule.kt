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
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val resultado = OkHttpClient.Builder().addInterceptor(logging).build()
        return resultado
    }

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

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        val resultado = retrofit.create(ApiService::class.java)
        return resultado
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        val resultado = FirebaseAuth.getInstance()
        return resultado
    }
}
