package com.hulkhiretech.payments.service.interfaces;

import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderRes;

/**
 * Service interface for order operations.
 */
public interface OrderService {

	OrderRes createOrder(CreateOrderReq createOrderReq);

	OrderRes captureOrder(String providerReference);

}
