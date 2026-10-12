package com.hulkhiretech.payment.service;

import org.springframework.stereotype.Service;

import com.hulkhiretech.payment.constant.TxnStatusEnum;
import com.hulkhiretech.payment.dto.TransactionDTO;
import com.hulkhiretech.payment.service.factory.TxnStatusFactory;
import com.hulkhiretech.payment.service.interfaces.TxnStatusProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentStatusService {
	
	private final TxnStatusFactory txnStatusFactory;

	public TransactionDTO processStatus(TransactionDTO dto) {
		log.info("Processing status in PaymentStatusService dto: {}", dto);
		
		TxnStatusEnum statusEnum = TxnStatusEnum.getByName(dto.getTxnStatus());
		
		TxnStatusProcessor statusProcessor = txnStatusFactory.getStatusProcessor(statusEnum);
        
		// Processor now returns a TransactionDTO. Keep this service returning a String
		// (existing API) by converting the returned DTO to a string representation.
		TransactionDTO responseDto = statusProcessor.processStatus(dto);

		log.info("Response DTO from {}: {}", statusProcessor.getClass().getSimpleName(), 
				responseDto);

		return responseDto;
	}
}
