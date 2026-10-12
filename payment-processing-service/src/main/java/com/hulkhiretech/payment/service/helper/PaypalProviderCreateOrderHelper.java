package com.hulkhiretech.payment.service.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payment.constant.ErrorCodeEnum;
import com.hulkhiretech.payment.dto.TransactionDTO;
import com.hulkhiretech.payment.exception.ProcessingException;
import com.hulkhiretech.payment.http.HttpRequest;
import com.hulkhiretech.payment.payalprovider.CreateOrderReq;
import com.hulkhiretech.payment.payalprovider.OrderRes;
import com.hulkhiretech.payment.payalprovider.PPErrorResponse;
import com.hulkhiretech.payment.pojo.InitiateTxnReq;
import com.hulkhiretech.payment.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaypalProviderCreateOrderHelper {

	private static final String PAYER_ACTION_REQUIRED = "PAYER_ACTION_REQUIRED";

	private final JsonUtil jsonUtil;

	@Value("${paypal.provider.createOrder.url}")
	private String paypalProviderCreateOrderUrl;


	public HttpRequest prepareRequest(TransactionDTO dto, InitiateTxnReq initiateTxnReq) {
		HttpHeaders customHttpHeaders = new HttpHeaders();
		customHttpHeaders.setContentType(MediaType.APPLICATION_JSON);

		CreateOrderReq createOrderReq = CreateOrderReq.builder()
				.amount(dto.getAmount())
				.currencyCode(dto.getCurrency())
				.returnUrl(initiateTxnReq.getReturnUrl())
				.cancelUrl(initiateTxnReq.getCancelUrl())
				.build();

		String reqAsJson = jsonUtil.convertObjectToJson(
				createOrderReq);

		log.info("PaypalCreateOrderRequest as JSON: {}", reqAsJson);

		HttpRequest httpRequest = HttpRequest.builder()
				.httpMethod(HttpMethod.POST)
				.url(paypalProviderCreateOrderUrl)
				.httpHeaders(customHttpHeaders)
				.body(reqAsJson)
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
					&& PAYER_ACTION_REQUIRED.equalsIgnoreCase(
							paypalResponseObj.getPaypalStatus())
					&& paypalResponseObj.getRedirectUrl() != null) {
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
				ErrorCodeEnum.PAYPAL_CREATE_ORDER_FAILED.getErrorCode(), 
				ErrorCodeEnum.PAYPAL_CREATE_ORDER_FAILED.getErrorMessage(), 
				HttpStatus.INTERNAL_SERVER_ERROR);

	}

}
