package io.jgitkins.server.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
//import org.springframework.validation.annotation.Validated;

@Getter
@Setter
//@Validated
@Component
@ConfigurationProperties("jgitkins.server.runtime")
public class RunnerRuntimeProperties {

    @NotBlank
    private String restHost = "localhost";

    @Min(1)
    private int restPort = 8084;

    @NotBlank
    private String restBasePath = "/api";

    @NotBlank
    private String grpcHost = "localhost";

    @Min(1)
    private int grpcPort = 9090;

    @Min(1)
    private long pollIntervalMs = 5000L;

    @Min(1)
    private long busyWaitIntervalMs = 1000L;
}
