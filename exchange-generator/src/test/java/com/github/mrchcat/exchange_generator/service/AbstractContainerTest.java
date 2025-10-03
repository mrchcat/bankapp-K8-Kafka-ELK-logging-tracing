package com.github.mrchcat.exchange_generator.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractContainerTest {

    public final static GenericContainer<?> zipkin =
            new GenericContainer<>(DockerImageName.parse("openzipkin/zipkin:3.5"));

    static {
        zipkin.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("management.zipkin.tracing.endpoint", AbstractContainerTest::getZipkinEndpoint);
    }

    static String getZipkinEndpoint() {
        return String.format("http://%s:%s/api/v2/spans", zipkin.getHost(), 9411);
    }
}
