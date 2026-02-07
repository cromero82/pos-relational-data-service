package com.infinitesoft.pos_relational_data_service.entities;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "bitacora_usuario")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BitacoraUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "evento_id")
    private Integer eventoId;

    @Type(type = "jsonb")
    @Column(name = "valor_antes", columnDefinition = "jsonb")
    private String valorAntes;

    @Type(type = "jsonb")
    @Column(name = "valor_despues", columnDefinition = "jsonb")
    private String valorDespues;

    @Column(name = "referencia_id")
    private Integer referenciaId;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onPrePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
    }
}
