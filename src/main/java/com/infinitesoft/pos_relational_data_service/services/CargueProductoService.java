package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.CargueProducto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface CargueProductoService {
    List<CargueProducto> getAll();
    CargueProducto create(CargueProducto cargueProducto);
    CargueProducto processCargue(MultipartFile file, String nombreCargue);
}
