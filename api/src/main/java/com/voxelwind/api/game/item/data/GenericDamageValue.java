package com.voxelwind.api.game.item.data;

import com.voxelwind.api.game.Metadata;

import java.util.Objects;

/**
 * This class provides a generic damage value.
 */
public final class GenericDamageValue implements Metadata {
    private final short damage;

    public GenericDamageValue(short damage) {
        this.damage = damage;
    }

    public final short getDamage() {
        return damage;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(damage);
    }

    @Override
    public final String toString() {
        return "GenericDamageValue{" +
                "damage=" + damage +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericDamageValue damageValue = (GenericDamageValue) o;
        return damage == damageValue.damage;
    }
}
