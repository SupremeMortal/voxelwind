package com.voxelwind.api.game.level.block;

import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.api.game.item.ItemType;

import java.util.List;
import java.util.Optional;

/**
 * This interface specifies a kind of block.
 */
public interface BlockType extends ItemType {
    default boolean isBlock() {
        return true;
    }

    default boolean isTool() {
        return false;
    }

    boolean isDiggable();

    boolean isTransparent();

    boolean isFlammable();

    float getHardness();

    float getBreakTime(Optional<ItemStack> stackOptional, List<ItemType> allowedTools);

    int getEmitsLight();

    int getFiltersLight();
}