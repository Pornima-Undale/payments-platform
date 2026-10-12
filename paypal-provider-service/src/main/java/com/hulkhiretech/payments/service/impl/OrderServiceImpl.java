package com.hulkhiretech.payments.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypal.PaypalOrderRes;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderRes;
import com.hulkhiretech.payments.service.client.PaypalClient;
import com.hulkhiretech.payments.service.helper.CaptureOrderHelper;
import com.hulkhiretech.payments.service.helper.CreateOrderHelper;
import com.hulkhiretech.payments.service.interfaces.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final PaypalClient paypalClient;

	private final CreateOrderHelper createOrderHelper;
	
	private final CaptureOrderHelper captureOrderHelper;

	private final ObjectMapper objectMapper;

	@Override
	public OrderRes createOrder(CreateOrderReq createOrderReq) {
		log.info("OrderService.createOrder called "
				+ "createOrderReq: {}", createOrderReq);
		
		HttpRequest httpRequest = createOrderHelper.prepareCreateOrderPaypalRequest(createOrderReq);
		log.info("Prepared HttpRequest for Paypal create order: {}", httpRequest);

		ResponseEntity<String> httpResponse = paypalClient.makeCall(httpRequest);
		log.info("PaypalClient.makeCall response: {}", httpResponse);

		PaypalOrderRes paypalResponseObj = createOrderHelper.processCreateOrderResponse(httpResponse); 
		log.info("Processed Paypal response into PaypalOrderRes object: {}", paypalResponseObj);

		OrderRes orderRes = new OrderRes();
		orderRes.setOrderId(paypalResponseObj.getId());
		orderRes.setPaypalStatus(paypalResponseObj.getStatus());
		orderRes.setRedirectUrl(paypalResponseObj.getLinks().stream()
				.filter(link -> "payer-action".equals(link.getRel()))
				.map(PaypalOrderRes.Link::getHref)
				.findFirst()
				.orElse(null));
		log.info("Mapped PaypalOrderRes to OrderRes: {}", orderRes);

		return orderRes;
	}

	@Override
	public OrderRes captureOrder(String orderId) {
		log.info("OrderService.captureOrder called orderId:{}", orderId);
		
		HttpRequest httpRequest = captureOrderHelper.prepareRequest(orderId);
		log.info("Prepared HttpRequest for Paypal create order: {}", httpRequest);
		
		ResponseEntity<String> httpResponse = paypalClient.makeCall(httpRequest);
		log.info("PaypalClient.makeCall response: {}", httpResponse);
		
		PaypalOrderRes paypalResponseObj = captureOrderHelper.processResponse(httpResponse);
		log.info("Processed Paypal response into PaypalOrderRes object: {}", paypalResponseObj);

		OrderRes orderRes = new OrderRes();
		orderRes.setOrderId(paypalResponseObj.getId());
		orderRes.setPaypalStatus(paypalResponseObj.getStatus());
		log.info("Mapped PaypalOrderRes to OrderRes: {}", orderRes);
		
		return orderRes;
	}
}
