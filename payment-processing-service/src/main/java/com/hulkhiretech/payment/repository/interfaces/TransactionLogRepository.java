package com.hulkhiretech.payment.repository.interfaces;

import com.hulkhiretech.payment.entity.TransactionLogEntity;

/**
 * Repository interface for transaction log persistence operations.
 */
public interface TransactionLogRepository {

    /**
     * Insert a transaction log row and return the generated primary key.
     *
     * @param entity the transaction log entity to insert
     * @return generated id (or -1 on failure)
     */
    int insertTxnLog(TransactionLogEntity entity);

}
