package com.infinitesoft.pos_relational_data_service.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Component
public class Log4jDataSourceConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        DbAppender.setDataSource(dataSource);
    }
}
