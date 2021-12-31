package com.rlee.discordbots.rpbot.profile;

public class ExpressionAttribute extends Attribute<String> {
    private String value;

    public ExpressionAttribute(String name) {
        super(name);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
