package cl.autostock.backend;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity @Table(name="productos",uniqueConstraints=@UniqueConstraint(columnNames="codigo"))
public class Producto {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @NotBlank public String codigo;
 @NotBlank public String nombre;
 public String categoria="Repuestos";
 public String marca="Genérica";
 @Min(0) public int stock;
 @Min(0) public int minimo=2;
 @Min(0) public int objetivo=10;
 @DecimalMin("0") public double precio;
 public String proveedor="Proveedor Demo";
 public String ubicacion="Bodega A";
 public Producto() {}
}
