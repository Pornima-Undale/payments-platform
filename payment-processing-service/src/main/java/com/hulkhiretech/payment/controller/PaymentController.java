package com.hulkhiretech.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payment.pojo.CreateTxnReq;
import com.hulkhiretech.payment.pojo.InitiateTxnReq;
import com.hulkhiretech.payment.pojo.PaymentResponse;
import com.hulkhiretech.payment.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
@Slf4j
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreateTxnReq createTxnReq) {
		log.info("POST /payments - createPayment called with createTxnReq: {}", createTxnReq);

		PaymentResponse response = paymentService.createPayment(createTxnReq);

		log.info("POST /payments - createPayment response: {}", response);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(response);
	}

	@PostMapping("/{txnReference}/initiate")
	public ResponseEntity<PaymentResponse> initiatePayment(
			@PathVariable String txnReference,
			@RequestBody InitiateTxnReq initiateTxnReq) {
		log.info("initiatePayment called with "
				+ "txnReference: {}", txnReference);
		
		PaymentResponse response = paymentService.initiatePayment(
				txnReference, initiateTxnReq);
		log.info("initiatePayment response: {}", response);

		return ResponseEntity.ok()
				.body(response);
	}

	@PostMapping("/{txnReference}/capture")
	public ResponseEntity<PaymentResponse> capturePayment(
			@PathVariable String txnReference) {
		log.info("capturePayment called with "
				+ "txnReference: {}", txnReference);
		
		PaymentResponse response = paymentService.capturePayment(txnReference);
		log.info("capturePayment response: {}", response);

		return ResponseEntity.ok()
				.body(response);
	}

}
