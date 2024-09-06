package com.tim.projectmanagement.utils;

import com.tim.projectmanagement.model.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

public class HttpResponseUtil {

    public static ResponseEntity<HttpResponse> createHttpResponseEntity(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .message(message)
                        .status(status)
                        .statusCode(status.value())
                        .build()
        );
    }
    public static ResponseEntity<HttpResponse> createHttpResponseEntityWithData(HttpStatus status, String message, Map<String, ?> data) {
        return ResponseEntity.status(status).body(
                HttpResponse.builder()
                        .timeStamp(LocalDateTime.now().toString())
                        .data(data)
                        .message(message)
                        .status(status)
                        .statusCode(status.value())
                        .build()
        );
    }
}
