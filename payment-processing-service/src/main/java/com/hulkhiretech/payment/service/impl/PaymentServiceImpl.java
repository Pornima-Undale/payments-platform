package com.hulkhiretech.payment.service.impl;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payment.constant.ErrorCodeEnum;
import com.hulkhiretech.payment.constant.TxnStatusEnum;
import com.hulkhiretech.payment.dto.TransactionDTO;
import com.hulkhiretech.payment.entity.TransactionEntity;
import com.hulkhiretech.payment.exception.ProcessingException;
import com.hulkhiretech.payment.http.HttpRequest;
import com.hulkhiretech.payment.payalprovider.OrderRes;
import com.hulkhiretech.payment.pojo.CreateTxnReq;
import com.hulkhiretech.payment.pojo.InitiateTxnReq;
import com.hulkhiretech.payment.pojo.PaymentResponse;
import com.hulkhiretech.payment.repository.interfaces.TransactionRepository;
import com.hulkhiretech.payment.service.PaymentStatusService;
import com.hulkhiretech.payment.service.client.PaypalProviderClient;
import com.hulkhiretech.payment.service.helper.PaypalProviderCaptureOrderHelper;
import com.hulkhiretech.payment.service.helper.PaypalProviderCreateOrderHelper;
import com.hulkhiretech.payment.service.interfaces.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	private final PaymentStatusService statusService;
	private final ModelMapper modelMapper;

	private final TransactionRepository transactionRepository;

	private final PaypalProviderClient paypalProviderClient;

	private final PaypalProviderCreateOrderHelper paypalProviderCreateOrderHelper;

	private final PaypalProviderCaptureOrderHelper paypalProviderCaptureOrderHelper;

	@Override
	public PaymentResponse createPayment(CreateTxnReq createTxnReq) {
		log.info("createPayment called with createTxnReq: {}", createTxnReq);
		TransactionDTO dto = prepareTxnDTOFromRequest(createTxnReq);
		log.info("Mapped CreateTxnReq to TransactionDTO: {}", dto);

		// Set status id and transaction reference here (caller responsibility)
		dto.setTxnStatus(TxnStatusEnum.CREATED.getName()); // CREATED
		String txnReference = generateUniqueTxnReference();
		dto.setTxnReference(txnReference);

		log.info("Prepared TransactionDTO: {}, txnReference={}", dto, txnReference);

		TransactionDTO response = statusService.processStatus(dto);

		log.info("Response from PaymentStatusService: {}", response);

		// Map response DTO to PaymentResponse
		PaymentResponse paymentResponse = new PaymentResponse();
		if (response != null) {
			paymentResponse.setTxnReference(response.getTxnReference());
			paymentResponse.setTxnStatus(response.getTxnStatus());
		}

		return paymentResponse;
	}

	private String generateUniqueTxnReference() {
		return UUID.randomUUID().toString();
	}

	private TransactionDTO prepareTxnDTOFromRequest(CreateTxnReq createTxnReq) {
		// Use ModelMapper to map CreateTxnReq -> TransactionDTO
		return modelMapper.map(createTxnReq, TransactionDTO.class);
	}

	/**
	 * 1. Make DB call to get transaction details by txnReference
	 * 2. Use status system to update/process INITIATED status in db
	 * 3. Call create-order paypal-provider api
	 * 4. Once success then update DB as PENDING
	 * 5. return the values to API response 
	 */
	@Override
	public PaymentResponse initiatePayment(String txnReference
			, InitiateTxnReq initiateTxnReq) {
		log.info("initiatePayment called with "
				+ "txnReference: {}, initiateTxnReq: {}", 
				txnReference, initiateTxnReq);

		TransactionEntity entity = transactionRepository.getTxnByTxnReference(txnReference);
		log.info("Fetched TransactionEntity from DB: {}", entity);

		// use modelMapper to map entity to DTO
		TransactionDTO dto = modelMapper.map(entity, TransactionDTO.class);
		log.info("Mapped TransactionEntity to TransactionDTO: {}", dto);

		dto.setTxnStatus(TxnStatusEnum.INITIATED.getName());
		TransactionDTO responseDto = statusService.processStatus(dto);
		log.info("Response from PaymentStatusService after processing INITIATED status: {}", responseDto);

		HttpRequest httpRequest = paypalProviderCreateOrderHelper.prepareRequest(
				dto, initiateTxnReq);
		log.info("Prepared HttpRequest for PaypalProviderClient: {}", httpRequest);

		try {
			ResponseEntity<String> httpResponse = paypalProviderClient.makeCall(httpRequest); // TODO call payal-provider create-order API
			log.info("Response from PaypalProviderClient: {}", httpResponse);

			OrderRes successResponseObj = paypalProviderCreateOrderHelper.processResponse(httpResponse); 
			log.info("Processed PaypalProviderClient response into OrderRes object: {}", successResponseObj);

			// if success
			dto.setTxnStatus(TxnStatusEnum.PENDING.getName());
			dto.setProviderReference(successResponseObj.getOrderId());
			responseDto = statusService.processStatus(dto);

			PaymentResponse response = new PaymentResponse();
			response.setTxnReference(responseDto.getTxnReference());
			response.setTxnStatus(responseDto.getTxnStatus());

			response.setProviderReference(responseDto.getProviderReference());
			response.setRedirectUrl(successResponseObj.getRedirectUrl());
			log.info("Returning PaymentResponse: {}", response);
			return response;
		} catch (ProcessingException e) {
			log.error("Exception while preparing HttpRequest for PaypalProviderClient: {}", e.getMessage(), e);

			responseDto.setTxnStatus(TxnStatusEnum.FAILED.getName());
			responseDto.setErrorCode(e.getErrorCode());
			responseDto.setErrorMessage(e.getErrorMessage());

			statusService.processStatus(responseDto);
			log.info("Updated TransactionDTO to FAILED status in DB: {}", responseDto);

			throw e; // rethrow or handle as per your error handling strategy
		} catch (Exception e) {
			log.error("Exception while preparing HttpRequest for PaypalProviderClient: {}", e.getMessage(), e);

			responseDto.setTxnStatus(TxnStatusEnum.FAILED.getName());
			responseDto.setErrorCode(ErrorCodeEnum.ERROR_CALLING_CREATE_ORDER.getErrorCode());
			responseDto.setErrorMessage(ErrorCodeEnum.ERROR_CALLING_CREATE_ORDER.getErrorMessage());

			statusService.processStatus(responseDto);
			log.info("Updated TransactionDTO to FAILED status in DB: {}", responseDto);

			throw new ProcessingException(
					ErrorCodeEnum.ERROR_CALLING_CREATE_ORDER.getErrorCode(), 
					ErrorCodeEnum.ERROR_CALLING_CREATE_ORDER.getErrorMessage(), 
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public PaymentResponse capturePayment(String txnReference) {
		log.info("capturePayment called with "
				+ "txnReference: {}", txnReference);

		TransactionEntity entity = transactionRepository.getTxnByTxnReference(txnReference);
		log.info("Fetched TransactionEntity from DB: {}", entity);

		// use modelMapper to map entity to DTO
		TransactionDTO dto = modelMapper.map(entity, TransactionDTO.class);
		log.info("Mapped TransactionEntity to TransactionDTO: {}", dto);

		dto.setTxnStatus(TxnStatusEnum.APPROVED.getName());
		TransactionDTO responseDto = statusService.processStatus(dto);
		log.info("Response from PaymentStatusService after processing "
				+ "APPROVED status: {}", responseDto);

		HttpRequest httpRequest = paypalProviderCaptureOrderHelper.prepareRequest(
				responseDto.getProviderReference());

		log.info("Prepared HttpRequest for PaypalProviderClient capture order: {}", httpRequest);

		try {
			ResponseEntity<String> httpResponse = paypalProviderClient.makeCall(httpRequest); // TODO call payal-provider create-order API
			log.info("Response from PaypalProviderClient: {}", httpResponse);

			OrderRes successResponseObj = paypalProviderCaptureOrderHelper.processResponse(httpResponse); 
			log.info("Processed PaypalProviderClient response into OrderRes object: {}", successResponseObj);

			// if success
			dto.setTxnStatus(TxnStatusEnum.SUCCESS.getName());
			responseDto = statusService.processStatus(dto);

			PaymentResponse response = new PaymentResponse();
			response.setTxnReference(responseDto.getTxnReference());
			response.setProviderReference(responseDto.getProviderReference());
			response.setTxnStatus(responseDto.getTxnStatus());

			log.info("Returning PaymentResponse: {}", response);
			return response;

		} catch (ProcessingException e) {
			log.error("Exception while processing capturePayment: {}", e.getMessage(), e);
			// IMPORTANT: DONT UPDATE DB AS FAILED. LET RECON TAKE CARE LATER

			throw e;
		}
	}
}
