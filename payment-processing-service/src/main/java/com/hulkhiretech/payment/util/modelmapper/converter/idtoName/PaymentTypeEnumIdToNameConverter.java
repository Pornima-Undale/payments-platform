package com.hulkhiretech.payment.util.modelmapper.converter.idtoName;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payment.constant.PaymentTypeEnum;

public class PaymentTypeEnumIdToNameConverter extends AbstractConverter<Integer, String> {
    @Override
    protected String convert(Integer source) {
        if (source == null) {
            return null;
        }
        PaymentTypeEnum type = PaymentTypeEnum.getById(source);
        return type == null ? null : type.getName();
    }
}
