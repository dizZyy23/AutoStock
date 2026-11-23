# AutoStock — Inventario automotriz

Estamos desarrollando AutoStock para apoyar el registro de productos, las entradas y salidas de stock y las solicitudes de reposición de un taller mecánico. El proyecto utiliza información ficticia.

En esta versión conservamos la aplicación Android con lectura QR, formularios, movimientos e inventario local con Room. También incorporamos un módulo separado para consultar y administrar productos a través de un backend Spring Boot. La sección «Servidor» se abre con el icono de nube en el inicio de la aplicación y permite demostrar solicitudes REST y operaciones CRUD.

La API externa se utiliza para consultar registros ficticios de proveedores de demostración. Para esta actividad utilizamos JSONPlaceholder, cuyos usuarios se muestran como contactos ficticios; no representan proveedores reales.

## Tecnologías

Kotlin, Jetpack Compose, Material 3, MVVM, Room, CameraX, ML Kit, Retrofit y Spring Boot con JPA y base de datos H2 de archivo.

## Módulos

- **AutoStock-Android:** aplicación móvil, inventario local, escaneo QR, navegación, módulo remoto y consulta a API externa.
- **AutoStock-Backend:** API REST propia con productos, movimientos y reposición, persistidos en H2.

## Endpoints

`GET /api/productos`, `GET /api/productos/{id}`, `POST /api/productos`, `PUT /api/productos/{id}`, `DELETE /api/productos/{id}`, `GET /api/movimientos`, `POST /api/movimientos`, `GET /api/reposicion`.

La API externa consultada es `GET https://jsonplaceholder.typicode.com/users`.

## Estado del avance

El inventario local y el módulo de servidor se mantienen separados en esta versión, por lo que todavía no existe sincronización automática entre Room y Spring Boot. Se incorporaron pruebas unitarias de validación y reglas de movimientos, pero la cobertura del 80 % debe medirse antes de afirmarla. La compilación, el APK firmado y las pruebas integrales quedan sujetos a la verificación en Android Studio y en el entorno de backend.
