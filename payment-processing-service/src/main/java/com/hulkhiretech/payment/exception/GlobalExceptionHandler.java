package com.hulkhiretech.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hulkhiretech.payment.constant.ErrorCodeEnum;
import com.hulkhiretech.payment.pojo.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * GlobalExceptionHandler - Global exception handler for REST APIs
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/**
	 * Handle PaypalProviderException
	 * 
	 * @param ex PaypalProviderException
	 * @return ResponseEntity with ErrorResponse and HttpStatus
	 */
	@ExceptionHandler(ProcessingException.class)
	public ResponseEntity<ErrorResponse> handlePaypalProviderException(ProcessingException ex) {
		log.error("PaypalProviderException occurred: errorCode={}, errorMessage={}, httpStatus={}", 
				ex.getErrorCode(), ex.getErrorMessage(), ex.getHttpStatus());
		
		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(ex.getErrorCode());
		errorResponse.setErrorMessage(ex.getErrorMessage());
		
		return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
	}
	
	/**
	 * Handle Exception
	 * 
	 * @param ex Exception
	 * @return ResponseEntity with ErrorResponse and HttpStatus
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(ErrorCodeEnum.GENERIC_ERROR.getErrorCode());
		errorResponse.setErrorMessage(ErrorCodeEnum.GENERIC_ERROR.getErrorMessage());
		
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
