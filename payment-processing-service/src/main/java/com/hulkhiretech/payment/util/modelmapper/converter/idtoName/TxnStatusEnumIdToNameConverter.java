package com.hulkhiretech.payment.util.modelmapper.converter.idtoName;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payment.constant.TxnStatusEnum;

public class TxnStatusEnumIdToNameConverter extends AbstractConverter<Integer, String> {
    @Override
    protected String convert(Integer source) {
        if (source == null) {
            return null;
        }
        TxnStatusEnum status = TxnStatusEnum.getById(source);
        return status == null ? null : status.getName();
    }
}
