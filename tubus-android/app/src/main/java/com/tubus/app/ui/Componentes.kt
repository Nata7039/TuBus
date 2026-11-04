package com.tubus.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tubus.app.data.Bus
import com.tubus.app.data.Ruta

// Paleta tomada del mockup de TuBus
val Navy = Color(0xFF0B2D7A)
val Verde = Color(0xFF1E9E4A)
val Rojo = Color(0xFFE53935)
val Amarillo = Color(0xFFF4B400)
val Fondo = Color(0xFFF6F8FC)
val TextoPrincipal = Color(0xFF1B2433)
val TextoSecundario = Color(0xFF5F6B7A)
val Borde = Color(0xFFE3E8F0)

private val Esquema = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    secondary = Verde,
    background = Fondo,
    surface = Color.White,
    onSurface = TextoPrincipal
)

@Composable
fun TuBusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Esquema, content = content)
}

val Ruta.color: Color get() = Color(colorInt)
val Ruta.colorTexto: Color get() = if (color.luminance() > 0.5f) Navy else Color.White

fun colorOcupacion(o: Int): Color = when {
    o >= 80 -> Rojo
    o >= 50 -> Amarillo
    else -> Verde
}

fun nivelOcupacion(o: Int): String = when {
    o >= 80 -> "Alta"
    o >= 50 -> "Media"
    else -> "Baja"
}

fun textoEta(min: Int): String = if (min <= 0) "Llegando" else "$min min"

/** Cuadro de color con el número de la ruta. */
@Composable
fun Insignia(ruta: Ruta, tamano: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp))
            .background(ruta.color),
        contentAlignment = Alignment.Center
    ) {
        Text(ruta.id.toString(), color = ruta.colorTexto, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun BarraOcupacion(ocupacion: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { ocupacion / 100f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = colorOcupacion(ocupacion),
            trackColor = Color(0xFFE6EAF0)
        )
        Text(
            "  $ocupacion%",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextoPrincipal
        )
    }
}

/** Tarjeta de ruta con el próximo bus, su tiempo de llegada y su ocupación. */
@Composable
fun TarjetaRuta(ruta: Ruta, proximo: Pair<Bus, Int>?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(ruta.color)
            )
            Row(
                Modifier
                    .padding(12.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Insignia(ruta)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(ruta.nombre, fontWeight = FontWeight.Bold, color = TextoPrincipal)
                    if (proximo != null) {
                        Text(
                            "Llega en ${textoEta(proximo.second)}",
                            fontSize = 14.sp,
                            color = TextoSecundario
                        )
                        BarraOcupacion(proximo.first.ocupacion)
                    } else {
                        Text("Sin buses en servicio", fontSize = 14.sp, color = TextoSecundario)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextoSecundario)
            }
        }
    }
}
