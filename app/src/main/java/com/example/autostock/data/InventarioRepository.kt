package com.example.autostock.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class InventarioRepository(private val db: InventarioDatabase) {
    private val dao = db.dao()
    val productos: Flow<List<Producto>> = dao.observarProductos()
    val movimientos: Flow<List<Movimiento>> = dao.observarMovimientos()

    suspend fun iniciarEjemplos() {
        if (dao.contar() != 0) return
        listOf(
            Producto(codigo="FIL-001", nombre="Filtro de aceite", categoria="Filtros", marca="Bosch", stock=8, minimo=3, objetivo=12, precio=8500.0, proveedor="Repuestos Cordillera", ubicacion="Bodega A - E2/R1"),
            Producto(codigo="ACE-002", nombre="Aceite motor 5W-30", categoria="Lubricantes", marca="DemoOil", stock=2, minimo=4, objetivo=12, precio=26990.0, proveedor="Lubricantes Sur", ubicacion="Bodega A - E1/R2"),
            Producto(codigo="FRE-003", nombre="Líquido de frenos DOT4", categoria="Fluidos", marca="AutoFluid", stock=0, minimo=2, objetivo=8, precio=7990.0, proveedor="Lubricantes Sur", ubicacion="Bodega B - E1/R1"),
            Producto(codigo="LIM-004", nombre="Limpiador de contactos", categoria="Limpieza", marca="ElectroClean", stock=15, minimo=5, objetivo=20, precio=5990.0, proveedor="Repuestos Cordillera", ubicacion="Bodega A - E3/R2")
        ).forEach { dao.insertarProducto(it) }
    }

    suspend fun guardar(producto: Producto) {
        if (producto.id == 0L) dao.insertarProducto(producto) else dao.actualizarProducto(producto)
    }
    suspend fun eliminar(producto: Producto) = dao.borrarProducto(producto)

    suspend fun registrar(id: Long, tipo: String, cantidad: Int, motivo: String): String? = db.withTransaction {
        val actual = dao.obtener(id) ?: return@withTransaction "No se encontró el producto"
        if (cantidad <= 0) return@withTransaction "La cantidad debe ser mayor que cero"
        val cambio = if (tipo == "Ingreso" || tipo == "Devolución") cantidad else -cantidad
        val nuevo = actual.stock + cambio
        if (nuevo < 0) return@withTransaction "Stock insuficiente. Disponible: ${actual.stock}"
        dao.actualizarProducto(actual.copy(stock = nuevo))
        dao.insertarMovimiento(Movimiento(productoId=id, nombreProducto=actual.nombre, tipo=tipo, cantidad=cantidad, anterior=actual.stock, resultante=nuevo, motivo=motivo))
        null
    }
}
