package com.example.acis.data.remote

import com.example.acis.data.SessionStore
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP de la app.
 *
 * La URL NO queda fija en el APK: se lee de SessionStore, así que cada
 * estudiante puede apuntar la app a la PC donde tenga corriendo la API.
 *
 * Ejemplos:
 * - Túnel de VS Code: https://xxxx-8080.devtunnels.ms/api/
 * - PC en la misma red:  http://192.168.1.20:8080/api/
 * - Emulador:            http://10.0.2.2:8080/api/
 */
object ApiClient {
    private var urlEnCache: String? = null
    private var apiEnCache: ApiService? = null

    /** Timeouts amplios para redes Wi-Fi lentas o con cambio de red. */
    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /** Devuelve el servicio apuntando a la URL configurada en la app. */
    fun api(): ApiService {
        val url = SessionStore.baseUrl
        if (apiEnCache == null || urlEnCache != url) {
            apiEnCache = Retrofit.Builder()
                .baseUrl(url)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            urlEnCache = url
        }
        return apiEnCache!!
    }
}

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun yo(@Header("Authorization") authorization: String): UsuarioDto

    @GET("ordenes")
    suspend fun ordenes(@Header("Authorization") authorization: String): List<OrdenDto>

    @GET("ordenes/{id}")
    suspend fun orden(
        @Header("Authorization") authorization: String,
        @Path("id") id: Long
    ): OrdenDto

    @GET("salud")
    suspend fun salud(): SaludDto
}
