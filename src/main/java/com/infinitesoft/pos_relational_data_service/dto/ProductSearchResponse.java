package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResponse {
    private Page<Product> page;
    private Double percentFromTotal;
}
