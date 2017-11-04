package com.voxelwind.server.game.inventories.transaction.record;

import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.api.game.level.block.BlockTypes;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.game.inventories.transaction.ContainerIds;
import com.voxelwind.server.network.session.PlayerSession;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Optional;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class ContainerTransactionRecord extends TransactionRecord{
    private int inventoryId;

    @Override
    public void write(ByteBuf buffer){
        Varints.encodeSigned(buffer, inventoryId);
        super.write(buffer);
    }

    @Override
    public void read(ByteBuf buffer){
        inventoryId = Varints.decodeSigned(buffer);
        super.read(buffer);
    }

    @Override
    public void execute(PlayerSession session) {
        switch (inventoryId) {
            // TODO Add checks
            case ContainerIds.INVENTORY:
                Optional<ItemStack> actualItem = session.getInventory().getItem(getSlot());

                if ((actualItem.isPresent() && !actualItem.get().equals(getOldItem())) ||
                        !actualItem.isPresent() && getOldItem().getItemType() != BlockTypes.AIR) {
                    // Not actually the same item.
                    session.sendPlayerInventory();
                    return;
                }

                session.getInventory().setItem(getSlot(), getNewItem());
                break;
            case ContainerIds.CURSOR:
                Optional<ItemStack> cursorItem = session.getInventory().getCursorItem();

                if (cursorItem.isPresent() && !cursorItem.get().equals(getOldItem()) ||
                        !cursorItem.isPresent() && getOldItem().getItemType() != BlockTypes.AIR) {
                    // Not actually the same item.
                    session.sendPlayerInventory();
                    return;
                }

                session.setCursorItem(getNewItem(), false);
                break;
        }
    }
}
