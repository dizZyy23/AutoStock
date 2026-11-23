# AutoStock

**Desarrollo de Aplicaciones Móviles (DSY1105)**  
**Integrantes:** Brayan Pino y Gabriel Millán

## Nuestro proyecto

Estamos desarrollando **AutoStock**, una aplicación Android para apoyar la gestión del inventario de un taller mecánico. Tomamos como referencia un taller pequeño de Puente Alto, donde una misma persona debe revisar repuestos, registrar entradas y salidas, y organizar las compras cuando faltan productos.

Buscamos que el inventario sea fácil de consultar desde el teléfono, sin depender de anotaciones ni de recordar dónde quedó cada repuesto. Este proyecto es un prototipo académico y trabaja con información ficticia.

## ¿Qué estamos construyendo?

La aplicación reúne un catálogo de productos con sus cantidades, categorías y ubicaciones, además del registro de movimientos como ingresos, ventas y consumos en servicios mecánicos. También contempla alertas cuando las existencias están bajas y una propuesta de reposición agrupada por proveedor.

Sumamos la lectura de códigos QR para agilizar el ingreso de productos. La idea es que, al escanear un QR con datos compatibles, podamos revisar la información en un formulario antes de guardarla. Así evitamos crear registros sin que el usuario los confirme.

## Tecnologías que utilizamos

- **Kotlin y Android Studio** para el desarrollo de la aplicación.
- **Jetpack Compose y Material 3** para la interfaz.
- **MVVM** para separar las pantallas de la lógica de la aplicación.
- **Room** para almacenar productos y movimientos de manera local.
- **CameraX y ML Kit** para la lectura de códigos QR.
- **Intents de Android** para preparar un correo de reposición desde una aplicación de correo instalada.

## Avance del proyecto

En esta versión incorporamos código para el acceso de demostración, la consulta y administración de productos, los movimientos de inventario, las alertas de reposición y el lector QR. También añadimos validaciones para los formularios y controles destinados a evitar cantidades negativas o códigos de producto duplicados.

**Estado actual:** el código todavía requiere sincronización, compilación y pruebas en Android Studio y en un dispositivo o emulador. En particular, debemos comprobar los permisos de cámara, la lectura del QR y el guardado de productos antes de dar estas funciones por terminadas.

Para esta etapa, los proveedores y las ubicaciones se representan dentro de los datos de cada producto. Más adelante podremos ampliar esos módulos según las instrucciones de la Evaluación Final Transversal.

## Datos de prueba

Usamos un usuario y PIN ficticios, además de productos y proveedores de ejemplo. La app no está pensada para manejar datos personales ni operar en un taller real en su estado actual.

## Próximos avances

Queremos seguir mejorando la experiencia de uso, probar los distintos recorridos de la aplicación y completar las funcionalidades que se soliciten en las siguientes evaluaciones. Mantenemos el proyecto como una base que podremos ampliar sin empezar desde cero.
