package cl.autostock.backend;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api")
public class InventarioController {
 private final InventarioService service;
 public InventarioController(InventarioService service) {this.service=service;}
 @GetMapping("/productos") public List<Producto> productos(){return service.listar();}
 @GetMapping("/productos/{id}") public Producto obtener(@PathVariable long id){return service.buscar(id);}
 @PostMapping("/productos") @ResponseStatus(HttpStatus.CREATED) public Producto crear(@Valid @RequestBody Producto p){return service.crear(p);}
 @PutMapping("/productos/{id}") public Producto editar(@PathVariable long id,@Valid @RequestBody Producto p){return service.editar(id,p);}
 @DeleteMapping("/productos/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void eliminar(@PathVariable long id){service.eliminar(id);}
 @GetMapping("/movimientos") public List<Movimiento> movimientos(){return service.historial();}
 @PostMapping("/movimientos") @ResponseStatus(HttpStatus.CREATED) public Movimiento movimiento(@RequestBody InventarioService.SolicitudMovimiento m){return service.registrar(m);}
 @GetMapping("/reposicion") public List<Producto> reposicion(){return service.reposicion();}
}
