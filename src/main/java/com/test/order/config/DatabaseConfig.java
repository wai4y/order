package com.test.order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(value ={"com.test.order.repository"})
@EnableTransactionManagement
public class DatabaseConfig {

}
