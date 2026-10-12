package com.hulkhiretech.payments.service.helper;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypal.PaypalError;
import com.hulkhiretech.payments.paypal.PaypalOrderRes;
import com.hulkhiretech.payments.util.JsonUtil;
import com.hulkhiretech.payments.util.PaypalErrorUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaptureOrderHelper {

	private static final String COMPLETED = "COMPLETED";

	private final JsonUtil jsonUtil;

	@Value("${paypal.captureOrder.url.template}")
	private String captureOrderUrlTemplate;


	public HttpRequest prepareRequest(String orderId) {
		HttpHeaders customHttpHeaders = new HttpHeaders();
		customHttpHeaders.add(Constant.PAYPAL_REQUEST_ID_HEADER, UUID.randomUUID().toString());
		customHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
		//Note accessToken will be added in PaypalClient
		
		String captureOrderUrl = captureOrderUrlTemplate.replace("{orderId}", orderId);

		HttpRequest httpRequest = HttpRequest.builder()
				.httpMethod(HttpMethod.POST)
				.url(captureOrderUrl)
				.httpHeaders(customHttpHeaders)
				.body("")
				.build();
		return httpRequest;
	}


	public PaypalOrderRes processResponse(ResponseEntity<String> httpResponse) {
		log.info("Processing Paypal create order response: {}", httpResponse);

		if (httpResponse.getStatusCode().is2xxSuccessful()) {
			PaypalOrderRes paypalResponseObj = jsonUtil.convertJsonToObject(
					httpResponse.getBody(), PaypalOrderRes.class);
			log.info("Converted Paypal response JSON to PaypalOrderRes "
					+ "paypalResponseObj: {}", paypalResponseObj);

			if (paypalResponseObj != null 
					&& paypalResponseObj.getId() != null
					&& COMPLETED.equalsIgnoreCase(
							paypalResponseObj.getStatus())) {
				log.info("Paypal response is success");

				return paypalResponseObj;
			} 

			log.error("Paypal response is not successful or does not contain expected data. "
					+ "paypalResponseObj: {}", paypalResponseObj);

		}

		// if 4xx or 5xx
		if (httpResponse.getStatusCode().is4xxClientError() 
				|| httpResponse.getStatusCode().is5xxServerError()) {
			log.error("Paypal create order response is not successful. "
					+ "httpResponse: {}", httpResponse);

			PaypalError errorObj = jsonUtil.convertJsonToObject(
					httpResponse.getBody(), PaypalError.class);

			// if not null
			if (errorObj != null) {
				String errorMessage = PaypalErrorUtil.generateErrorMessage(errorObj);
				log.error("Paypal error details: {}", errorMessage);

				throw new PaypalProviderException(
						ErrorCodeEnum.PAYPAL_CAPTURE_ORDER_ERROR.getErrorCode(), 
						errorMessage,
						HttpStatus.valueOf(httpResponse.getStatusCode().value()));
			}

			log.error("Paypal error response is null or could not be parsed. "
					+ "httpResponse: {}", httpResponse);

		} 

		log.error("Paypal create order response is not successful and could not be processed. "
				+ "httpResponse: {}", httpResponse);
		throw new PaypalProviderException(
				ErrorCodeEnum.PAYPAL_CAPTURE_ORDER_FAILED.getErrorCode(), 
				ErrorCodeEnum.PAYPAL_CAPTURE_ORDER_FAILED.getErrorMessage(), 
				HttpStatus.INTERNAL_SERVER_ERROR);

	}


}
