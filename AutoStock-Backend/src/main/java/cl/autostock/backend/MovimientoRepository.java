package cl.autostock.backend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovimientoRepository extends JpaRepository<Movimiento,Long> { List<Movimiento> findAllByOrderByFechaDesc(); }
