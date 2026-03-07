//package com.b0cka.exceptionHandler;
//
//import com.b0cka.dto.ErrorResponse;
//import com.b0cka.ex.BalanceError;
//import com.b0cka.ex.NotFoundImageException;
//import com.b0cka.ex.RedisException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//    @ExceptionHandler(RedisException.class)
//    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(RedisException ex) {
//        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
//        error.setTimestamp(LocalDateTime.now().format(customFormatter));
//        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(BalanceError.class)
//    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(BalanceError ex) {
//        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
//        error.setTimestamp(LocalDateTime.now().format(customFormatter));
//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
//    }
//
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
//        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred");
//        error.setTimestamp(LocalDateTime.now().format(customFormatter));
//        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @ExceptionHandler(NotFoundImageException.class)
//    public ResponseEntity<ErrorResponse> handleResourceNotFoundImageException(NotFoundImageException ex) {
//        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
//        error.setTimestamp(LocalDateTime.now().format(customFormatter));
//        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//    }
//}
