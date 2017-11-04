package com.voxelwind.server.game.level.block.behaviors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.api.game.item.ItemType;
import com.voxelwind.api.game.level.block.Block;
import com.voxelwind.api.server.Player;
import com.voxelwind.api.server.Server;
import com.voxelwind.server.game.level.block.BlockBehavior;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import static com.voxelwind.api.game.item.ItemTypes.*;

public class DroppableBySpecificToolsBlockBehavior extends DecreaseBreakTimeBySpecificToolsBehaviour {
    public static final List<ItemType> ALL_PICKAXES = ImmutableList.of(WOODEN_PICKAXE, DIAMOND_PICKAXE, GOLDEN_PICKAXE, IRON_PICKAXE, STONE_PICKAXE);
    public static final List<ItemType> ALL_STONE_PICKAXES = ImmutableList.of(DIAMOND_PICKAXE, GOLDEN_PICKAXE, IRON_PICKAXE, STONE_PICKAXE);
    public static final List<ItemType> ALL_IRON_PICKAXES = ImmutableList.of(DIAMOND_PICKAXE, GOLDEN_PICKAXE, IRON_PICKAXE);
    public static final List<ItemType> ALL_GOLD_PICKAXES = ImmutableList.of(DIAMOND_PICKAXE, GOLDEN_PICKAXE);
    public static final List<ItemType> DIAMOND_PICKAXES = ImmutableList.of(DIAMOND_PICKAXE, GOLDEN_PICKAXE);
    public static final List<ItemType> SHEARS_ONLY = ImmutableList.of(SHEARS);

    public DroppableBySpecificToolsBlockBehavior(List<ItemType> allowedTypes) {
        super(allowedTypes);
    }

    @Override
    public Collection<ItemStack> getDrops(Server server, Player player, Block block, @Nullable ItemStack withItem) {
        if (withItem != null && allowedTypes.contains(withItem.getItemType())) {
            return super.getDrops(server, player, block, withItem);
        }
        return ImmutableList.of();
    }
}