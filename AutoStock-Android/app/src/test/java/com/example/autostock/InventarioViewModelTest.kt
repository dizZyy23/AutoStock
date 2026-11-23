package com.example.autostock

import com.example.autostock.data.*
import com.example.autostock.viewmodel.InventarioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventarioViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private class FakeRepository : InventarioDataSource {
        override val productos = MutableStateFlow<List<Producto>>(emptyList())
        override val movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
        var iniciado = 0
        var guardado: Producto? = null
        var eliminado: Producto? = null
        var falloGuardar = false
        var errorMovimiento: String? = null
        var ultimoMovimiento: List<Any>? = null
        override suspend fun iniciarEjemplos() { iniciado++ }
        override suspend fun guardar(producto: Producto) {
            if (falloGuardar) throw IllegalStateException("Código repetido")
            guardado = producto
        }
        override suspend fun eliminar(producto: Producto) { eliminado = producto }
        override suspend fun registrar(id: Long, tipo: String, cantidad: Int, motivo: String): String? {
            ultimoMovimiento = listOf(id, tipo, cantidad, motivo)
            return errorMovimiento
        }
    }

    private val producto = Producto(codigo = "ACE-001", nombre = "Aceite")

    @Test fun iniciaDatosDemostracion() = runTest {
        val repo = FakeRepository()
        InventarioViewModel(repo)
        advanceUntilIdle()
        assertEquals(1, repo.iniciado)
    }

    @Test fun guardaProductoYConfirma() = runTest {
        val repo = FakeRepository()
        val vm = InventarioViewModel(repo)
        var confirmado = false
        vm.guardar(producto) { confirmado = true }
        advanceUntilIdle()
        assertEquals(producto, repo.guardado)
        assertEquals("Producto guardado", vm.mensaje.value)
        assertTrue(confirmado)
    }

    @Test fun muestraErrorSiGuardarFalla() = runTest {
        val repo = FakeRepository().apply { falloGuardar = true }
        val vm = InventarioViewModel(repo)
        var confirmado = false
        vm.guardar(producto) { confirmado = true }
        advanceUntilIdle()
        assertNull(repo.guardado)
        assertFalse(confirmado)
        assertTrue(vm.mensaje.value!!.startsWith("No se pudo guardar"))
    }

    @Test fun borraProducto() = runTest {
        val repo = FakeRepository()
        val vm = InventarioViewModel(repo)
        vm.borrar(producto)
        advanceUntilIdle()
        assertEquals(producto, repo.eliminado)
        assertEquals("Producto eliminado", vm.mensaje.value)
    }

    @Test fun registraMovimientoCorrecto() = runTest {
        val repo = FakeRepository()
        val vm = InventarioViewModel(repo)
        var confirmado = false
        vm.registrar(7, "Ingreso", 3, "Compra") { confirmado = true }
        advanceUntilIdle()
        assertEquals(listOf(7L, "Ingreso", 3, "Compra"), repo.ultimoMovimiento)
        assertEquals("Movimiento registrado", vm.mensaje.value)
        assertTrue(confirmado)
    }

    @Test fun reportaErrorDeMovimiento() = runTest {
        val repo = FakeRepository().apply { errorMovimiento = "Stock insuficiente" }
        val vm = InventarioViewModel(repo)
        var confirmado = false
        vm.registrar(7, "Venta", 100, "Venta") { confirmado = true }
        advanceUntilIdle()
        assertEquals("Stock insuficiente", vm.mensaje.value)
        assertFalse(confirmado)
    }

    @Test fun limpiaMensaje() = runTest {
        val repo = FakeRepository()
        val vm = InventarioViewModel(repo)
        vm.guardar(producto) {}
        advanceUntilIdle()
        assertNotNull(vm.mensaje.value)
        vm.limpiarMensaje()
        assertNull(vm.mensaje.value)
    }
}
