package com.hulkhiretech.payment.util.modelmapper.converter.nametoid;

import org.modelmapper.AbstractConverter;

import com.hulkhiretech.payment.constant.ProviderEnum;

public class ProviderEnumConverter extends AbstractConverter<String, Integer> {
    @Override
    protected Integer convert(String source) {
        if (source == null) {
            return null;
        }
        ProviderEnum provider = ProviderEnum.getByName(source);
        return provider == null ? null : provider.getId();
    }
}
