package com.hulkhiretech.payment.util.modelmapper.converter.idtoName;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payment.constant.ProviderEnum;

public class ProviderEnumIdToNameConverter extends AbstractConverter<Integer, String> {
    @Override
    protected String convert(Integer source) {
        if (source == null) {
            return null;
        }
        ProviderEnum provider = ProviderEnum.getById(source);
        return provider == null ? null : provider.getName();
    }
}
