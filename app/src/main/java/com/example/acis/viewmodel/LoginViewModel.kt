package com.example.acis.viewmodel

import androidx.lifecycle.ViewModel
import com.example.acis.data.DatosDemo
import com.example.acis.data.Perfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel (MVVM): guarda la "estructura"/estado de la pantalla de Login
 * y centraliza la lógica/validación. La UI solo observa [uiState] y llama a sus funciones.
 */
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val error: String? = null,
    val perfilAutenticado: Perfil? = null
)

class LoginViewModel : ViewModel() {
    // Estado mutable PRIVADO: solo el ViewModel lo modifica.
    private val _uiState = MutableStateFlow(LoginUiState())
    // Estado público de SOLO LECTURA: la UI lo observa.
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, error = null) }
    }

    fun onClaveChange(valor: String) {
        _uiState.update { it.copy(clave = valor, error = null) }
    }

    /** Valida e intenta autenticar. La lógica queda aquí (no en la pantalla). */
    fun ingresar() {
        val actual = _uiState.value
        if (actual.correo.isBlank() || actual.clave.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa tu correo y tu clave.") }
            return
        }
        val perfil = DatosDemo.autenticar(actual.correo, actual.clave)
        if (perfil == null) {
            _uiState.update { it.copy(error = "Correo o clave incorrectos. Inténtalo nuevamente.") }
        } else {
            _uiState.update { it.copy(perfilAutenticado = perfil, error = null) }
        }
    }

    fun consumirNavegacion() {
        _uiState.update { it.copy(perfilAutenticado = null) }
    }
}
