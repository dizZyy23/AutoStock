# AutoStock — DSY1105

Aplicación académica de inventario para un taller automotriz ficticio de Puente Alto, desarrollada en Kotlin, Jetpack Compose, Material 3, MVVM y Room.

## Abrir en Android Studio
1. Descomprime el ZIP.
2. Abre Android Studio > **Open** y selecciona la carpeta `AutoStock`.
3. Instala Android SDK 36 si Android Studio lo solicita. Recomendado JDK 17.
4. Deja completar **Gradle Sync**. Se necesita conexión a internet la primera vez para descargar dependencias.
5. Ejecuta en un emulador Android 8.0+ o teléfono Android.

> IMPORTANTE: Este paquete NO incluye el binario oficial `gradle-wrapper.jar` ni los scripts `gradlew`, porque no fue posible obtenerlos desde el entorno de generación. Si tu Android Studio exige un Gradle Wrapper, usa un Gradle instalado para ejecutar `gradle wrapper --gradle-version 8.13`, o copia los archivos oficiales del wrapper de un proyecto Android recién creado con la misma versión de Gradle. No copies `local.properties` de otro equipo.

## Cuenta de demostración
- Usuario: `admin`
- PIN: `1234`

Esta autenticación es **solo de demostración**, no es adecuada para una aplicación real. No guardes datos personales reales.

## Funcionalidades implementadas en el código
- Panel de resumen con total de productos, alertas y movimientos.
- Catálogo de productos, búsqueda y filtro por categoría.
- Formulario con validaciones fuera de la vista y mensajes por campo.
- Alta, edición y eliminación confirmada de productos.
- Registro de ingresos, ventas, consumos, ajustes y devoluciones.
- Prevención de stock negativo y movimientos en transacciones de Room.
- Historial de movimientos con existencias anterior y resultante.
- Reposición por proveedor y cálculo `máximo(0, objetivo - stock)`.
- Apertura de cliente de correo con borrador y destinatario ficticio.
- Escáner de QR/códigos con ZXing (necesita dispositivo/cámara compatible).
- Animación de cambios en tarjetas y mensajes de errores.
- Base Room local con productos de demostración solo al primer inicio.

## Limitaciones y tareas pendientes antes de entrega
- **NO fue compilado ni probado en Android Studio dentro de este entorno**. Es imprescindible hacer Gradle Sync, compilar, ejecutar y corregir cualquier incompatibilidad según tu instalación.
- Inicio de sesión ficticio sin almacenamiento seguro, no usar en producción.
- Proveedores y ubicaciones están representados como campos del producto; sus formularios separados pueden añadirse para la EFT.
- El escáner requiere comprobar permisos de cámara y funcionamiento real en el dispositivo.
- El correo requiere una app de correo instalada; no envía mensajes automáticamente.
- Falta crear repositorio GitHub con commits genuinos de ambos estudiantes y planificación real en Trello.
- Revisar con el docente qué funciones del caso completo se exigirán en la EFT.

## Reparto de trabajo sugerido (pareja)
Integrante A: UI, navegación y formularios. Integrante B: Room, movimientos y reposición. Ambos: pruebas, GitHub, Trello y defensa.

## Referencias
Caso DSY1105 «Gestión móvil de inventario, ventas y reposición de productos automotrices» y rúbrica Evaluación Parcial 2 entregados por el estudiante.
