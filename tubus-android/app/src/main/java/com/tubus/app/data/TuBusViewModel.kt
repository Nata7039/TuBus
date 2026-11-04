package com.tubus.app.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TuBusViewModel(app: Application) : AndroidViewModel(app) {

    val rutas: List<Ruta> = RutasRepo.cargar(app)
    private val simulador = Simulador(rutas)
    private val prefs = app.getSharedPreferences("tubus", Context.MODE_PRIVATE)

    private val _buses = MutableStateFlow(simulador.buses)
    val buses: StateFlow<List<Bus>> = _buses.asStateFlow()

    private val _favoritos = MutableStateFlow(
        (prefs.getStringSet("favoritos", emptySet()) ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
    )
    val favoritos: StateFlow<Set<Int>> = _favoritos.asStateFlow()

    private val _escala = MutableStateFlow(prefs.getFloat("escala", 1f))
    val escala: StateFlow<Float> = _escala.asStateFlow()

    init {
        // Cada segundo se mueven los buses simulados.
        viewModelScope.launch {
            while (true) {
                delay(1000)
                simulador.avanzar(1.0)
                _buses.value = simulador.buses
            }
        }
    }

    fun ruta(id: Int): Ruta = rutas.first { it.id == id }

    fun alternarFavorito(id: Int) {
        val nuevo = _favoritos.value.toMutableSet()
        if (!nuevo.add(id)) nuevo.remove(id)
        _favoritos.value = nuevo
        prefs.edit().putStringSet("favoritos", nuevo.map { it.toString() }.toSet()).apply()
    }

    fun cambiarEscala(valor: Float) {
        _escala.value = valor
        prefs.edit().putFloat("escala", valor).apply()
    }
}
