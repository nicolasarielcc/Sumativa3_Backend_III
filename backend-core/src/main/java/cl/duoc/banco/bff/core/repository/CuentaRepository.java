package cl.duoc.banco.bff.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.banco.bff.core.entity.Cuenta;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
}
