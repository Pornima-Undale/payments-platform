package com.hulkhiretech.payment.repository.impl;

import com.hulkhiretech.payment.entity.TransactionLogEntity;
import com.hulkhiretech.payment.repository.interfaces.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Implementation of {@link TransactionLogRepository} using NamedParameterJdbcTemplate.
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class TransactionLogRepositoryImpl implements TransactionLogRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public int insertTxnLog(TransactionLogEntity entity) {
        log.info("insertTxnLog called with entity={}", entity);

        final String sql = "INSERT INTO `payments`.`transaction_log` " +
                "(transaction_id, txn_from_status, txn_to_status) " +
                "VALUES (:transactionId, :txnFromStatus, :txnToStatus)";

        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(entity);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(sql, params, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                log.error("KeyHolder did not contain a generated key after insertTxnLog");
                return -1;
            }
            int generatedId = key.intValue();
            log.debug("Inserted transaction_log with generated id={}", generatedId);
            return generatedId;
        } catch (Exception ex) {
            log.error("Failed to insert transaction_log: {}", ex.getMessage(), ex);
            return -1;
        }
    }

}
