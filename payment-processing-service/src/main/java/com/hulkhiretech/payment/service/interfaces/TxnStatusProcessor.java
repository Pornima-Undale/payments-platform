package com.hulkhiretech.payment.service.interfaces;

import com.hulkhiretech.payment.dto.TransactionDTO;

public interface TxnStatusProcessor {
    
	TransactionDTO processStatus(TransactionDTO dto);

}
