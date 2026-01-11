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
    private String serviceHost;

    @NotBlank
    private String restScheme;

//    @NotBlank
//    private String restHost = "localhost";

    private int restPort;

    @NotBlank
    private String restBasePath = "/api";

//    @NotBlank
//    private String grpcHost = "localhost";

    private int grpcPort;

    // TODO
    private long pollIntervalMs = 5000L;

    private long busyWaitIntervalMs = 1000L;
}
