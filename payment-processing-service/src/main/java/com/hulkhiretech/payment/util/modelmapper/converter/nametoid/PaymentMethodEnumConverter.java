package com.hulkhiretech.payment.util.modelmapper.converter.nametoid;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payment.constant.PaymentMethodEnum;

public class PaymentMethodEnumConverter extends AbstractConverter<String, Integer> {
    @Override
    protected Integer convert(String source) {
        return PaymentMethodEnum.getByName(source).getId();
    }
}