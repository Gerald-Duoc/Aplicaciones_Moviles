package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acis.data.Perfil
import com.example.acis.data.SessionStore
import com.example.acis.repository.AcisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel (MVVM): guarda la "estructura"/estado de la pantalla de Login
 * y centraliza la lógica/validación. La UI solo observa [uiState] y llama a sus funciones.
 */
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val baseUrl: String = SessionStore.baseUrl,
    val mensajeApi: String? = null,
    val error: String? = null,
    val perfilAutenticado: Perfil? = null
)

class LoginViewModel : ViewModel() {
    private val repositorio = AcisRepository()

    // Estado mutable PRIVADO: solo el ViewModel lo modifica.
    private val _uiState = MutableStateFlow(LoginUiState(baseUrl = SessionStore.baseUrl))
    // Estado público de SOLO LECTURA: la UI lo observa.
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, error = null) }
    }

    fun onClaveChange(valor: String) {
        _uiState.update { it.copy(clave = valor, error = null) }
    }

    fun onBaseUrlChange(valor: String) {
        _uiState.update { it.copy(baseUrl = valor, mensajeApi = null, error = null) }
    }

    /**
     * Guarda la URL de la API en el celular. Así el MISMO APK sirve para
     * cualquier PC: cada persona apunta al servidor que tenga en el puerto 8080.
     */
    fun guardarBaseUrl() {
        val valor = _uiState.value.baseUrl.trim()
        if (valor.isBlank()) {
            _uiState.update { it.copy(error = "Escribe la URL de la API.") }
            return
        }
        val normalizada = SessionStore.normalizarBaseUrl(valor)
        SessionStore.baseUrl = normalizada
        SessionStore.ultimoError = null
        _uiState.update {
            it.copy(
                baseUrl = normalizada,
                mensajeApi = "URL guardada: $normalizada",
                error = null
            )
        }
    }

    fun restaurarBaseUrl() {
        SessionStore.restaurarBaseUrl()
        SessionStore.ultimoError = null
        val url = SessionStore.baseUrl
        _uiState.update {
            it.copy(baseUrl = url, mensajeApi = "URL predeterminada restaurada: $url", error = null)
        }
    }

    /** Valida e intenta autenticar contra la API. La lógica queda aquí (no en la pantalla). */
    fun ingresar() {
        val actual = _uiState.value
        if (actual.correo.isBlank() || actual.clave.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa tu correo y tu clave.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            repositorio.login(actual.correo, actual.clave)
                .onSuccess { perfil ->
                    _uiState.update { it.copy(perfilAutenticado = perfil, error = null) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "No se pudo iniciar sesión.")
                    }
                }
        }
    }

    fun consumirNavegacion() {
        _uiState.update { it.copy(perfilAutenticado = null) }
    }
}
