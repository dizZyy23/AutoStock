package com.example.autostock.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado y errores desacoplados de los composables.
data class EstadoServidor(
    val productos: List<ProductoRemoto> = emptyList(),
    val movimientos: List<MovimientoRemoto> = emptyList(),
    val proveedores: List<ProveedorExterno> = emptyList(),
    val cargando: Boolean = false,
    val mensaje: String = ""
)

class ServidorViewModel : ViewModel() {
    private val _estado = MutableStateFlow(EstadoServidor())
    val estado = _estado.asStateFlow()
    init { actualizar() }

    fun actualizar() = viewModelScope.launch {
        _estado.value = _estado.value.copy(cargando = true, mensaje = "")
        try {
            val productos = ApiFactory.inventario.productos()
            val movimientos = ApiFactory.inventario.movimientos()
            _estado.value = _estado.value.copy(productos = productos, movimientos = movimientos, cargando = false)
        } catch (ex: Exception) {
            _estado.value = _estado.value.copy(cargando = false, mensaje = "No se pudo conectar a Spring Boot: ${ex.localizedMessage}")
        }
    }

    fun cargarProveedores() = viewModelScope.launch {
        _estado.value = _estado.value.copy(cargando = true)
        try {
            _estado.value = _estado.value.copy(proveedores = ApiFactory.proveedores.proveedores(), cargando = false, mensaje = "API externa consultada")
        } catch (ex: Exception) {
            _estado.value = _estado.value.copy(cargando = false, mensaje = "Error API externa: ${ex.localizedMessage}")
        }
    }

    fun guardar(producto: ProductoRemoto) = ejecutar {
        if (producto.id == null) ApiFactory.inventario.crear(producto)
        else ApiFactory.inventario.editar(producto.id, producto)
    }
    fun borrar(id: Long) = ejecutar { ApiFactory.inventario.eliminar(id) }
    fun movimiento(productoId: Long, tipo: String, cantidad: Int) = ejecutar {
        ApiFactory.inventario.registrar(NuevoMovimiento(productoId, tipo, cantidad))
    }
    private fun ejecutar(accion: suspend () -> Unit) = viewModelScope.launch {
        _estado.value = _estado.value.copy(cargando = true, mensaje = "")
        try { accion(); actualizar() }
        catch (ex: Exception) { _estado.value = _estado.value.copy(cargando = false, mensaje = ex.localizedMessage ?: "Error inesperado") }
    }
}
