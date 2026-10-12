package com.hulkhiretech.payment.repository.interfaces;

import com.hulkhiretech.payment.entity.TransactionEntity;

/**
 * Repository interface for transaction persistence operations.
 */
public interface TransactionRepository {

    /**
     * Create a transaction record.
     *
     * @param entity the transaction entity to create
     * @return an int indicating result (dummy value for now)
     */
    int createTransaction(TransactionEntity entity);
    
    TransactionEntity getTxnByTxnReference(String txnReference);
    
    boolean updateTxnDetails(TransactionEntity entity);
}
