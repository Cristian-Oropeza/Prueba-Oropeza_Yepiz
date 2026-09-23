package com.proyecto.servicios.model.gestopago.product;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class TrimmedStringAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String value) {
        return value == null ? null : value.trim();
    }

    @Override
    public String marshal(String value) {
        return value;
    }
}