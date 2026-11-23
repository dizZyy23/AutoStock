package com.example.autostock.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.autostock.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** La lógica no depende de Room; se puede sustituir el origen de datos en pruebas. */
class InventarioViewModel(private val repository: InventarioDataSource) : ViewModel() {
    val productos = repository.productos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val movimientos = repository.movimientos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init { viewModelScope.launch { repository.iniciarEjemplos() } }

    fun limpiarMensaje() { _mensaje.value = null }

    fun guardar(producto: Producto, alTerminar: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.guardar(producto)
                _mensaje.value = "Producto guardado"
                alTerminar()
            } catch (_: Exception) {
                _mensaje.value = "No se pudo guardar: verifica que el código no esté repetido"
            }
        }
    }

    fun borrar(producto: Producto) {
        viewModelScope.launch {
            repository.eliminar(producto)
            _mensaje.value = "Producto eliminado"
        }
    }

    fun registrar(id: Long, tipo: String, cantidad: Int, motivo: String, alTerminar: () -> Unit) {
        viewModelScope.launch {
            val error = repository.registrar(id, tipo, cantidad, motivo)
            if (error == null) {
                _mensaje.value = "Movimiento registrado"
                alTerminar()
            } else {
                _mensaje.value = error
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(InventarioViewModel::class.java))
                    val db = Room.databaseBuilder(
                        application, InventarioDatabase::class.java, "autostock.db"
                    ).build()
                    return InventarioViewModel(InventarioRepository(db)) as T
                }
            }
    }
}
