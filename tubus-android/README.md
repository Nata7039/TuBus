# TuBus – prototipo Android

App en Kotlin + Jetpack Compose (Material 3) con mapa de OpenStreetMap (osmdroid, sin clave de API).
Los buses son **simulados**: se mueven por cada ruta, con tiempo de llegada y ocupación calculados.

## Cómo abrirlo
1. Android Studio (Koala o superior) → *Open* → elegir esta carpeta. Dejar que sincronice Gradle (necesita internet la primera vez).
2. Ejecutar en un emulador o celular con Android 7.0 (API 24) o superior. El mapa necesita internet para descargar los mosaicos.
3. Pruebas unitarias: `./gradlew test` (cálculo de tiempos de llegada).

## Pantallas (según el mockup)
Splash → Inicio (búsqueda, accesos rápidos, rutas cercanas) → Mapa en vivo → Rutas → Detalle de ruta (paradas, tiempo, ocupación, favorita) → Favoritos → Alertas → Perfil (tamaño de letra y acerca de).

## Datos de rutas
Todo sale de `app/src/main/assets/rutas.json`, con las **26 rutas reales** de Tunja exportadas de utmiruta (Google My Maps).
- `trazado`: recorrido real por las calles (ida en azul seguido del regreso en rojo), como lista `[lat, lng]`. Los buses simulados lo recorren en circuito.
- `paraderos`: **virtuales**, uno cada ~450 m sobre el trazado y nombrados con la calle del tramo (los KMZ no traen paraderos reales). Cada uno tiene `n`, `lat`, `lng` e `i` (vértice del trazado). Reemplácelos por los reales cuando los tengan.
- Color, cantidad de buses simulados y paradero de referencia (`refParadero`) por ruta.

## Estructura
- `data/` modelos, carga del JSON, simulador y cálculo de tiempos de llegada, ViewModel.
- `ui/` componentes, pantallas y mapa.
- `MainActivity.kt` navegación y tamaño de letra.

## Pendiente para la versión real
Reemplazar el simulador por la posición real de los GPS (servidor), ocupación con el sensor contador, notificaciones push y cuentas Plus.

## Compilar sin Android Studio (GitHub)
Suba esta carpeta a un repositorio de GitHub; en la pestaña *Actions* se ejecuta "Compilar APK" y al terminar queda el archivo `TuBus-apk` para descargar (contiene `app-debug.apk`).
