package com.hulkhiretech.payments.pojo;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreateOrderReq {

    @Schema(
        description = "URL where the customer is redirected after successfully completing the payment.",
        example = "https://merchantwebsite.com/returnUrl"
    )
    private String returnUrl;

    @Schema(
        description = "URL where the customer is redirected if the payment is cancelled.",
        example = "https://merchantwebsite.com/cancelUrl"
    )
    private String cancelUrl;

    @Schema(
        description = "Three-letter ISO 4217 currency code for the payment amount.",
        example = "USD"
    )
    private String currencyCode;

    @Schema(
        description = "Payment amount to be charged.",
        example = "1.00"
    )
    private BigDecimal amount;
}