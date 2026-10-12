package com.hulkhiretech.payment.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.hulkhiretech.payment.constant.ErrorCodeEnum;
import com.hulkhiretech.payment.exception.ProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class HttpServiceEngine {

	private final RestClient restClient;

	public ResponseEntity<String> makeHttpCall(HttpRequest httpRequest) {
		log.info("HttpServiceEngine.makeHttpCall called with "
				+ "httpRequest: {}", httpRequest);

		try {
			ResponseEntity<String> httpResponse = restClient
					.method(httpRequest.getHttpMethod())
					.uri(httpRequest.getUrl())
					.headers(
							restClientHttpHeaders -> restClientHttpHeaders.addAll(httpRequest.getHttpHeaders()))
					.body(httpRequest.getBody())
					.retrieve()
					.toEntity(String.class); 
			log.info("HttpServiceEngine.makeHttpCall response: {}", httpResponse);

			return httpResponse;

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("HTTP error occurred while making HTTP call: {}", e.getMessage(), e);
			
			// if service_unavailable or gateway_timeout, throw PaypalProviderException
			if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE 
					|| e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
				log.error("External service is unavailable or timed out: {}", 
						e.getMessage(), e);
				
				throw new ProcessingException(
						ErrorCodeEnum.UNABLE_TO_CONNECT_TO_EXTERNAL_SERVICE.getErrorCode(), 
						ErrorCodeEnum.UNABLE_TO_CONNECT_TO_EXTERNAL_SERVICE.getErrorMessage(), 
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			
			return ResponseEntity
					.status(e.getStatusCode())
					.headers(e.getResponseHeaders())
					.body(e.getResponseBodyAsString());
			
		} catch (Exception e) {
			log.error("Exception occurred while making HTTP call: {}", e.getMessage(), e);
			throw new ProcessingException(
					ErrorCodeEnum.UNABLE_TO_CONNECT_TO_EXTERNAL_SERVICE.getErrorCode(), 
					ErrorCodeEnum.UNABLE_TO_CONNECT_TO_EXTERNAL_SERVICE.getErrorMessage(), 
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
