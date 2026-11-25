package com.example.acis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.acis.data.SessionStore
import com.example.acis.ui.screens.CatastroScreen
import com.example.acis.ui.screens.ClienteScreen
import com.example.acis.ui.screens.DetalleOtScreen
import com.example.acis.ui.screens.EjecucionOtScreen
import com.example.acis.ui.screens.GestionOtScreen
import com.example.acis.ui.screens.LoginScreen
import com.example.acis.ui.screens.MisOtScreen
import com.example.acis.ui.screens.PanelOtScreen
import com.example.acis.ui.theme.AcisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SessionStore.init(applicationContext)
        setContent {
            AcisTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "login") {

                    // Pantalla 1: acceso para todos los perfiles
                    composable("login") { LoginScreen(navController) }

                    // Perfil técnico de terreno: pantallas 2, 3 y 4
                    composable("mis_ot") { MisOtScreen(navController) }
                    composable(
                        route = "detalle/{otId}",
                        arguments = listOf(navArgument("otId") { type = NavType.StringType })
                    ) { entrada ->
                        DetalleOtScreen(navController, entrada.arguments?.getString("otId") ?: "")
                    }
                    composable(
                        route = "ejecucion/{otId}",
                        arguments = listOf(navArgument("otId") { type = NavType.StringType })
                    ) { entrada ->
                        EjecucionOtScreen(navController, entrada.arguments?.getString("otId") ?: "")
                    }

                    // Perfil planificador: pantallas 5, 6 y 7
                    composable("panel") { PanelOtScreen(navController) }
                    composable(
                        route = "gestionar/{modo}/{otId}",
                        arguments = listOf(
                            navArgument("modo") { type = NavType.StringType },
                            navArgument("otId") { type = NavType.StringType }
                        )
                    ) { entrada ->
                        GestionOtScreen(
                            navController,
                            entrada.arguments?.getString("modo") ?: "nueva",
                            entrada.arguments?.getString("otId") ?: ""
                        )
                    }
                    composable("catastro") { CatastroScreen(navController) }

                    // Perfil cliente: pantalla 8
                    composable("cliente") { ClienteScreen(navController) }
                }
            }
        }
    }
}
