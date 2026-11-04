package com.tubus.app.data

import android.content.Context
import android.graphics.Color
import org.json.JSONObject

/** Carga las rutas desde assets/rutas.json. Para usar datos reales basta con editar ese archivo. */
object RutasRepo {
    fun cargar(ctx: Context): List<Ruta> {
        val texto = ctx.assets.open("rutas.json").bufferedReader().use { it.readText() }
        val arr = JSONObject(texto).getJSONArray("rutas")
        val lista = ArrayList<Ruta>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val tz = o.getJSONArray("trazado")
            val trazado = ArrayList<Punto>()
            for (j in 0 until tz.length()) {
                val q = tz.getJSONArray(j)
                trazado.add(Punto(q.getDouble(0), q.getDouble(1)))
            }
            val ps = o.getJSONArray("paraderos")
            val paraderos = ArrayList<Paradero>()
            for (j in 0 until ps.length()) {
                val p = ps.getJSONObject(j)
                paraderos.add(Paradero(p.getString("n"), p.getDouble("lat"), p.getDouble("lng"), p.optInt("i", 0)))
            }
            lista.add(
                Ruta(
                    id = o.getInt("id"),
                    nombre = o.getString("nombre"),
                    descripcion = o.optString("descripcion", ""),
                    colorInt = Color.parseColor(o.getString("color")),
                    trazado = trazado,
                    paraderos = paraderos,
                    refParadero = o.optInt("refParadero", 0).coerceIn(0, paraderos.size - 1),
                    numBuses = o.optInt("buses", 2)
                )
            )
        }
        return lista
    }
}
