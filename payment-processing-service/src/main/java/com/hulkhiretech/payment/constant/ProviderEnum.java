package com.hulkhiretech.payment.constant;

import lombok.Getter;

@Getter
public enum ProviderEnum {

    PAYPAL(1, "PAYPAL");

    private final Integer id;
    private final String name;

    ProviderEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Return the enum constant matching the given id, or null if none matches.
     *
     * @param id integer id of the provider
     * @return matching ProviderEnum or null when not found or id is null
     */
    public static ProviderEnum getById(Integer id) {
        if (id == null) {
            return null;
        }
        for (ProviderEnum provider : values()) {
            if (id.equals(provider.id)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Return the enum constant matching the given name (case-insensitive), or null if none matches.
     * It compares against the {@code name} field of the enum.
     *
     * @param name textual name of the provider
     * @return matching ProviderEnum or null when not found or name is null
     */
    public static ProviderEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (ProviderEnum provider : values()) {
            if (name.equalsIgnoreCase(provider.name)) {
                return provider;
            }
        }
        return null;
    }
}