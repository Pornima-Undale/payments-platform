package com.hulkhiretech.payment.constant;

import lombok.Getter;

@Getter
public enum TxnStatusEnum {

    CREATED(1, "CREATED"),
    INITIATED(2, "INITIATED"),
    PENDING(3, "PENDING"),
    APPROVED(4, "APPROVED"),
    SUCCESS(5, "SUCCESS"),
    FAILED(6, "FAILED");

    private final Integer id;
    private final String name;

    TxnStatusEnum(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Return the enum constant matching the given id, or null if none matches.
     *
     * @param id integer id of the status
     * @return matching TxnStatusEnum or null when not found or id is null
     */
    public static TxnStatusEnum getById(Integer id) {
        if (id == null) {
            return null;
        }
        for (TxnStatusEnum status : values()) {
            if (id.equals(status.id)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Return the enum constant matching the given name (case-insensitive), or null if none matches.
     * It compares against the {@code name} field of the enum.
     *
     * @param name textual name of the status
     * @return matching TxnStatusEnum or null when not found or name is null
     */
    public static TxnStatusEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (TxnStatusEnum status : values()) {
            if (name.equalsIgnoreCase(status.name)) {
                return status;
            }
        }
        return null;
    }
}