package com.test.order.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.test.order.controller.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class AppExceptionHandler extends ResponseEntityExceptionHandler {
    @Value("${spring.application.name}")
    private String applicationName;

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> baseExceptionHandler(HttpServletRequest req, Exception e) {
        return ResponseEntity.internalServerError().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ErrorResponse> runtimeExceptionHandler(HttpServletRequest req, RuntimeException e) {
        return ResponseEntity.unprocessableEntity().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<ErrorResponse> businessExceptionHandler(HttpServletRequest req, BusinessException e) {
        return ResponseEntity.unprocessableEntity().body(new ErrorResponse(e.getMessage() + Optional.ofNullable(e.getReason()).orElse("")));
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request
    ) {
        BindingResult bindingResult = ex.getBindingResult();

        StringBuilder stringBuilder = new StringBuilder();
        for (FieldError error : bindingResult.getFieldErrors()) {
            String field = error.getField();
            String msg = error.getDefaultMessage();
            String message =
                    String.format(
                            "Error Field： %s，Reason： %s",
                            field, msg);
            stringBuilder.append(message);
        }
        log.error(stringBuilder.toString(), ex);

        return ResponseEntity.badRequest().body(
                new ErrorResponse(stringBuilder.toString()));
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var allErrors = ex.getAllErrors();
        StringBuilder stringBuilder=new StringBuilder();
        for (var error : allErrors) {
            stringBuilder.append(error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(stringBuilder.toString()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                         HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        String errorMessage = "Invalid request: " + ex.getMessage();
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            if(ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String field = ife.getPath().get(0).getFieldName();
                String invalidValue = ife.getValue().toString();
                String validValues = Arrays.stream(ife.getTargetType().getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                errorMessage = String.format(
                        "Invalid value '%s' for field '%s'. Allowed values are: %s",
                        invalidValue, field, validValues);
            }
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(errorMessage));
    }


    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Object[] args = {ex.getPropertyName(), ex.getValue()};
        String defaultDetail = "Failed to convert '" + args[0] + "' with value: '" + args[1] + "'";
        return ResponseEntity.badRequest().body(new ErrorResponse(defaultDetail));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }


}
