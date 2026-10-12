package com.hulkhiretech.payment.repository.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.hulkhiretech.payment.entity.TransactionEntity;
import com.hulkhiretech.payment.repository.interfaces.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link TransactionRepository} that persists transactions
 * using NamedParameterJdbcTemplate.
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Override
	public int createTransaction(TransactionEntity entity) {
		log.info("createTransaction called with entity: {}", entity);

		final String sql = "INSERT INTO `payments`.`transaction` (" +
				"user_id, payment_method_id, provider_id, payment_type_id, txn_status_id, " +
				"amount, currency, merchant_transaction_reference, txn_reference) " +
				"VALUES (:userId, :paymentMethodId, :providerId, :paymentTypeId, :txnStatusId, " +
				"COALESCE(:amount, 0), :currency, :merchantTransactionReference, :txnReference)";

		// Use BeanPropertySqlParameterSource so bean properties (camelCase) map to named params
		// SQL uses the same camelCase parameter names (e.g. :userId). For DB defaults when amount
		// is null, we use COALESCE(...) in the SQL above.
		BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(entity);

		KeyHolder keyHolder = new GeneratedKeyHolder();

		try {
			namedParameterJdbcTemplate.update(sql, params, keyHolder);
			Number key = keyHolder.getKey();
			if (key == null) {
				log.error("KeyHolder did not contain a generated key after insert");
				return -1;
			}
			int generatedId = key.intValue();
			log.debug("Inserted transaction with generated id={}", generatedId);
			return generatedId;
		} catch (Exception ex) {
			log.error("Failed to insert transaction: {}", ex.getMessage(), ex);
			return -1;
		}
	}

	@Override
	public TransactionEntity getTxnByTxnReference(String txnReference) {
		final String sql = "SELECT id, user_id AS userId, payment_method_id AS paymentMethodId, "
				+ "provider_id AS providerId, payment_type_id AS paymentTypeId, txn_status_id AS txnStatusId, "
				+ "amount, currency, merchant_transaction_reference AS merchantTransactionReference, "
				+ "txn_reference AS txnReference, provider_reference AS providerReference, "
				+ "error_code AS errorCode, error_message AS errorMessage, "
				+ "created_at AS createdAt, updated_at AS updatedAt, retry_count AS retryCount "
				+ "FROM `payments`.`transaction` WHERE txn_reference = :txnReference LIMIT 1";

		MapSqlParameterSource params = new MapSqlParameterSource().addValue("txnReference", txnReference);

		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params,
					new BeanPropertyRowMapper<>(TransactionEntity.class));
		} catch (EmptyResultDataAccessException ex) {
			// no result found
			return null;
		} catch (Exception ex) {
			log.error("Error fetching transaction by txnReference {}: {}", txnReference, ex.getMessage(), ex);
			return null;
		}
	}

	@Override
	public boolean updateTxnDetails(TransactionEntity entity) {
		if (entity == null || entity.getId() == null) {
			log.warn("updateTxnDetails called with null entity or null id");
			return false;
		}

		log.info("updateTxnDetails called for id={}", entity.getId());

		final String sql = "UPDATE `payments`.`transaction` SET "
				+ "txn_status_id = :txnStatusId, "
				+ "provider_reference = :providerReference, "
				+ "error_code = :errorCode, "
				+ "error_message = :errorMessage, "
				+ "retry_count = :retryCount "
				+ "WHERE id = :id";

		BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(entity);

		try {
			int rows = namedParameterJdbcTemplate.update(sql, params);
			if (rows == 1) {
				log.debug("Successfully updated transaction id={}", entity.getId());
				return true;
			} else {
				log.warn("updateTxnDetails affected {} rows for id={}", rows, entity.getId());
				return rows > 0;
			}
		} catch (Exception ex) {
			log.error("Error updating transaction id {}: {}", entity.getId(), ex.getMessage(), ex);
			return false;
		}
	}
}
