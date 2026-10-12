package com.hulkhiretech.payment.util.modelmapper.converter.nametoid;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payment.constant.PaymentTypeEnum;

public class PaymentTypeEnumConverter extends AbstractConverter<String, Integer> {
    @Override
    protected Integer convert(String source) {
        if (source == null) {
            return null;
        }
        PaymentTypeEnum type = PaymentTypeEnum.getByName(source);
        return type == null ? null : type.getId();
    }
}
