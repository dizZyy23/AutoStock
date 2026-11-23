package cl.autostock.backend;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="movimientos")
public class Movimiento {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long productoId;
 public String tipo;
 public int cantidad;
 public int anterior;
 public int resultante;
 public String motivo;
 public LocalDateTime fecha=LocalDateTime.now();
 public Movimiento() {}
}
