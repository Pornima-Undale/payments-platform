package com.hulkhiretech.payment.service.impl.statusprocessor;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.hulkhiretech.payment.constant.TxnStatusEnum;
import com.hulkhiretech.payment.dto.TransactionDTO;
import com.hulkhiretech.payment.entity.TransactionEntity;
import com.hulkhiretech.payment.entity.TransactionLogEntity;
import com.hulkhiretech.payment.repository.interfaces.TransactionLogRepository;
import com.hulkhiretech.payment.repository.interfaces.TransactionRepository;
import com.hulkhiretech.payment.service.interfaces.TxnStatusProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTxnStatusProcessor implements TxnStatusProcessor {

	protected final ModelMapper modelMapper;
	protected final TransactionRepository transactionRepository;
	
	private List<TxnStatusEnum> finalStates = 
			List.of(TxnStatusEnum.SUCCESS, TxnStatusEnum.FAILED);

	@Autowired
	private TransactionLogRepository transactionLogRepository;
	
	public AbstractTxnStatusProcessor(
			ModelMapper modelMapper, 
			TransactionRepository transactionRepository) {
		this.modelMapper = modelMapper;
		this.transactionRepository = transactionRepository;
	}
	
	@Override
	public TransactionDTO processStatus(TransactionDTO newDto) {
		log.info("Processing status in AbstractTxnStatusProcessor, dto={}", newDto);
		
		TransactionEntity existingEntity = transactionRepository.getTxnByTxnReference(newDto.getTxnReference());
		log.info("Fetched existing transaction entity: {}", existingEntity);
		
		TxnStatusEnum existingStatusEnum = existingEntity != null ? 
				TxnStatusEnum.getById(existingEntity.getTxnStatusId()) : null;
		
		TxnStatusEnum newStatusEnum = 
				TxnStatusEnum.getByName(newDto.getTxnStatus());
		
		// STEP1: If previous status and new status is same then don't update
		// TODO temporary comment out for testing, uncomment later
		//checkSameStatusAndExit(newDto, existingStatusEnum, newStatusEnum);
		
		// STEP2: If previous status is in final state: SUCCESS / FAILED then don't process.
		// TODO temporary comment out for testing, uncomment later
		//checkExistingFinalStatusAndExit(newDto, existingStatusEnum);
		
		// STEP3: PROCESS current status
		newDto = processStatusInternal(newDto);
		log.info("STEP3 completed Processed status in processStatusInternal, updated dto={}", newDto);
		
		// STEP4: insert row in txn log' table
		logTxnStatusChange(newDto, existingStatusEnum, newStatusEnum);
		
		// STEP5: for final status raise kafka event
		sendKafkaEventIfFinalStatus(newDto, newStatusEnum);
		
		log.info("Successfully processed returning final "
				+ "dto: {}", newDto);
		return newDto;
	}

	private void sendKafkaEventIfFinalStatus(TransactionDTO newDto, TxnStatusEnum newStatusEnum) {
		// TODO Auto-generated method stub
		
	}

	private void logTxnStatusChange(TransactionDTO newDto, TxnStatusEnum existingStatusEnum,
			TxnStatusEnum newStatusEnum) {
		TransactionLogEntity logEntity = TransactionLogEntity.builder()
				.transactionId(newDto.getId()) 
				.txnFromStatus(
						existingStatusEnum != null ? existingStatusEnum.getName() : "NA")
				.txnToStatus(newStatusEnum.getName())
				.build();
		int logPk = transactionLogRepository.insertTxnLog(logEntity);
		log.info("Inserted transaction log with primary key: {}", logPk);
	}

	private void checkExistingFinalStatusAndExit(TransactionDTO newDto, TxnStatusEnum existingStatusEnum) {
		if(existingStatusEnum != null && finalStates.contains(existingStatusEnum)) {
			log.error("Previous status is in final state for txnReference: {}. No further processing allowed.", newDto.getTxnReference());
			throw new RuntimeException(
					"Previous status is in final state for "
					+ "txnReference: " + newDto.getTxnReference() + ". No further processing allowed.");
		}
	}

	private void checkSameStatusAndExit(TransactionDTO newDto, TxnStatusEnum existingStatusEnum,
			TxnStatusEnum newStatusEnum) {
		if(existingStatusEnum != null && existingStatusEnum == newStatusEnum) {
			log.error("Previous status and new status are same for txnReference: {}. No update needed.", newDto.getTxnReference());
			throw new RuntimeException(
					"Previous status and new status are same for "
					+ "txnReference: " + newDto.getTxnReference() + ". No update needed.");
		}
	}
	
	public abstract TransactionDTO processStatusInternal(TransactionDTO dto);

}
