package com.hulkhiretech.payment.service.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payment.constant.ErrorCodeEnum;
import com.hulkhiretech.payment.exception.ProcessingException;
import com.hulkhiretech.payment.http.HttpRequest;
import com.hulkhiretech.payment.http.HttpServiceEngine;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaypalProviderClient {

	private final HttpServiceEngine httpServiceEngine;

	@CircuitBreaker(name = "payment-processing-service",
			fallbackMethod = "fallbackMakeCall")
	public ResponseEntity<String> makeCall(HttpRequest httpRequest) {
		log.info("PaypalClient.makePaypalCall called httpRequest: {}", httpRequest);

		ResponseEntity<String> httpResponse = 
				httpServiceEngine.makeHttpCall(httpRequest);

		log.info("HttpServiceEngine.makeHttpCall httpResponse: {}", httpResponse);
		return httpResponse;
	}
	
	public ResponseEntity<String> fallbackMakeCall(
			HttpRequest httpRequest, Throwable t) {
		// Handle fallback logic here
		log.error("Fallback method called due to: {}", t.getMessage(), t);
		
		// if t is instance of PaymentValidationException, rethrow it with typecast
		if (t instanceof ProcessingException pve) {
			log.error("Rethrowing PaymentValidationException from fallback: {}", pve.getMessage(), pve);
	        throw pve;
	    }
		
		log.error("Returning fallback error response for HTTP call to URL: {}", httpRequest.getUrl());
		throw new ProcessingException(
				ErrorCodeEnum.UNABLE_TO_CONNECT_TO_PAYPAL_PROVIDER.getErrorCode(),
				ErrorCodeEnum.UNABLE_TO_CONNECT_TO_PAYPAL_PROVIDER.getErrorMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}


}
