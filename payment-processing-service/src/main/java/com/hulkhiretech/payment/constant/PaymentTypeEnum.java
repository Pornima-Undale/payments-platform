package com.hulkhiretech.payment.constant;

import lombok.Getter;

@Getter
public enum PaymentTypeEnum {

    SALE(1, "SALE");

    private final Integer id;
    private final String name;

    PaymentTypeEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Return the enum constant matching the given id, or null if none matches.
     *
     * @param id integer id of the type
     * @return matching PaymentTypeEnum or null when not found or id is null
     */
    public static PaymentTypeEnum getById(Integer id) {
        if (id == null) {
            return null;
        }
        for (PaymentTypeEnum type : values()) {
            if (id.equals(type.id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Return the enum constant matching the given name (case-insensitive), or null if none matches.
     * It compares against the {@code name} field of the enum.
     *
     * @param name textual name of the type
     * @return matching PaymentTypeEnum or null when not found or name is null
     */
    public static PaymentTypeEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (PaymentTypeEnum type : values()) {
            if (name.equalsIgnoreCase(type.name)) {
                return type;
            }
        }
        return null;
    }
}