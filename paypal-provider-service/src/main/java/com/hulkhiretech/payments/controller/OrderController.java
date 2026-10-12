package com.hulkhiretech.payments.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderRes;
import com.hulkhiretech.payments.service.interfaces.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {
	private final OrderService orderService;

	
	@PostMapping
	public ResponseEntity<OrderRes> createOrder(@RequestBody CreateOrderReq createOrderReq) {
		log.info("createOrder called (controller) - delegating to service"
				+ "createOrderReq: {}", createOrderReq);
		
		OrderRes response = orderService.createOrder(createOrderReq);
		log.info("createOrder response: {}", response);
		
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(response);
	}

	@PostMapping("/{orderId}/capture")
	public ResponseEntity<OrderRes> captureOrder(@PathVariable String orderId) {
		log.info("captureOrder called (controller) orderId:{}", orderId);
		
		OrderRes response = orderService.captureOrder(orderId);
		
		log.info("captureOrder response: {}", response);
		return ResponseEntity.ok()
				.body(response);
	}
}
