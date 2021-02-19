package com.rbkmoney.orgmanager.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "${scheduler.invitation.lockFor}")
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(JdbcTemplate postgresJdbcTemplate) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(postgresJdbcTemplate)
                        .withTableName("org_manager.shedlock")
                        .usingDbTime()
                        .build()
        );
    }

}
