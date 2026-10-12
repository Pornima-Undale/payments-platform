package com.hulkhiretech.payment.constant;

import lombok.Getter;

@Getter
public enum PaymentMethodEnum {

    APM(1, "APM");

    private final Integer id;
    private final String name;

    PaymentMethodEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Return the enum constant matching the given id, or null if none matches.
     *
     * @param id integer id of the method
     * @return matching PaymentMethodEnum or null when not found or id is null
     */
    public static PaymentMethodEnum getById(Integer id) {
        if (id == null) {
            return null;
        }
        for (PaymentMethodEnum method : values()) {
            if (id.equals(method.id)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Return the enum constant matching the given name (case-insensitive), or null if none matches.
     * It compares against the {@code name} field of the enum.
     *
     * @param name textual name of the method
     * @return matching PaymentMethodEnum or null when not found or name is null
     */
    public static PaymentMethodEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (PaymentMethodEnum method : values()) {
            if (name.equalsIgnoreCase(method.name)) {
                return method;
            }
        }
        return null;
    }
}