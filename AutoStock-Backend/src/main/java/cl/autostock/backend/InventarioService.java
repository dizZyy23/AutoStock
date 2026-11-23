package cl.autostock.backend;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@Service
public class InventarioService {
 private final ProductoRepository productos;
 private final MovimientoRepository movimientos;
 public InventarioService(ProductoRepository productos,MovimientoRepository movimientos) { this.productos=productos;this.movimientos=movimientos; }
 public List<Producto> listar() { return productos.findAll(); }
 public List<Movimiento> historial() { return movimientos.findAllByOrderByFechaDesc(); }
 public List<Producto> reposicion() { return productos.findAll().stream().filter(p->p.stock<=p.minimo).toList(); }
 @Transactional public Producto crear(Producto p) {
  if(productos.existsByCodigoIgnoreCase(p.codigo)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Código duplicado");
  p.id=null; validar(p); return productos.save(p);
 }
 @Transactional public Producto editar(long id, Producto p) {
  Producto actual=buscar(id);
  if(!actual.codigo.equalsIgnoreCase(p.codigo) && productos.existsByCodigoIgnoreCase(p.codigo)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Código duplicado");
  validar(p); p.id=id; return productos.save(p);
 }
 @Transactional public void eliminar(long id) {
  buscar(id);
  // Conservar trazabilidad de movimientos: eliminar no borra el historial.
  productos.deleteById(id);
 }
 public Producto buscar(long id) { return productos.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Producto no encontrado")); }
 private void validar(Producto p) {
  if(p.codigo==null || p.codigo.isBlank() || p.nombre==null || p.nombre.isBlank() || p.stock<0 || p.minimo<0 || p.objetivo<0 || p.precio<0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Datos de producto inválidos");
 }
 public record SolicitudMovimiento(Long productoId,String tipo,int cantidad,String motivo) {}
 @Transactional public Movimiento registrar(SolicitudMovimiento solicitud) {
  if(solicitud.productoId()==null || solicitud.tipo()==null || solicitud.cantidad()<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Movimiento inválido");
  Producto producto=buscar(solicitud.productoId());
  boolean ingreso=switch(solicitud.tipo()) {case "Ingreso","Devolución"->true; case "Venta","Consumo","Ajuste"->false; default->throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tipo no válido");};
  int anterior=producto.stock;
  long nuevo=(long)anterior+(ingreso?solicitud.cantidad():-solicitud.cantidad());
  if(nuevo<0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Stock insuficiente");
  if(nuevo>Integer.MAX_VALUE)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cantidad fuera de rango");
  producto.stock=(int)nuevo;productos.save(producto);
  Movimiento mov=new Movimiento();mov.productoId=producto.id;mov.tipo=solicitud.tipo();mov.cantidad=solicitud.cantidad();mov.anterior=anterior;mov.resultante=producto.stock;mov.motivo=solicitud.motivo();
  return movimientos.save(mov);
 }
}
