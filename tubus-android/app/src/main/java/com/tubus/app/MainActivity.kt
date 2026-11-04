package com.tubus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tubus.app.data.TuBusViewModel
import com.tubus.app.ui.AlertasScreen
import com.tubus.app.ui.BarraInferior
import com.tubus.app.ui.DetalleRutaScreen
import com.tubus.app.ui.FavoritosScreen
import com.tubus.app.ui.Fondo
import com.tubus.app.ui.InicioScreen
import com.tubus.app.ui.MapaScreen
import com.tubus.app.ui.PerfilScreen
import com.tubus.app.ui.RutasScreen
import com.tubus.app.ui.SplashScreen
import com.tubus.app.ui.TuBusTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración de osmdroid: identificador de la app para descargar los mosaicos del mapa.
        Configuration.getInstance().load(
            applicationContext,
            applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContent {
            val vm: TuBusViewModel = viewModel()
            val escala by vm.escala.collectAsState()
            val densidad = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(densidad.density, densidad.fontScale * escala)
            ) {
                TuBusTheme { TuBusApp(vm) }
            }
        }
    }
}

@Composable
fun TuBusApp(vm: TuBusViewModel) {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route ?: ""
    val conBarra = listOf("inicio", "mapa", "rutas", "perfil").any { rutaActual.startsWith(it) }

    Scaffold(
        containerColor = Fondo,
        bottomBar = { if (conBarra) BarraInferior(rutaActual, nav) }
    ) { padding ->
        NavHost(nav, startDestination = "splash", modifier = Modifier.padding(padding)) {
            composable("splash") {
                SplashScreen {
                    nav.navigate("inicio") { popUpTo("splash") { inclusive = true } }
                }
            }
            composable("inicio") { InicioScreen(vm, nav) }
            composable(
                "mapa?ruta={ruta}",
                arguments = listOf(navArgument("ruta") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { MapaScreen(vm, it.arguments?.getInt("ruta") ?: -1) }
            composable("rutas") { RutasScreen(vm, nav) }
            composable(
                "detalle/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { DetalleRutaScreen(vm, it.arguments?.getInt("id") ?: 1, nav) }
            composable("favoritos") { FavoritosScreen(vm, nav) }
            composable("alertas") { AlertasScreen(vm, nav) }
            composable("perfil") { PerfilScreen(vm) }
        }
    }
}
