package com.hulkhiretech.payment.payalprovider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ErrorResponse - POJO for error response
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PPErrorResponse {

	private String errorCode;
	private String errorMessage;
}
