package com.voxelwind.server.game.inventories.transaction;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.server.game.inventories.transaction.record.TransactionRecord;
import com.voxelwind.server.network.mcpe.packets.McpeInventoryTransaction;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Transaction {
    private McpeInventoryTransaction.TransactionType transactionType;
    private final List<TransactionRecord> transactions = new ArrayList<>();
    private int actionType;
    private Vector3i position;
    private int face;
    private int slot;
    private ItemStack item;
    private Vector3f fromPosition;
    private Vector3f clickPosition;
    private long entityId;
}
