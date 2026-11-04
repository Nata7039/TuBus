package com.tubus.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tubus.app.R
import com.tubus.app.data.Alertas
import com.tubus.app.data.Eta
import com.tubus.app.data.TuBusViewModel

// ---------------------------------------------------------------- Splash
@Composable
fun SplashScreen(onTerminar: () -> Unit) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, tween(durationMillis = 2200))
        onTerminar()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF081B4B), Navy, Color(0xFF123D9C)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(R.drawable.logo_tubus),
                    contentDescription = "Logo de TuBus",
                    modifier = Modifier.size(160.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("TuBus", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)
            Text("Tu ruta, en tiempo real", color = Color(0xFFDCE6FA), fontSize = 16.sp)
            Spacer(Modifier.height(48.dp))
            LinearProgressIndicator(
                progress = { progreso.value },
                modifier = Modifier
                    .width(220.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color.White,
                trackColor = Color(0x44FFFFFF)
            )
            Spacer(Modifier.height(10.dp))
            Text("Cargando...", color = Color.White, fontSize = 14.sp)
        }
    }
}

// ---------------------------------------------------------------- Inicio
@Composable
private fun AccesoRapido(icono: ImageVector, etiqueta: String, fondo: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(fondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = etiqueta, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(etiqueta, fontSize = 13.sp, color = TextoPrincipal)
    }
}

@Composable
fun InicioScreen(vm: TuBusViewModel, nav: NavController) {
    val buses by vm.buses.collectAsState()
    var consulta by remember { mutableStateOf("") }
    val lista = vm.rutas.filter { r ->
        consulta.isBlank() ||
            r.nombre.contains(consulta, ignoreCase = true) ||
            r.paraderos.any { it.nombre.contains(consulta, ignoreCase = true) }
    }
    val hayAlertas = Alertas.generar(buses, vm.rutas).isNotEmpty()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "TuBus", color = Navy, fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { irA(nav, "alertas") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alertas", tint = Navy)
                    }
                    if (hayAlertas) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Rojo)
                        )
                    }
                }
            }
            Text("¡Hola!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
            Text("¿A dónde quieres ir?", color = TextoSecundario)
        }
        item {
            OutlinedTextField(
                value = consulta,
                onValueChange = { consulta = it },
                placeholder = { Text("Buscar destino o ruta...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AccesoRapido(Icons.AutoMirrored.Filled.List, "Rutas", Verde) { irA(nav, "rutas") }
                AccesoRapido(Icons.Default.Place, "Mapa", Navy) { irA(nav, "mapa") }
                AccesoRapido(Icons.Default.Star, "Favoritos", Color(0xFFE8710A)) { nav.navigate("favoritos") }
                AccesoRapido(Icons.Default.Notifications, "Alertas", Color(0xFF00897B)) { nav.navigate("alertas") }
            }
        }
        item {
            Text("Rutas cercanas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
        }
        if (lista.isEmpty()) {
            item { Text("No se encontraron rutas para \"$consulta\".", color = TextoSecundario) }
        }
        items(lista, key = { it.id }) { r ->
            TarjetaRuta(r, Eta.proximo(buses, r)) { nav.navigate("detalle/${r.id}") }
        }
    }
}

// ---------------------------------------------------------------- Rutas y favoritos
@Composable
fun RutasScreen(vm: TuBusViewModel, nav: NavController) {
    val buses by vm.buses.collectAsState()
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item { Text("Rutas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal) }
        items(vm.rutas, key = { it.id }) { r ->
            TarjetaRuta(r, Eta.proximo(buses, r)) { nav.navigate("detalle/${r.id}") }
        }
    }
}

@Composable
fun FavoritosScreen(vm: TuBusViewModel, nav: NavController) {
    val buses by vm.buses.collectAsState()
    val favoritos by vm.favoritos.collectAsState()
    val lista = vm.rutas.filter { it.id in favoritos }
    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Favoritos") { nav.popBackStack() }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (lista.isEmpty()) {
                item {
                    Text(
                        "Aún no tiene rutas favoritas. Abra una ruta y toque la estrella para guardarla.",
                        color = TextoSecundario
                    )
                }
            }
            items(lista, key = { it.id }) { r ->
                TarjetaRuta(r, Eta.proximo(buses, r)) { nav.navigate("detalle/${r.id}") }
            }
        }
    }
}

// ---------------------------------------------------------------- Detalle de ruta
@Composable
fun BarraSuperior(titulo: String, accion: (@Composable () -> Unit)? = null, onAtras: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAtras) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextoPrincipal)
        }
        Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextoPrincipal, modifier = Modifier.weight(1f))
        accion?.invoke()
    }
}

