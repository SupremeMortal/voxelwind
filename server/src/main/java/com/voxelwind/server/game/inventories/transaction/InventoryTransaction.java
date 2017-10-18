package com.voxelwind.server.game.inventories.transaction;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.server.game.inventories.transaction.record.TransactionRecord;
import com.voxelwind.server.game.inventories.transaction.type.TransactionType;
import com.voxelwind.server.network.mcpe.packets.McpeInventoryTransaction;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InventoryTransaction {
    private final List<TransactionRecord> transactions = new ArrayList<>();
    private TransactionType transactionType;

    public McpeInventoryTransaction.Type getType(){
        return transactionType.getType();
    }
}
