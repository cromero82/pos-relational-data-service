package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreBackupResponseDto {
    private boolean exito;
    private String mensaje;

    // Datos de autenticación
    private Integer rolesCreados;
    private Integer rolesActualizados;
    private Integer usuariosCreados;
    private Integer usuariosActualizados;
    private Integer perfilesCreados;
    private Integer perfilesActualizados;

    // Tablas sin relaciones
    private Integer clientesCreados;
    private Integer clientesActualizados;
    private Integer estadoRecibosCreados;
    private Integer estadoRecibosActualizados;
    private Integer metodoPagoCreados;
    private Integer metodoPagoActualizados;
    private Integer companyCreados;
    private Integer companyActualizados;
    private Integer configuracionAppCreados;
    private Integer configuracionAppActualizados;

    // Tablas con relaciones (en orden de dependencias)
    private Integer eventosCreados;
    private Integer eventosActualizados;
    private Integer productosCreados;
    private Integer productosActualizados;
    private Integer historialProductosCreados;
    private Integer historialProductosActualizados;
    private Integer sesionesCreadas;
    private Integer sesionesActualizadas;
    private Integer ticketsCreados;
    private Integer ticketsActualizados;
    private Integer ticketRecibosCreados;
    private Integer ticketRecibosActualizados;
    private Integer recibosCreados;
    private Integer recibosActualizados;
    private Integer historialRecibosCreados;
    private Integer historialRecibosActualizados;
    private Integer reciboDetallesCreados;
    private Integer reciboDetallesActualizados;
    private Integer historialReciboDetallesCreados;
    private Integer historialReciboDetallesActualizados;
    private Integer reciboDetalleHistoricosCreados;
    private Integer reciboDetalleHistoricosActualizados;
    private Integer edicionRecibosCreados;
    private Integer edicionRecibosActualizados;
    private Integer edicionReciboDetallesCreados;
    private Integer edicionReciboDetallesActualizados;
    private Integer tipoEgresosCreados;
    private Integer tipoEgresosActualizados;
    private Integer proveedoresCreados;
    private Integer proveedoresActualizados;
    private Integer egresosCreados;
    private Integer egresosActualizados;
    private Integer flujoDineroCreados;
    private Integer flujoDineroActualizados;
    private Integer tipoResultadoFinCreados;
    private Integer tipoResultadoFinActualizados;
    private Integer estadisticaFinCreados;
    private Integer estadisticaFinActualizados;
    private Integer corteVentasCreados;
    private Integer corteVentasActualizados;
    private Integer ventasTipoCreados;
    private Integer ventasTipoActualizados;
    private Integer bitacoraUsuariosCreados;
    private Integer bitacoraUsuariosActualizados;
    private Integer cargueProductosCreados;
    private Integer cargueProductosActualizados;
    private Integer cargueProductoConflictosCreados;
    private Integer cargueProductoConflictosActualizados;
    private Integer grupoEspejoCreados;
    private Integer grupoEspejoActualizados;
    private Integer productoEspejoCreados;
    private Integer productoEspejoActualizados;
}
