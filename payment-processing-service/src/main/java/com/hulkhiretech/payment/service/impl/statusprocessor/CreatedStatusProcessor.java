package com.hulkhiretech.payment.service.impl.statusprocessor;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.hulkhiretech.payment.dto.TransactionDTO;
import com.hulkhiretech.payment.entity.TransactionEntity;
import com.hulkhiretech.payment.repository.interfaces.TransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreatedStatusProcessor extends AbstractTxnStatusProcessor {
	
	public CreatedStatusProcessor(
			TransactionRepository transactionRepository, 
			ModelMapper modelMapper) {
		super(modelMapper, transactionRepository);
	}
	
	@Override
	public TransactionDTO processStatusInternal(TransactionDTO dto) {
		log.info("Processing status in CreatedStatusProcessor, dto={}", dto);
		
		// Map DTO to entity using ModelMapper and log the entity
		TransactionEntity entity = modelMapper.map(dto, TransactionEntity.class);
		log.info("Mapped TransactionEntity from DTO: {}", entity);

		// Call repository to create the transaction (dummy implementation) and log the response
		int pkId = transactionRepository.createTransaction(entity);
		log.info("createTransaction returned: {}", pkId);

		// Set the generated primary key on the DTO and return it
		dto.setId(pkId);
		return dto;
	}
	
}
