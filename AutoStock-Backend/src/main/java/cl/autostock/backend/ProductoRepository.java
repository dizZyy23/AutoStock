package cl.autostock.backend;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductoRepository extends JpaRepository<Producto,Long> { boolean existsByCodigoIgnoreCase(String codigo); }
