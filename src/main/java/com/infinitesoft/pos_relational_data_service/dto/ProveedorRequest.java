package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.validators.UniqueDocumento;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ProveedorRequest {

    @Size(max = 50, message = "El documento no puede tener más de 50 caracteres")
    @UniqueDocumento
    private String documento;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 150, message = "El nombre no puede tener más de 150 caracteres")
    private String nombre;

    @Size(max = 30, message = "El teléfono no puede tener más de 30 caracteres")
    private String telefono;

    @Size(max = 100, message = "El correo no puede tener más de 100 caracteres")
    private String correo;
}
