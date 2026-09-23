package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Data;
import javax.persistence.Column;

@Data
public class ConfigurationAppRequest {
    @Column(name = "value", length = 1000, nullable = false)
    private String value;
}
