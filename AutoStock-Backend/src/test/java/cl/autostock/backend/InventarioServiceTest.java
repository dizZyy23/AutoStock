package cl.autostock.backend;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class InventarioServiceTest {
 ProductoRepository productos;
 MovimientoRepository movimientos;
 InventarioService service;
 Producto p;
 @BeforeEach void preparar() {
  productos=mock(ProductoRepository.class);movimientos=mock(MovimientoRepository.class);
  service=new InventarioService(productos,movimientos);
  p=new Producto();p.id=1L;p.codigo="FIL-001";p.nombre="Filtro";p.stock=5;p.minimo=2;
  when(productos.findById(1L)).thenReturn(Optional.of(p));
  when(movimientos.save(any(Movimiento.class))).thenAnswer(inv->inv.getArgument(0));
 }
 @Test void ventaRestaYRegistraHistorial() {
  Movimiento m=service.registrar(new InventarioService.SolicitudMovimiento(1L,"Venta",3,"Prueba"));
  assertEquals(2,p.stock);assertEquals(5,m.anterior);assertEquals(2,m.resultante);
  verify(productos).save(p);verify(movimientos).save(any());
 }
 @Test void ingresoAumentaStock() {
  service.registrar(new InventarioService.SolicitudMovimiento(1L,"Ingreso",4,"Compra"));assertEquals(9,p.stock);
 }
 @Test void noPermiteStockNegativo() {
  assertThrows(ResponseStatusException.class,()->service.registrar(new InventarioService.SolicitudMovimiento(1L,"Venta",6,"")));
  assertEquals(5,p.stock);verify(movimientos,never()).save(any());
 }
 @Test void noPermiteCantidadesCero() {
  assertThrows(ResponseStatusException.class,()->service.registrar(new InventarioService.SolicitudMovimiento(1L,"Ingreso",0,"")));
 }
 @Test void noPermiteCodigoDuplicado() {
  when(productos.existsByCodigoIgnoreCase("FIL-001")).thenReturn(true);
  assertThrows(ResponseStatusException.class,()->service.crear(p));
 }
 @Test void reposicionDetectaBajoMinimo() {
  when(productos.findAll()).thenReturn(java.util.List.of(p));p.stock=2;
  assertEquals(1,service.reposicion().size());
 }
}
