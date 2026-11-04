package com.tubus.app

import com.tubus.app.data.Bus
import com.tubus.app.data.Eta
import com.tubus.app.data.Paradero
import com.tubus.app.data.Punto
import com.tubus.app.data.Ruta
import org.junit.Assert.assertEquals
import org.junit.Test

class EtaTest {
    private val ruta = Ruta(
        id = 1, nombre = "Ruta de prueba", descripcion = "", colorInt = 0,
        trazado = listOf(Punto(5.5300, -73.3700), Punto(5.5400, -73.3700), Punto(5.5500, -73.3700)),
        paraderos = listOf(
            Paradero("A", 5.5300, -73.3700, 0),
            Paradero("B", 5.5400, -73.3700, 1),
            Paradero("C", 5.5500, -73.3700, 2)
        ),
        refParadero = 1, numBuses = 1
    )

    private fun bus(pos: Double, detenido: Int = 0) = Bus("B1", 1, pos, 50, detenido, 0.0, 0.0)

    @Test
    fun busEnElOrigenLlegaAlSegundoParaderoSegunLaDistancia() {
        val esperado = ruta.distanciaParadero(1) / Eta.VELOCIDAD
        assertEquals(esperado, Eta.segundos(bus(0.0), ruta, 1), 0.001)
    }

    @Test
    fun busQueYaPasoElParaderoLoAlcanzaDandoLaVuelta() {
        // Justo después del paradero B: debe completar el ciclo y volver a pasar por B.
        val pos = ruta.distanciaParadero(1) + 10
        val esperado = (ruta.longitud - 10) / Eta.VELOCIDAD
        assertEquals(esperado, Eta.segundos(bus(pos), ruta, 1), 0.001)
    }

    @Test
    fun busDetenidoSumaSuTiempoDeEspera() {
        val base = Eta.segundos(bus(0.0), ruta, 1)
        assertEquals(base + 20, Eta.segundos(bus(0.0, detenido = 20), ruta, 1), 0.001)
    }

    @Test
    fun elPuntoDelRecorridoCoincideConLosParaderos() {
        val p = ruta.puntoEn(ruta.distanciaParadero(1))
        assertEquals(5.5400, p.lat, 0.0001)
        assertEquals(-73.3700, p.lng, 0.0001)
    }
}
