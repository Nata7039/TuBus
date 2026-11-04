package com.tubus.app.data

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Punto geográfico simple. */
data class Punto(val lat: Double, val lng: Double)

/** [idx] es el índice del vértice del trazado donde queda el paradero. */
data class Paradero(val nombre: String, val lat: Double, val lng: Double, val idx: Int = 0)

object Geo {
    /** Distancia en metros entre dos puntos (fórmula de haversine). */
    fun distancia(a: Paradero, b: Paradero): Double = distancia(Punto(a.lat, a.lng), Punto(b.lat, b.lng))

    fun distancia(a: Punto, b: Punto): Double {
        val radio = 6371000.0
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLng = Math.toRadians(b.lng - a.lng)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLng / 2).pow(2)
        return 2 * radio * asin(sqrt(h))
    }
}

/**
 * Ruta de bus. [trazado] es el recorrido real por las calles: primero la ida y luego el regreso.
 * Se trata como un circuito cerrado: al terminar el regreso el bus vuelve a empezar la ida.
 * Los paraderos van sobre el trazado (cada uno apunta a un vértice mediante [Paradero.idx]).
 */
class Ruta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val colorInt: Int,
    val trazado: List<Punto>,
    val paraderos: List<Paradero>,
    val refParadero: Int,
    val numBuses: Int
) {
    /** Vértices del circuito cerrado (el último vuelve al primero). */
    private val circuito: List<Punto> = trazado + trazado.first()

    /** Distancia acumulada (m) desde el inicio hasta cada vértice del circuito. */
    val acum: DoubleArray = DoubleArray(circuito.size).also { a ->
        for (i in 1 until circuito.size) a[i] = a[i - 1] + Geo.distancia(circuito[i - 1], circuito[i])
    }

    /** Longitud total del ciclo (ida + regreso), en metros. */
    val longitud: Double get() = acum.last()

    /** Distancia (m) desde el inicio del ciclo hasta el paradero [i]. */
    fun distanciaParadero(i: Int): Double = acum[paraderos[i].idx.coerceIn(0, acum.size - 1)]

    /** Punto del circuito a [d] metros del inicio; [d] se toma módulo la longitud. */
    fun puntoEn(d: Double): Punto {
        val dist = ((d % longitud) + longitud) % longitud
        var i = 1
        while (i < acum.size - 1 && acum[i] < dist) i++
        val tramo = acum[i] - acum[i - 1]
        val f = if (tramo <= 0.0) 0.0 else ((dist - acum[i - 1]) / tramo).coerceIn(0.0, 1.0)
        val a = circuito[i - 1]
        val b = circuito[i]
        return Punto(a.lat + (b.lat - a.lat) * f, a.lng + (b.lng - a.lng) * f)
    }
}

/**
 * Bus simulado.
 * [pos] es la posición en el ciclo (ida + regreso), de 0 a longitud de la ruta.
 */
data class Bus(
    val id: String,
    val rutaId: Int,
    val pos: Double,
    val ocupacion: Int,
    val detenido: Int,
    val lat: Double,
    val lng: Double
)
