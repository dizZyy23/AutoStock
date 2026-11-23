package cl.autostock.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventarioServiceCoberturaTest {
    ProductoRepository productos;
    MovimientoRepository movimientos;
    InventarioService service;
    Producto original;

    @BeforeEach void iniciar() {
        productos=mock(ProductoRepository.class);
        movimientos=mock(MovimientoRepository.class);
        service=new InventarioService(productos,movimientos);
        original=producto("FIL-001", "Filtro", 5, 2);
        original.id=1L;
        when(productos.findById(1L)).thenReturn(Optional.of(original));
        when(productos.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));
        when(movimientos.save(any(Movimiento.class))).thenAnswer(i -> i.getArgument(0));
    }
    static Producto producto(String codigo,String nombre,int stock,int minimo) {
        Producto p=new Producto();p.codigo=codigo;p.nombre=nombre;p.stock=stock;p.minimo=minimo;
        p.objetivo=10;p.precio=1000;return p;
    }
    static void error(HttpStatus status, Runnable accion) {
        ResponseStatusException ex=assertThrows(ResponseStatusException.class,accion::run);
        assertEquals(status,ex.getStatusCode());
    }
    @Test void listaProductos() {
        when(productos.findAll()).thenReturn(List.of(original));
        assertEquals(1,service.listar().size());
    }
    @Test void historialOrdenado() {
        Movimiento m=new Movimiento();
        when(movimientos.findAllByOrderByFechaDesc()).thenReturn(List.of(m));
        assertSame(m,service.historial().get(0));
    }
    @Test void sinReposicionSiSuperaElMinimo() {
        when(productos.findAll()).thenReturn(List.of(original));
        assertTrue(service.reposicion().isEmpty());
    }
    @Test void crearValidoReiniciaId() {
        Producto p=producto("NUEVO","Filtro de aire",3,1);p.id=99L;
        assertNull(service.crear(p).id);
        verify(productos).save(p);
    }
    @Test void crearCodigoDuplicado() {
        Producto p=producto("DUP","Duplicado",2,1);
        when(productos.existsByCodigoIgnoreCase("DUP")).thenReturn(true);
        error(HttpStatus.CONFLICT,()->service.crear(p));
        verify(productos,never()).save(any());
    }
    @Test void crearSinCodigo() {
        error(HttpStatus.BAD_REQUEST,()->service.crear(producto("  ","Filtro",1,1)));
    }
    @Test void crearSinNombre() {
        error(HttpStatus.BAD_REQUEST,()->service.crear(producto("FIL"," ",1,1)));
    }
    @Test void crearConValoresNegativos() {
        Producto p=producto("FIL","Filtro",-1,0);
        error(HttpStatus.BAD_REQUEST,()->service.crear(p));
        p.stock=1;p.minimo=-1;
        error(HttpStatus.BAD_REQUEST,()->service.crear(p));
        p.minimo=1;p.objetivo=-1;
        error(HttpStatus.BAD_REQUEST,()->service.crear(p));
        p.objetivo=10;p.precio=-1;
        error(HttpStatus.BAD_REQUEST,()->service.crear(p));
    }
    @Test void editarProductoValido() {
        Producto nuevo=producto("FIL-001","Filtro mejorado",7,3);
        Producto resultado=service.editar(1L,nuevo);
        assertEquals(1L,resultado.id);
        assertEquals("Filtro mejorado",resultado.nombre);
        verify(productos).save(nuevo);
    }
    @Test void editarCodigoExistenteDeOtroProducto() {
        Producto nuevo=producto("OTRO","Filtro",7,2);
        when(productos.existsByCodigoIgnoreCase("OTRO")).thenReturn(true);
        error(HttpStatus.CONFLICT,()->service.editar(1L,nuevo));
    }
    @Test void editarSinCambiarCodigoNoExigeUnico() {
        Producto nuevo=producto("fil-001","Filtro",7,2);
        service.editar(1L,nuevo);
        verify(productos,never()).existsByCodigoIgnoreCase(anyString());
    }
    @Test void editarDatosInvalidos() {
        Producto nuevo=producto("FIL-001","Filtro",-5,2);
        error(HttpStatus.BAD_REQUEST,()->service.editar(1L,nuevo));
    }
    @Test void eliminarExistente() {
        service.eliminar(1L);verify(productos).deleteById(1L);
    }
    @Test void buscarYEliminarInexistente() {
        error(HttpStatus.NOT_FOUND,()->service.buscar(7L));
        error(HttpStatus.NOT_FOUND,()->service.eliminar(7L));
    }
    @Test void devolucionAumentaExistencias() {
        service.registrar(new InventarioService.SolicitudMovimiento(1L,"Devolución",2,"Devuelto"));
        assertEquals(7,original.stock);
    }
    @Test void consumoDescuentaExistencias() {
        service.registrar(new InventarioService.SolicitudMovimiento(1L,"Consumo",2,"Servicio"));
        assertEquals(3,original.stock);
    }
    @Test void ajusteDescuentaExistencias() {
        service.registrar(new InventarioService.SolicitudMovimiento(1L,"Ajuste",1,"Daño"));
        assertEquals(4,original.stock);
    }
    @Test void movimientoRegistraAntesYDespues() {
        Movimiento m=service.registrar(new InventarioService.SolicitudMovimiento(1L,"Ingreso",2,"Compra"));
        assertEquals(5,m.anterior);assertEquals(7,m.resultante);
        assertEquals("Ingreso",m.tipo);assertEquals("Compra",m.motivo);
    }
    @Test void movimientoSinIdOTipo() {
        error(HttpStatus.BAD_REQUEST,()->service.registrar(new InventarioService.SolicitudMovimiento(null,"Venta",1,"")));
        error(HttpStatus.BAD_REQUEST,()->service.registrar(new InventarioService.SolicitudMovimiento(1L,null,1,"")));
    }
    @Test void movimientoTipoDesconocido() {
        error(HttpStatus.BAD_REQUEST,()->service.registrar(new InventarioService.SolicitudMovimiento(1L,"REGALO",1,"")));
    }
    @Test void movimientoCantidadNegativa() {
        error(HttpStatus.BAD_REQUEST,()->service.registrar(new InventarioService.SolicitudMovimiento(1L,"Ingreso",-1,"")));
    }
    @Test void movimientoSobreflujoEntero() {
        original.stock=Integer.MAX_VALUE;
        error(HttpStatus.BAD_REQUEST,()->service.registrar(new InventarioService.SolicitudMovimiento(1L,"Ingreso",1,"")));
        assertEquals(Integer.MAX_VALUE,original.stock);
    }
    @Test void movimientoIdInexistente() {
        error(HttpStatus.NOT_FOUND,()->service.registrar(new InventarioService.SolicitudMovimiento(7L,"Ingreso",1,"")));
    }
}
