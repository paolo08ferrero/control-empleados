package app.control.repository;

import app.control.model.RegistroFichaje;
import app.control.model.TipoFichaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroFichajeRepository extends JpaRepository<RegistroFichaje, Long> {

    // Último fichaje de un empleado sin importar el tipo
    Optional<RegistroFichaje> findTopByEmpleadoIdOrderByFechaHoraDesc(Long empleadoId);

    // Último registro de un empleado según tipo
    Optional<RegistroFichaje> findTopByEmpleadoIdAndTipoOrderByFechaHoraDesc(Long empleadoId, TipoFichaje tipo);

    // Fichajes de un empleado por ID, ordenados por fecha descendente
    List<RegistroFichaje> findByEmpleadoIdOrderByFechaHoraDesc(Long empleadoId);

    // Última ubicación de todos los empleados
    @Query("""
        SELECT r FROM RegistroFichaje r
        WHERE r.tipo = 'UBICACION' AND r.fechaHora = (
            SELECT MAX(r2.fechaHora) FROM RegistroFichaje r2
            WHERE r2.empleado.id = r.empleado.id AND r2.tipo = 'UBICACION'
        )
    """)
    List<RegistroFichaje> findUltimasUbicacionesDeEmpleados();

    // Todos los fichajes ordenados por fecha descendente
    @Query("SELECT r FROM RegistroFichaje r ORDER BY r.fechaHora DESC")
    List<RegistroFichaje> findAllOrderByFechaHoraDesc();

    // Fichajes de un empleado por fecha específica (opcional para todos los empleados si empleadoId es null)
    @Query("""
        SELECT r FROM RegistroFichaje r
        WHERE (:empleadoId IS NULL OR r.empleado.id = :empleadoId)
          AND FUNCTION('DATE', r.fechaHora) = :fecha
        ORDER BY r.fechaHora DESC
    """)
    List<RegistroFichaje> findByEmpleadoIdAndFecha(Long empleadoId, LocalDate fecha);

    // ---------------- NUEVO ----------------
    // Última ubicación activa antes de la SALIDA más reciente
    @Query("""
        SELECT r FROM RegistroFichaje r
        WHERE r.empleado.id = :empleadoId
          AND r.tipo = 'UBICACION'
          AND r.fechaHora = (
              SELECT MAX(r2.fechaHora) FROM RegistroFichaje r2
              WHERE r2.empleado.id = :empleadoId AND r2.tipo = 'UBICACION'
          )
    """)
    Optional<RegistroFichaje> findUltimaUbicacionActiva(Long empleadoId);
}
