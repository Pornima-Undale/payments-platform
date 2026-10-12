package com.hulkhiretech.payment.util.modelmapper.converter.nametoid;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payment.constant.TxnStatusEnum;

public class TxnStatusEnumConverter extends AbstractConverter<String, Integer> {
    @Override
    protected Integer convert(String source) {
        if (source == null) {
            return null;
        }
        TxnStatusEnum status = TxnStatusEnum.getByName(source);
        return status == null ? null : status.getId();
    }
}
