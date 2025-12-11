package io.jgitkins.server.presentation.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public final class ResponseFactory {

    private ResponseFactory() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(Object resourceId, T body) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resourceId)
                .toUri();
        return ResponseEntity.created(location).body(ApiResponse.success(body));
    }
}
