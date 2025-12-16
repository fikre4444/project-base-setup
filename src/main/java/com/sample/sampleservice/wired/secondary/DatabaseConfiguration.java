package com.sample.sampleservice.wired.secondary;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableJpaRepositories(basePackages = {"com.sample.sampleservice"}, enableDefaultTransactions = false)
class DatabaseConfiguration {
}
