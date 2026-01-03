package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

@Entity
@Table(name = "cargue_producto_conflictos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CargueProductoConflicto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cargue_prod_id")
    private CargueProducto cargueProducto;

    @Column(name = "tipo_conflicto_id")
    private Integer tipoConflictoId;

    @Column(name = "nombre_producto")
    private String nombreProducto;

    @Type(type = "jsonb")
    @Column(name = "datos_conflicto", columnDefinition = "jsonb")
    private String datosConflicto;

    @Builder.Default
    private Boolean resuelto = false;

    public TipoConflicto getTipoConflicto() {
        return TipoConflicto.fromId(tipoConflictoId);
    }

    public void setTipoConflicto(TipoConflicto tipoConflicto) {
        this.tipoConflictoId = tipoConflicto.getId();
    }
}