@Composable
fun DetalleRutaScreen(vm: TuBusViewModel, id: Int, nav: NavController) {
    val ruta = vm.ruta(id)
    val buses by vm.buses.collectAsState()
    val favoritos by vm.favoritos.collectAsState()
    val proximo = Eta.proximo(buses, ruta)
    val esFavorita = id in favoritos

    Column(Modifier.fillMaxSize()) {
        BarraSuperior(
            "Detalle de ruta",
            accion = {
                IconButton(onClick = { vm.alternarFavorito(id) }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = if (esFavorita) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (esFavorita) Amarillo else Color(0xFFB8C0CC)
                    )
                }
            },
            onAtras = { nav.popBackStack() }
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Encabezado con el color de la ruta
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ruta.color)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) { Text(ruta.id.toString(), color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                Column {
                    Text(ruta.nombre, color = ruta.colorTexto, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(ruta.descripcion, color = ruta.colorTexto, fontSize = 13.sp)
                }
            }

            // Tiempo de llegada y ocupación
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Tiempo estimado de llegada", fontSize = 12.sp, color = TextoSecundario)
                        Text(
                            if (proximo != null) textoEta(proximo.second) else "—",
                            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal
                        )
                        Text("a ${ruta.paraderos[ruta.refParadero].nombre}", fontSize = 12.sp, color = TextoSecundario)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Ocupación del bus", fontSize = 12.sp, color = TextoSecundario)
                        if (proximo != null) {
                            Text(
                                nivelOcupacion(proximo.first.ocupacion),
                                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = colorOcupacion(proximo.first.ocupacion)
                            )
                            BarraOcupacion(proximo.first.ocupacion)
                        } else {
                            Text("—", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Paradas con el próximo bus
            Text("Paradas principales", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoPrincipal)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    ruta.paraderos.forEachIndexed { i, p ->
                        val eta = Eta.proximo(buses, ruta, i)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (i == ruta.refParadero) ruta.color else Color.White)
                                        .border(2.dp, ruta.color, CircleShape)
                                )
                                if (i < ruta.paraderos.size - 1) {
                                    Box(
                                        Modifier
                                            .width(3.dp)
                                            .height(26.dp)
                                            .background(ruta.color.copy(alpha = 0.5f))
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(p.nombre, color = TextoPrincipal, fontWeight = if (i == ruta.refParadero) FontWeight.Bold else FontWeight.Normal)
                                if (i == ruta.refParadero) Text("Paradero de referencia", fontSize = 11.sp, color = TextoSecundario)
                            }
                            Text(
                                if (eta != null) textoEta(eta.second) else "—",
                                fontSize = 13.sp, color = TextoSecundario
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        Button(
            onClick = { nav.navigate("mapa?ruta=$id") },
            colors = ButtonDefaults.buttonColors(containerColor = Verde),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp)
        ) {
            Icon(Icons.Default.Place, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ver en el mapa", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------------------------------------------------------------- Alertas
@Composable
fun AlertasScreen(vm: TuBusViewModel, nav: NavController) {
    val buses by vm.buses.collectAsState()
    val alertas = Alertas.generar(buses, vm.rutas)
    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Alertas") { nav.popBackStack() }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (alertas.isEmpty()) {
                item { Text("Sin alertas por ahora. Todos los buses circulan con normalidad.", color = TextoSecundario) }
            }
            items(alertas) { a ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (a.grave) Rojo else Amarillo)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(a.titulo, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                            Text(a.detalle, fontSize = 13.sp, color = TextoSecundario)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Perfil / configuración / ayuda
@Composable
fun PerfilScreen(vm: TuBusViewModel) {
    val escala by vm.escala.collectAsState()
    val opciones = listOf("Pequeño" to 0.85f, "Mediano" to 1.0f, "Grande" to 1.2f)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Perfil", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextoPrincipal)
        Text("Preferencias", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text("Cambia el tamaño de la letra en toda la aplicación", color = TextoSecundario)
        opciones.forEach { (nombre, valor) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { vm.cambiarEscala(valor) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = escala == valor, onClick = { vm.cambiarEscala(valor) })
                Text(nombre, fontSize = (16 * valor).sp, color = TextoPrincipal)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Acerca de", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy)
        Text(
            "TuBus es un prototipo académico para consultar rutas, ubicación, tiempo de llegada y ocupación " +
                "del transporte público urbano de Tunja. Los buses que se ven en el mapa son simulados.",
            color = TextoPrincipal
        )
        Text(
            "Desarrollado por Natalia Martínez Balseiro y Yeison Estiven Parada Alvarado. UPTC, Tunja.",
            color = TextoSecundario
        )
        Text("Versión 0.1 (prototipo)", color = TextoSecundario, textAlign = TextAlign.Start)
    }
}

// ---------------------------------------------------------------- Barra inferior
fun irA(nav: NavController, destino: String) {
    nav.navigate(destino) {
        launchSingleTop = true
        popUpTo("inicio")
    }
}

@Composable
fun BarraInferior(rutaActual: String, nav: NavController) {
    val items = listOf(
        Triple("inicio", "Inicio", Icons.Default.Home),
        Triple("mapa", "Mapa", Icons.Default.Place),
        Triple("rutas", "Rutas", Icons.AutoMirrored.Filled.List),
        Triple("perfil", "Perfil", Icons.Default.Person)
    )
    NavigationBar(containerColor = Color.White) {
        items.forEach { (ruta, etiqueta, icono) ->
            NavigationBarItem(
                selected = rutaActual.startsWith(ruta),
                onClick = { irA(nav, ruta) },
                icon = { Icon(icono, contentDescription = etiqueta) },
                label = { Text(etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Verde,
                    selectedTextColor = Verde,
                    indicatorColor = Color(0xFFE3F4E9)
                )
            )
        }
    }
}
