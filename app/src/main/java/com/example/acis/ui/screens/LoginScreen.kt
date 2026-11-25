package com.example.acis.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.acis.data.DatosDemo
import com.example.acis.data.Perfil
import com.example.acis.data.SessionStore
import com.example.acis.viewmodel.LoginViewModel

/**
 * Pantalla 1 - Inicio de sesión (HU-01).
 * Según el perfil con que se ingresa, la app muestra solo las opciones de ese perfil.
 *
 * MVVM: esta pantalla (View) NO guarda la lógica ni el estado de negocio;
 * observa [LoginViewModel.uiState] (StateFlow) y delega las acciones al ViewModel.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    vm: LoginViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    var verClave by rememberSaveable { mutableStateOf(false) }
    var verConfigApi by rememberSaveable { mutableStateOf(false) }

    // Efecto de navegación: cuando el ViewModel marca un perfil autenticado, navegamos.
    LaunchedEffect(state.perfilAutenticado) {
        val perfil = state.perfilAutenticado ?: return@LaunchedEffect
        val destino = when (perfil) {
            Perfil.TECNICO -> "mis_ot"
            Perfil.PLANIFICADOR -> "panel"
            Perfil.CLIENTE -> "cliente"
        }
        vm.consumirNavegacion()
        navController.navigate(destino) {
            popUpTo("login") { inclusive = true }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.size(84.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "ACIS Servicios Técnicos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Gestión de órdenes de trabajo en terreno",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.correo,
                onValueChange = { vm.onCorreoChange(it) },
                label = { Text("Correo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.clave,
                onValueChange = { vm.onClaveChange(it) },
                label = { Text("Clave") },
                singleLine = true,
                visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { verClave = !verClave }) {
                        Text(if (verClave) "Ocultar" else "Ver")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { verConfigApi = !verConfigApi },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (verConfigApi) "Ocultar configuración de API"
                    else "Configurar API (otra PC)"
                )
            }
            Text(
                text = "Servidor actual: ${state.baseUrl}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )

            if (verConfigApi) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.baseUrl,
                    onValueChange = { vm.onBaseUrlChange(it) },
                    label = { Text("URL de la API") },
                    placeholder = { Text("http://192.168.1.20:8080/api/") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Ejemplos:\n" +
                        "• Red local: http://IP_DE_LA_PC:8080/api/\n" +
                        "• Túnel: https://xxxx-8080.devtunnels.ms/api/\n" +
                        "Si escribiste solo la IP o el puerto, la app agrega /api/ automáticamente.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.guardarBaseUrl() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar URL de la API")
                }
                TextButton(
                    onClick = { vm.restaurarBaseUrl() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Usar URL predeterminada")
                }
            }

            if (state.mensajeApi != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.mensajeApi ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.ingresar() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }

            Spacer(Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Usuarios de prueba (solo prototipo)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Toca uno para completar los datos automáticamente.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    DatosDemo.usuariosPrueba.forEach { usuario ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm.onCorreoChange(usuario.correo)
                                    vm.onClaveChange(usuario.clave)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(usuario.etiqueta, fontWeight = FontWeight.Medium)
                                Text(
                                    "${usuario.correo}  ·  clave ${usuario.clave}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
