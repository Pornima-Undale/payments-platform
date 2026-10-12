package com.hulkhiretech.payment.constant;

/**
 * ErrorCodeEnum - Enum for error codes and messages
 */
public enum ErrorCodeEnum {
	
	GENERIC_ERROR("20000", "Unable to process the request. Please try again later."),
	INVALID_NAME("20001", "Name cannot be null"),
	UNABLE_TO_CONNECT_TO_EXTERNAL_SERVICE("20002", "Unable to connect to external service. Please try again later."),
	PAYPAL_CREATE_ORDER_FAILED("20003", "Failed to create order with Paypal. Please try again later."),
	PAYPAL_CREATE_ORDER_ERROR("20004", "<Paypal error. Prepare message dynamically from paypal response.>"),
	PAYPAL_CAPTURE_ORDER_FAILED("20005", "Failed to capture order with Paypal. Please try again later."),
	PAYPAL_CAPTURE_ORDER_ERROR("20006", "<Paypal error. Prepare message dynamically from paypal response.>"),
	ERROR_CALLING_CREATE_ORDER("20007", "Error occurred while calling create order API. Please try again later."),
	UNABLE_TO_CONNECT_TO_PAYPAL_PROVIDER("20008", "Unable to connect to Paypal provider. Please try again later.");
	
	private final String errorCode;
	private final String errorMessage;
	
	ErrorCodeEnum(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
