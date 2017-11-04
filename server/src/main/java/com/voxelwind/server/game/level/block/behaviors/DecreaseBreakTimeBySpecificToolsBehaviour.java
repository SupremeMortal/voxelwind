package com.voxelwind.server.game.level.block.behaviors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.voxelwind.api.game.item.ItemType;
import com.voxelwind.api.game.item.ItemTypes;

import java.util.List;

public class DecreaseBreakTimeBySpecificToolsBehaviour extends SimpleBlockBehavior {
    public static final List<ItemType> ALL_SWORDS = ImmutableList.of(ItemTypes.WOODEN_SWORD, ItemTypes.STONE_SWORD, ItemTypes.IRON_SWORD, ItemTypes.GOLDEN_SWORD, ItemTypes.DIAMOND_SWORD);
    public static final List<ItemType> ALL_PICKAXES = ImmutableList.of(ItemTypes.WOODEN_PICKAXE, ItemTypes.STONE_PICKAXE, ItemTypes.IRON_PICKAXE, ItemTypes.GOLDEN_PICKAXE, ItemTypes.DIAMOND_PICKAXE);
    public static final List<ItemType> ALL_AXES = ImmutableList.of(ItemTypes.WOODEN_AXE, ItemTypes.STONE_AXE, ItemTypes.IRON_AXE, ItemTypes.GOLDEN_AXE, ItemTypes.DIAMOND_AXE);
    public static final List<ItemType> ALL_SHOVELS = ImmutableList.of(ItemTypes.WOODEN_SHOVEL, ItemTypes.STONE_SHOVEL, ItemTypes.IRON_SHOVEL, ItemTypes.GOLDEN_SHOVEL, ItemTypes.DIAMOND_SHOVEL);

    protected final List<ItemType> allowedTypes;

    public DecreaseBreakTimeBySpecificToolsBehaviour(List<ItemType> allowedTypes) {
        Preconditions.checkNotNull(allowedTypes, "allowedTypes");
        this.allowedTypes = allowedTypes;
    }

    public List<ItemType> getAllowedTypes() {
        return allowedTypes;
    }
}