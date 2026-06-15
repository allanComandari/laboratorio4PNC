package com.server.app.repositories;

import com.server.app.entities.Abono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AbonoRepository extends JpaRepository<Abono, Integer> {
    List<Abono> findByPlanPagoId(int planPagoId);
}
