package com.hulkhiretech.payment.payalprovider;

import lombok.Data;

@Data
public class OrderRes {

	private String orderId;
	private String paypalStatus;
	private String redirectUrl;
}
