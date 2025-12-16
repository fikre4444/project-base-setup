package com.sample.sampleservice.shared.openfeign.infrastructure.secondary;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Error response: {} {}", response.status(), response.reason());
        return switch (response.status()) { // TODO - Handle specific error codes separately
            case 500, 401, 400, 403, 404, 409, 405, 302 -> handleError(response);
            default -> new Exception("Something went wrong");
        };
    }

    // private OpenFeignException handleError(Response response) {
    //     ErrorResponse errorResponse;
    //     try {
    //         log.info("bytes {} {}", response.body(), response.reason());
    //         if (response.body() != null) {
    //             byte[] bytes = IOUtils.toByteArray(response.body().asInputStream());
    //             errorResponse = objectMapper.readValue(bytes, ErrorResponse.class);
    //         } else {
    //             errorResponse = new ErrorResponse();
    //             errorResponse.setError_description(response.reason());
    //         }
    //     } catch (IOException e) {
    //         log.error("Failed while reading error response", e);
    //         errorResponse = new ErrorResponse();
    //         errorResponse.setError_description(response.reason());
    //     }
    //     errorResponse.setStatus(response.status());

    //     return new OpenFeignException(errorResponse, response.status());
    // }

    private OpenFeignException handleError(Response response) { 
        ErrorResponse errorResponse = null; 
        String responseBody = "No response body"; 
        try { 
            if (response.body() != null) { 
                responseBody = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8); 
                log.info("Error response body: {}", responseBody); 
                errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class); 
            } 
        } catch (IOException e) { 
            log.error("Failed while reading error response", e); 
        } 

        if (errorResponse == null) { 
            errorResponse = new ErrorResponse(); 
            errorResponse.setError_description(response.reason()); 
        } 
        errorResponse.setStatus(response.status()); 

        return new OpenFeignException(errorResponse, response.status()); 
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorResponse implements Serializable {

        private int status;

        private String error;

        private String error_description;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    @Slf4j
    public static class OpenFeignException extends RuntimeException {
        private final ErrorResponse errorResponse;
        private final Integer status;

        public OpenFeignException(ErrorResponse errorResponse, Integer status) {
            super(errorResponse.getError_description());
            this.errorResponse = errorResponse;
            this.status = status;
            log.error(errorResponse.getError_description());
        }
    }
}
