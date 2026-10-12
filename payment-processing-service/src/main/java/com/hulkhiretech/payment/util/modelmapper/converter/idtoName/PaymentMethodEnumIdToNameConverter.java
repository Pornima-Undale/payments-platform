package com.hulkhiretech.payment.util.modelmapper.converter.idtoName;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payment.constant.PaymentMethodEnum;

public class PaymentMethodEnumIdToNameConverter extends AbstractConverter<Integer, String> {
    @Override
    protected String convert(Integer source) {
        if (source == null) {
            return null;
        }
        PaymentMethodEnum method = PaymentMethodEnum.getById(source);
        return method == null ? null : method.getName();
    }
}
