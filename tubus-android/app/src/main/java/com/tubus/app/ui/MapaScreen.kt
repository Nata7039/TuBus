package com.tubus.app.ui

import android.graphics.drawable.GradientDrawable
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tubus.app.data.Eta
import com.tubus.app.data.TuBusViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private fun circulo(ctx: Context, color: Int, dp: Int, borde: Int = android.graphics.Color.WHITE): GradientDrawable {
    val px = (dp * ctx.resources.displayMetrics.density).toInt()
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke((2 * ctx.resources.displayMetrics.density).toInt(), borde)
        setSize(px, px)
    }
}

/**
 * Mapa en vivo con OpenStreetMap. Si [rutaId] es -1 muestra todas las rutas;
 * de lo contrario solo la ruta indicada.
 */
@Composable
fun MapaScreen(vm: TuBusViewModel, rutaId: Int) {
    val ctx = LocalContext.current
    val buses by vm.buses.collectAsState()
    var seleccionado by remember { mutableStateOf<String?>(null) }
    val marcadores = remember { mutableMapOf<String, Marker>() }

    val mapView = remember(rutaId) {
        marcadores.clear()
        MapView(ctx).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            val visibles = vm.rutas.filter { rutaId < 0 || it.id == rutaId }
            if (rutaId >= 0 && visibles.isNotEmpty()) {
                val medio = visibles.first().paraderos.let { it[it.size / 2] }
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(medio.lat, medio.lng))
            } else {
                controller.setZoom(14.0)
                controller.setCenter(GeoPoint(5.5353, -73.3678)) // Plaza de Bolívar, Tunja
            }
            visibles.forEach { r ->
                overlays.add(Polyline().apply {
                    setPoints((r.trazado + r.trazado.first()).map { GeoPoint(it.lat, it.lng) })
                    outlinePaint.color = r.colorInt
                    outlinePaint.strokeWidth = 9f
                })
                // Con todas las rutas a la vez solo se dibuja el trazado; los paraderos aparecen al elegir una.
                if (rutaId >= 0) r.paraderos.forEach { p ->
                    overlays.add(Marker(this).apply {
                        position = GeoPoint(p.lat, p.lng)
                        title = p.nombre
                        icon = circulo(ctx, android.graphics.Color.WHITE, 12, r.colorInt)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    })
                }
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observador)
        mapView.onResume()
        onDispose {
            lifecycle.removeObserver(observador)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                buses.filter { rutaId < 0 || it.rutaId == rutaId }.forEach { b ->
                    val ruta = vm.ruta(b.rutaId)
                    val m = marcadores.getOrPut(b.id) {
                        Marker(mv).apply {
                            icon = circulo(ctx, ruta.colorInt, 26)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            setOnMarkerClickListener { _, _ ->
                                seleccionado = b.id
                                true
                            }
                            mv.overlays.add(this)
                        }
                    }
                    m.position = GeoPoint(b.lat, b.lng)
                }
                mv.invalidate()
            }
        )

        val bus = buses.firstOrNull { it.id == seleccionado }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (bus == null) {
                Text(
                    "Toque un bus en el mapa para ver su tiempo de llegada y su ocupación.",
                    color = TextoSecundario,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val ruta = vm.ruta(bus.rutaId)
                val (parada, minutos) = Eta.proximaParada(bus, ruta)
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Insignia(ruta, 36.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ruta.nombre, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                            Text(
                                if (bus.detenido > 0) "Detenido" else "En movimiento",
                                fontSize = 12.sp,
                                color = if (bus.detenido > 0) Rojo else Verde,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(textoEta(minutos), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoPrincipal)
                    }
                    Text(
                        "Próxima parada: ${parada.nombre}",
                        fontSize = 13.sp,
                        color = TextoSecundario,
                        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                    )
                    Text("Ocupación: ${nivelOcupacion(bus.ocupacion)}", fontSize = 13.sp, color = TextoPrincipal)
                    BarraOcupacion(bus.ocupacion, Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
