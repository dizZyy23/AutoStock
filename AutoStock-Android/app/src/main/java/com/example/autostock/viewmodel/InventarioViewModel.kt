package com.example.autostock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.autostock.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventarioViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(application, InventarioDatabase::class.java, "autostock.db").build()
    private val repository = InventarioRepository(db)
    val productos = repository.productos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val movimientos = repository.movimientos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init { viewModelScope.launch { repository.iniciarEjemplos() } }
    fun limpiarMensaje() { _mensaje.value = null }
    fun guardar(producto: Producto, alTerminar: () -> Unit) {
        viewModelScope.launch {
            try { repository.guardar(producto); _mensaje.value = "Producto guardado"; alTerminar() }
            catch (_: Exception) { _mensaje.value = "No se pudo guardar: verifica que el código no esté repetido" }
        }
    }
    fun borrar(producto: Producto) { viewModelScope.launch { repository.eliminar(producto); _mensaje.value = "Producto eliminado" } }
    fun registrar(id: Long, tipo: String, cantidad: Int, motivo: String, alTerminar: () -> Unit) {
        viewModelScope.launch {
            val error = repository.registrar(id,tipo,cantidad,motivo)
            if (error == null) { _mensaje.value = "Movimiento registrado"; alTerminar() } else _mensaje.value = error
        }
    }
}
