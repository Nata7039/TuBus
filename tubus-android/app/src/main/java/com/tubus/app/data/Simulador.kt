package com.tubus.app.data

import kotlin.math.roundToInt
import kotlin.random.Random

/** Cálculo de tiempos de llegada sobre el ciclo (ida + regreso) de cada ruta. */
object Eta {
    /** Velocidad media simulada de los buses, en metros por segundo (≈ 21,6 km/h). */
    const val VELOCIDAD = 6.0

    /** Segundos que faltan para que [b] pase por el paradero [idx] de su ruta. */
    fun segundos(b: Bus, r: Ruta, idx: Int): Double {
        val ciclo = r.longitud
        val d = r.distanciaParadero(idx)
        val falta = ((d - b.pos) % ciclo + ciclo) % ciclo
        return falta / VELOCIDAD + b.detenido
    }

    /** Minutos redondeados; 0 significa "llegando". */
    fun minutos(b: Bus, r: Ruta, idx: Int): Int = (segundos(b, r, idx) / 60.0).roundToInt()

    /** Bus de la ruta que llegará primero al paradero [idx], con sus minutos. */
    fun proximo(buses: List<Bus>, r: Ruta, idx: Int = r.refParadero): Pair<Bus, Int>? =
        buses.filter { it.rutaId == r.id }
            .minByOrNull { segundos(it, r, idx) }
            ?.let { it to minutos(it, r, idx) }

    /** Próximo paradero que alcanzará el bus y minutos que faltan. */
    fun proximaParada(b: Bus, r: Ruta): Pair<Paradero, Int> {
        var mejor = 0
        var mejorSeg = Double.MAX_VALUE
        for (i in r.paraderos.indices) {
            val s = segundos(b, r, i)
            if (s < mejorSeg) {
                mejorSeg = s
                mejor = i
            }
        }
        return r.paraderos[mejor] to (mejorSeg / 60.0).roundToInt()
    }
}

/** Genera y mueve buses ficticios sobre las rutas cargadas. */
class Simulador(private val rutas: List<Ruta>, semilla: Int = 7) {
    private val rnd = Random(semilla)

    var buses: List<Bus> = crear()
        private set

    private fun ruta(id: Int) = rutas.first { it.id == id }

    private fun crear(): List<Bus> = rutas.flatMap { r ->
        (0 until r.numBuses).map { k ->
            val ciclo = r.longitud
            val pos = (ciclo * k / r.numBuses + r.longitud * 0.1) % ciclo
            construir("R${r.id}-B${k + 1}", r, pos, 30 + rnd.nextInt(50), 0)
        }
    }

    private fun construir(id: String, r: Ruta, pos: Double, ocup: Int, det: Int): Bus {
        val p = r.puntoEn(pos)
        return Bus(id, r.id, pos, ocup, det, p.lat, p.lng)
    }

    /** Avanza la simulación [dt] segundos. */
    fun avanzar(dt: Double) {
        buses = buses.map { b ->
            val r = ruta(b.rutaId)
            var det = b.detenido
            var pos = b.pos
            if (det > 0) {
                det -= 1
            } else {
                pos = (pos + Eta.VELOCIDAD * dt) % r.longitud
                // Con poca probabilidad el bus se detiene unos segundos (tráfico o falla).
                if (rnd.nextInt(500) == 0) det = 20
            }
            val ocup = (b.ocupacion + rnd.nextInt(-2, 3)).coerceIn(5, 100)
            construir(b.id, r, pos, ocup, det)
        }
    }
}

/** Aviso mostrado en la pantalla de alertas. */
data class Alerta(val titulo: String, val detalle: String, val grave: Boolean)

object Alertas {
    fun generar(buses: List<Bus>, rutas: List<Ruta>): List<Alerta> {
        val lista = ArrayList<Alerta>()
        for (b in buses) {
            val r = rutas.firstOrNull { it.id == b.rutaId } ?: continue
            if (b.detenido > 0) {
                lista.add(Alerta("Bus detenido en ${r.nombre}", "El bus ${b.id} lleva unos segundos sin avanzar.", true))
            }
            if (b.ocupacion >= 80) {
                lista.add(Alerta("Ocupación alta en ${r.nombre}", "El bus ${b.id} va al ${b.ocupacion}% de su capacidad.", false))
            }
        }
        return lista
    }
}
