# preAlarm

App de Android (Kotlin + Jetpack Compose + Material 3) para salir de casa **puntual y sin agobios**.

## La idea

Sales a las 14:00 y antes tienes que hacer varias cosas, cada una con su tiempo. Con preAlarm:

1. Creas una rutina con las tareas **en orden** y cuánto tarda cada una.
2. La app calcula a qué hora tienes que **empezar** (hora de salida − tiempo total) y programa los avisos.
3. Cuando empieza la rutina, ves un **contador grande** para la tarea actual y una lista con los horarios previstos.
4. Al acabar cada periodo suena un **aviso sonoro breve** (no es una alarma: no hay que descartar nada) y aparece una notificación.
5. Cuando terminas una tarea, pulsas **«¡Hecho!»**. La app calcula si vas **en hora, adelantado o atrasado** y te lo muestra mientras corres de un lado a otro.

Los avisos se programan con `AlarmManager` exacto, así que suenan aunque la app esté cerrada o el móvil en reposo.

## Pantallas

- **Inicio**: lista de rutinas guardadas (nombre, hora de salida, duración total) con botón «Empezar».
- **Editor**: nombre, hora de salida (selector de hora Material 3) y tareas con duración en minutos, reordenables.
- **Sesión**: cuenta atrás hasta la salida, tarea actual con contador y progreso, estado «en hora / adelantado / atrasado», y resumen de tareas con horas previstas y reales.

## Diseño

Material 3 (Material You): color dinámico del sistema en Android 12+, tema claro/oscuro propio en versiones anteriores, formas muy redondeadas y una interfaz pensada para consultarse de un vistazo.

## Técnica

- Kotlin 2.0, Jetpack Compose (BOM), Material 3, minSdk 26 / targetSdk 35.
- MVVM ligero: un `ViewModel` + `StateFlow`, sin inyección de dependencias.
- Persistencia con DataStore Preferences + kotlinx.serialization (sin base de datos).
- Alarmas exactas con `SCHEDULE_EXACT_ALARM` (API 31–32) / `USE_EXACT_ALARM` (API 33+), con degradación a alarmas inexactas si no hay permiso.
- Ajustes: idioma por app, tema (sistema/claro/oscuro), colores dinámicos, sonido del aviso con previsualización, canal de audio (alarma o multimedia) y vibración.
- 8 idiomas: inglés (por defecto), español, català, Deutsch, français, italiano, 日本語, 中文.
- APK release con R8: ~2 MB.

## Compilar

```bash
./gradlew assembleDebug      # APK de prueba
./gradlew testDebugUnitTest  # tests unitarios de la lógica de horarios
```

Ábrelo también directamente en Android Studio (Ladybug o superior).

## Estructura

```
app/src/main/java/com/peudenhau/prealarm/
├── MainActivity.kt            # Navegación entre pantallas
├── data/                      # Modelos serializables + repositorio DataStore
├── schedule/                  # Cálculo del plan (horas de inicio/fin por tarea)
├── alarm/                     # AlarmScheduler (programación) + AlarmReceiver (sonido y notificación)
└── ui/                        # Tema Material 3, ViewModel y pantallas Compose
```
