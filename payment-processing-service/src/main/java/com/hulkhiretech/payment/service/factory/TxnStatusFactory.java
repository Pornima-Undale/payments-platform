package com.hulkhiretech.payment.service.factory;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.hulkhiretech.payment.service.impl.statusprocessor.FailedStatusProcessor;
import com.hulkhiretech.payment.service.impl.statusprocessor.PendingStatusProcessor;
import com.hulkhiretech.payment.service.impl.statusprocessor.SuccessStatusProcessor;
import com.hulkhiretech.payment.service.impl.statusprocessor.CreatedStatusProcessor;
import com.hulkhiretech.payment.service.impl.statusprocessor.InitiatedStatusProcessor;
import com.hulkhiretech.payment.constant.TxnStatusEnum;
import com.hulkhiretech.payment.service.impl.statusprocessor.ApprovedStatusProcessor;
import com.hulkhiretech.payment.service.interfaces.TxnStatusProcessor;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TxnStatusFactory {

	private final ApplicationContext appContext;

	public TxnStatusProcessor getStatusProcessor(TxnStatusEnum statusEnum) {

		if (statusEnum == null) {
			throw new IllegalArgumentException("Status must not be null");
		}

		// Switch directly on the enum constant. This is type-safe and matches TxnStatusEnum values.
		switch (statusEnum) {
		case CREATED:
			return appContext.getBean(CreatedStatusProcessor.class);

		case INITIATED:
			return appContext.getBean(InitiatedStatusProcessor.class);

		case PENDING:
			return appContext.getBean(PendingStatusProcessor.class);

		case APPROVED:
			return appContext.getBean(ApprovedStatusProcessor.class);

		case SUCCESS:
			return appContext.getBean(SuccessStatusProcessor.class);

		case FAILED:
			return appContext.getBean(FailedStatusProcessor.class);

		default:
			// Defensive: should never happen unless enum is changed in future
			throw new IllegalArgumentException("Invalid transaction status: " + statusEnum);
		}
	}
}
