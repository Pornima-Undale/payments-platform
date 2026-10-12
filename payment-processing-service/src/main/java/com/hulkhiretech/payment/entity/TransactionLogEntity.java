package com.hulkhiretech.payment.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity representing a row in payments.transaction_log
 */
@Data
@Builder
public class TransactionLogEntity {

    private Integer id;
    private Integer transactionId;
    private String txnFromStatus;
    private String txnToStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
