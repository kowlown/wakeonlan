package com.mystnihon.nitrite.metadata;

@SuppressWarnings("unused")
public class Attribute<Type> {
    private final String name;
    private final Class<Type> type;

    public Attribute(String name, Class<Type> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<Type> getType() {
        return type;
    }
}
