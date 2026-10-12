package com.hulkhiretech.payment.service.impl.statusprocessor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payment.dto.TransactionDTO;
import com.hulkhiretech.payment.entity.TransactionEntity;
import com.hulkhiretech.payment.repository.interfaces.TransactionRepository;
// ...existing code... (no direct TxnStatusProcessor import - extends AbstractTxnStatusProcessor instead)

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PendingStatusProcessor extends AbstractTxnStatusProcessor {
    
	public PendingStatusProcessor(
			TransactionRepository transactionRepository,
			ModelMapper modelMapper) {
		super(modelMapper, transactionRepository);
	}

	@Override
	public TransactionDTO processStatusInternal(TransactionDTO dto) {
		log.info("Processing status in PendingStatusProcessor, dto={}", dto);
		
		TransactionEntity entity = modelMapper.map(dto, TransactionEntity.class);
		log.info("Mapped TransactionEntity from DTO: {}", entity);

		boolean isUpdate = transactionRepository.updateTxnDetails(entity);
		log.info("updateTxnDetails returned: {}", isUpdate);
		
		if(!isUpdate) {
			log.error("Failed to update transaction details for txnReference: {}", dto.getTxnReference());
			throw new RuntimeException("Failed to update transaction details for txnReference: " + dto.getTxnReference());
		}
		
		log.info("Successfully updated transaction details for txnReference: {}", dto.getTxnReference());
		return dto;
	}

}
