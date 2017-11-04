package com.voxelwind.server.game.level.block.behaviors;

import com.google.common.collect.ImmutableList;
import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.api.game.item.ItemType;
import com.voxelwind.api.game.item.ItemTypes;
import com.voxelwind.api.game.level.block.Block;
import com.voxelwind.api.server.Player;
import com.voxelwind.api.server.Server;
import com.voxelwind.server.game.level.block.BlockBehavior;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class CobwebBlockBehavior extends DecreaseBreakTimeBySpecificToolsBehaviour {
    public static final BlockBehavior INSTANCE = new CobwebBlockBehavior();

    public CobwebBlockBehavior() {
        super(ImmutableList.of(ItemTypes.WOODEN_SWORD, ItemTypes.STONE_SWORD, ItemTypes.IRON_SWORD, ItemTypes.GOLDEN_SWORD, ItemTypes.DIAMOND_SWORD, ItemTypes.SHEARS));
    }

    @Override
    public Collection<ItemStack> getDrops(Server server, Player player, Block block, @Nullable ItemStack with) {
        if (with != null) {
            if (with.getItemType() == ItemTypes.SHEARS || with.getItemType() == ItemTypes.IRON_SWORD || with.getItemType() == ItemTypes.GOLDEN_SWORD ||
                    with.getItemType() == ItemTypes.DIAMOND_SWORD) {
                return ImmutableList.of(server.createItemStackBuilder()
                        .itemType(with.getItemType() == ItemTypes.SHEARS ? block.getBlockState().getBlockType() : ItemTypes.STRING)
                        .amount(1)
                        .build());
            }
        }
        return ImmutableList.of();
    }
}