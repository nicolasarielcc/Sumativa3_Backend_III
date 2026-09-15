package cl.duoc.banco.bff.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.banco.bff.core.entity.Transaccion;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByCuentaIdOrderByFechaDesc(Long cuentaId);
}
