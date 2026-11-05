# ACIS · Gestión de Órdenes de Trabajo en Terreno

Proyecto semestral — DSY1105 Desarrollo de Aplicaciones Móviles (Android, Kotlin + Jetpack Compose).

## Integrantes
- Sebastián Escalona
- Gerald Cortés

## Arquitectura
Estructura **MVVM** (Guía 7):
- `ui/` — pantallas (View, Compose) y componentes
- `viewmodel/` — estado y lógica por pantalla (StateFlow)
- `model/` — entidades/modelos (incluye ejemplo Room)
- `repository/` — acceso a datos (DatosDemo / base para Room)
- `data/` — modelos y datos de demostración actuales

## Stack
- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- ViewModel + StateFlow
- Room (configurado como base para persistencia local, integración pendiente)
- minSdk 24, compile/targetSdk 37

## Ejecución
1. Abrir la carpeta `ACIS` en Android Studio.
2. Verificar `local.properties` con la ruta de tu Android SDK (no se versiona).
3. Sync Gradle y ejecutar en emulador/dispositivo (API 24+).

## Perfiles de prueba (prototipo)
- Técnico: `tecnico@acis.cl` / `1234`
- Planificador: `planificador@acis.cl` / `1234`
- Cliente: `cliente@acis.cl` / `1234`

## Notas
- Prototipo con datos en memoria (`DatosDemo`); persistencia local con Room en integración progresiva.
- `local.properties`, `build/`, `.gradle/` y `.idea/` están excluidos por `.gitignore`.
