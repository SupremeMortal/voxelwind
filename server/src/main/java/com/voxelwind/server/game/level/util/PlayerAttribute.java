package com.voxelwind.server.game.level.util;

import java.util.Objects;

public class PlayerAttribute extends EntityAttribute {
    private final float defaultValue;

    public PlayerAttribute(String name, float minimumValue, float maximumValue, float value, float defaultValue) {
        super(name, minimumValue, maximumValue, value);
        this.defaultValue = defaultValue;
    }

    public float getDefaultValue() {
        return defaultValue;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerAttribute attribute = (PlayerAttribute) o;
        return Float.compare(attribute.getMinimumValue(), getMinimumValue()) == 0 &&
                Float.compare(attribute.getMaximumValue(), getMaximumValue()) == 0 &&
                Float.compare(attribute.getValue(), getValue()) == 0 &&
                Float.compare(attribute.defaultValue, defaultValue) == 0 &&
                Objects.equals(getName(), attribute.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getMinimumValue(), getMaximumValue(), getValue(), defaultValue);
    }

    @Override
    public String toString() {
        return "PlayerAttribute{" +
                "name='" + getName() + '\'' +
                ", minimumValue=" + getMinimumValue() +
                ", maximumValue=" + getMaximumValue() +
                ", value=" + getValue() +
                ", defaultValue=" + defaultValue +
                '}';
    }
}
