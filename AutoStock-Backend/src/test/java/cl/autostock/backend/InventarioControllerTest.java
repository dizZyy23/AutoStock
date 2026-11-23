
package cl.autostock.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventarioControllerTest {

    private InventarioService service;
    private InventarioController controller;

    @BeforeEach
    void configurar() {
        service = mock(InventarioService.class);
        controller = new InventarioController(service);
    }

    @Test
    void listarProductos() {
        Producto producto = mock(Producto.class);

        when(service.listar()).thenReturn(List.of(producto));

        List<Producto> resultado = controller.productos();

        assertEquals(1, resultado.size());
        assertSame(producto, resultado.get(0));

        verify(service).listar();
    }

    @Test
    void obtenerProductoPorId() {
        Producto producto = mock(Producto.class);

        when(service.buscar(1L)).thenReturn(producto);

        Producto resultado = controller.obtener(1L);

        assertSame(producto, resultado);
        verify(service).buscar(1L);
    }

    @Test
    void crearProducto() {
        Producto producto = mock(Producto.class);

        when(service.crear(producto)).thenReturn(producto);

        Producto resultado = controller.crear(producto);

        assertSame(producto, resultado);
        verify(service).crear(producto);
    }

    @Test
    void editarProducto() {
        Producto producto = mock(Producto.class);

        when(service.editar(1L, producto))
                .thenReturn(producto);

        Producto resultado = controller.editar(1L, producto);

        assertSame(producto, resultado);
        verify(service).editar(1L, producto);
    }

    @Test
    void eliminarProducto() {
        controller.eliminar(1L);

        verify(service).eliminar(1L);
    }

    @Test
    void consultarMovimientos() {
        Movimiento movimiento = mock(Movimiento.class);

        when(service.historial())
                .thenReturn(List.of(movimiento));

        List<Movimiento> resultado = controller.movimientos();

        assertEquals(1, resultado.size());
        assertSame(movimiento, resultado.get(0));

        verify(service).historial();
    }

    @Test
    void registrarMovimiento() {
        InventarioService.SolicitudMovimiento solicitud =
                mock(InventarioService.SolicitudMovimiento.class);

        Movimiento movimiento = mock(Movimiento.class);

        when(service.registrar(solicitud))
                .thenReturn(movimiento);

        Movimiento resultado = controller.movimiento(solicitud);

        assertSame(movimiento, resultado);
        verify(service).registrar(solicitud);
    }

    @Test
    void consultarReposicion() {
        Producto producto = mock(Producto.class);

        when(service.reposicion())
                .thenReturn(List.of(producto));

        List<Producto> resultado = controller.reposicion();

        assertEquals(1, resultado.size());
        assertSame(producto, resultado.get(0));

        verify(service).reposicion();
    }
}
