package com.multicompany.sales_system.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum UsuarioRole {
    COMPRADOR, VENDEDOR, MODERADOR, ADMIN;

    @JsonCreator
    public static UsuarioRole from(String v) {
        return UsuarioRole.valueOf(v.trim().toUpperCase());
    }
}
