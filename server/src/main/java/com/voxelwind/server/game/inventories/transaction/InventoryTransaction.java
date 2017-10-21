package com.voxelwind.server.game.inventories.transaction;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.game.inventories.transaction.record.TransactionRecord;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.session.McpeSession;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class InventoryTransaction {
    @Getter
    private List<TransactionRecord> records = new ArrayList<>();
    private int slot;
    private ItemStack item;
    private Vector3f fromPosition;

    public void read(ByteBuf buffer) {
        slot = Varints.decodeSigned(buffer);
        item = McpeUtil.readItemStack(buffer);
        fromPosition = McpeUtil.readVector3f(buffer);
    }

    public void write(ByteBuf buffer) {
        Varints.encodeSigned(buffer, slot);
        McpeUtil.writeItemStack(buffer, item);
        McpeUtil.writeVector3f(buffer, fromPosition);
    }

    public abstract Type getType();

    public abstract void handle(McpeSession session);

    public enum Type {
        NORMAL,
        INVENTORY_MISMATCH,
        ITEM_USE,
        ITEM_USE_ON_ENTITY,
        ITEM_RELEASE
    }
}
