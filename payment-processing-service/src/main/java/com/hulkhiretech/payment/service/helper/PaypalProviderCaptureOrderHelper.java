package com.hulkhiretech.payment.service.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payment.constant.ErrorCodeEnum;
import com.hulkhiretech.payment.exception.ProcessingException;
import com.hulkhiretech.payment.http.HttpRequest;
import com.hulkhiretech.payment.payalprovider.OrderRes;
import com.hulkhiretech.payment.payalprovider.PPErrorResponse;
import com.hulkhiretech.payment.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaypalProviderCaptureOrderHelper {

	private static final String COMPLETED = "COMPLETED";

	private final JsonUtil jsonUtil;

	@Value("${paypal.provider.captureOrder.url.template}")
	private String paypalProviderCaptureOrderUrlTemplate;


	public HttpRequest prepareRequest(String providerReference) {
		HttpHeaders customHttpHeaders = new HttpHeaders();
		//customHttpHeaders.setContentType(MediaType.APPLICATION_JSON);

		String url = paypalProviderCaptureOrderUrlTemplate.replace(
				"{orderId}", providerReference); // Replace with actual orderId if needed

		HttpRequest httpRequest = HttpRequest.builder()
				.httpMethod(HttpMethod.POST)
				.url(url)
				.httpHeaders(customHttpHeaders)
				.body("")
				.build();
		return httpRequest;
	}

	public OrderRes processResponse(ResponseEntity<String> httpResponse) {
		log.info("Processing Paypal create order response: {}", httpResponse);

		if (httpResponse.getStatusCode().is2xxSuccessful()) {
			OrderRes paypalResponseObj = jsonUtil.convertJsonToObject(
					httpResponse.getBody(), OrderRes.class);
			log.info("Converted Paypal response JSON to PaypalOrderRes "
					+ "paypalResponseObj: {}", paypalResponseObj);

			if (paypalResponseObj != null 
					&& paypalResponseObj.getOrderId() != null
					&& COMPLETED.equalsIgnoreCase(
							paypalResponseObj.getPaypalStatus())) {
				log.info("Paypal response is success");

				return paypalResponseObj;
			} 

			log.error("Paypal create order response does not contain expected data. "
					+ "paypalResponseObj: {}", paypalResponseObj);

		}

		// if 4xx or 5xx
		if (httpResponse.getStatusCode().is4xxClientError() 
				|| httpResponse.getStatusCode().is5xxServerError()) {
			log.error("Paypal create order response is not successful. "
					+ "httpResponse: {}", httpResponse);

			PPErrorResponse errorObj = jsonUtil.convertJsonToObject(
					httpResponse.getBody(), PPErrorResponse.class);

			// if not null
			if (errorObj != null) {
				log.error("Paypal error details: errorCode={}, errorMessage={}", 
						errorObj.getErrorCode(), errorObj.getErrorMessage());

				throw new ProcessingException(
						errorObj.getErrorCode(), 
						errorObj.getErrorMessage(),
						HttpStatus.valueOf(httpResponse.getStatusCode().value()));
			}

			log.error("Paypal error response is null or could not be parsed. "
					+ "httpResponse: {}", httpResponse);

		} 

		log.error("Paypal create order response is not successful or does not contain expected data. "
				+ "httpResponse: {}", httpResponse);
		throw new ProcessingException(
				ErrorCodeEnum.PAYPAL_CAPTURE_ORDER_FAILED.getErrorCode(), 
				ErrorCodeEnum.PAYPAL_CAPTURE_ORDER_FAILED.getErrorMessage(), 
				HttpStatus.INTERNAL_SERVER_ERROR);

	}

}
