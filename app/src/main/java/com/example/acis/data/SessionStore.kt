package com.example.acis.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Guarda la sesión (token) en el celular con SharedPreferences.
 * Así el token sobrevive aunque se cierre la app y se puede usar en cada
 * petición como: Authorization: Bearer <token>.
 */
object SessionStore {
    private const val PREFS = "acis_sesion"
    private const val TOKEN = "token"
    private const val PERFIL = "perfil"
    private const val MODO_API = "modo_api"
    private const val BASE_URL = "base_url"

    /** URL que usa la app si el usuario no configura otra. */
    const val BASE_URL_DEFAULT = "https://g0nx3nvm-8080.brs.devtunnels.ms/api/"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    val token: String?
        get() = if (::prefs.isInitialized) prefs.getString(TOKEN, null) else null

    val perfil: String?
        get() = if (::prefs.isInitialized) prefs.getString(PERFIL, null) else null

    /** URL base de la API. Se puede cambiar desde la pantalla de login. */
    var baseUrl: String
        get() = if (::prefs.isInitialized) {
            prefs.getString(BASE_URL, BASE_URL_DEFAULT) ?: BASE_URL_DEFAULT
        } else {
            BASE_URL_DEFAULT
        }
        set(value) {
            if (::prefs.isInitialized) {
                prefs.edit().putString(BASE_URL, normalizarBaseUrl(value)).apply()
            }
        }

    /**
     * Deja una URL usable por Retrofit:
     * - agrega http:// si el usuario escribió solo la IP,
     * - agrega /api/ si escribió solo el host o el puerto,
     * - siempre termina con /.
     */
    fun normalizarBaseUrl(valor: String): String {
        var url = valor.trim().trimEnd('/')
        if (url.isBlank()) return BASE_URL_DEFAULT
        if (!url.startsWith("http://", ignoreCase = true) &&
            !url.startsWith("https://", ignoreCase = true)
        ) {
            url = "http://$url"
        }
        if (!url.endsWith("/api", ignoreCase = true)) {
            url += "/api"
        }
        return "$url/"
    }

    fun restaurarBaseUrl() {
        if (::prefs.isInitialized) prefs.edit().remove(BASE_URL).apply()
    }

    /** Último error de red/JSON, para mostrarlo en pantalla durante la defensa. */
    var ultimoError: String? = null

    /** true = los datos vienen de la API; false = modo demostración con DatosDemo. */
    var modoApi: Boolean
        get() = ::prefs.isInitialized && prefs.getBoolean(MODO_API, false)
        set(value) {
            if (::prefs.isInitialized) prefs.edit().putBoolean(MODO_API, value).apply()
        }

    fun guardarSesion(token: String, perfil: Perfil) {
        if (!::prefs.isInitialized) return
        prefs.edit()
            .putString(TOKEN, token)
            .putString(PERFIL, perfil.name)
            .putBoolean(MODO_API, true)
            .apply()
    }

    fun guardarModoDemo(perfil: Perfil) {
        if (!::prefs.isInitialized) return
        prefs.edit()
            .remove(TOKEN)
            .putString(PERFIL, perfil.name)
            .putBoolean(MODO_API, false)
            .apply()
    }

    fun cerrarSesion() {
        if (!::prefs.isInitialized) return
        val urlGuardada = baseUrl
        prefs.edit().clear().apply()
        // La URL configurable se conserva: no depende del usuario ni del equipo.
        baseUrl = urlGuardada
    }

    fun authorization(): String? = token?.let { "Bearer $it" }
}
