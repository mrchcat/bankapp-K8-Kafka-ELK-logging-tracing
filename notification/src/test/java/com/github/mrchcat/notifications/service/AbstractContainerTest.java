package com.github.mrchcat.notifications.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import skydrinker.testcontainers.mailcatcher.MailCatcherContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractContainerTest {

    public final static PostgreSQLContainer postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.4"));

    public final static GenericContainer<?> zipkin =
            new GenericContainer<>(DockerImageName.parse("openzipkin/zipkin:3.5"));

    public final static MailCatcherContainer mailcatcher =
            new MailCatcherContainer("dockage/mailcatcher:0.9.0");


    static {
        postgres.start();
        zipkin.start();
        mailcatcher.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractContainerTest::getPostgresUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("management.zipkin.tracing.endpoint", AbstractContainerTest::getZipkinEndpoint);

        registry.add("spring.mail.host", mailcatcher::getHost);
        registry.add("spring.mail.port", mailcatcher::getSmtpPort);
    }

    static String getZipkinEndpoint() {
        return String.format("http://%s:%s/api/v2/spans", zipkin.getHost(), 9411);
    }


    static String getPostgresUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s",
                postgres.getHost(),
                postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgres.getDatabaseName()
        );
    }
}
