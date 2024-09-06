package com.tim.projectmanagement.exception;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tim.projectmanagement.exception.custom.ApiException;
import com.tim.projectmanagement.model.HttpResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.io.OutputStream;
import java.util.Date;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class ErrorHandler {

    public static void processError(HttpServletResponse response, Exception exception) {
        if(exception instanceof ApiException || exception instanceof DisabledException || exception instanceof LockedException ||
                exception instanceof BadCredentialsException || exception instanceof InvalidClaimException) {
            HttpResponse httpResponse = createHttpResponse(exception.getMessage(), BAD_REQUEST);
            buildResponse(response, httpResponse);
        } else {
            HttpResponse httpResponse = createHttpResponse("An error occurred. Please try again", INTERNAL_SERVER_ERROR);
            buildResponse(response, httpResponse);
        }
    }

    private static void buildResponse(HttpServletResponse response, HttpResponse httpResponse) {
        OutputStream outputStream;
        try {
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setStatus(UNAUTHORIZED.value());

            outputStream = response.getOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(
                    outputStream,
                    httpResponse
            );
            outputStream.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private static HttpResponse createHttpResponse(String message, HttpStatus httpStatus) {
        return HttpResponse.builder()
                .timeStamp(new Date().toString())
                .statusCode(httpStatus.value())
                .status(httpStatus)
                .reason(message)
                .build();
    }
}
