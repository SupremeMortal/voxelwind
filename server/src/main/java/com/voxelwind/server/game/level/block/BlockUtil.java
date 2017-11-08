package com.voxelwind.server.game.level.block;

import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.api.game.level.block.BlockTypes;
import com.voxelwind.server.game.item.VoxelwindItemStack;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BlockUtil {
    public static final ItemStack AIR = new VoxelwindItemStack(BlockTypes.AIR, 1, null);
}
