package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboElectronico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HistorialReciboElectronicoRepository
        extends JpaRepository<HistorialReciboElectronico, Long> {

    Optional<HistorialReciboElectronico> findByHistorialReciboId(Long historialReciboId);

    Optional<HistorialReciboElectronico> findByAbonoCxcId(Long abonoCxcId);

    List<HistorialReciboElectronico> findByHistorialReciboIdIn(List<Long> historialReciboIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE notificacion_email_pago SET historial_recibo_electronico_id = NULL "
            + "WHERE historial_recibo_electronico_id IN ("
            + "SELECT id FROM historial_recibos_electronicos WHERE historial_recibo_id = :historialReciboId)",
            nativeQuery = true)
    int detachNotificaciones(@Param("historialReciboId") Long historialReciboId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM ticket_sin_notificacion WHERE historial_recibo_id = :historialReciboId",
            nativeQuery = true)
    int deleteTicketsSinNotificacion(@Param("historialReciboId") Long historialReciboId);

    void deleteByHistorialReciboId(Long historialReciboId);
}
