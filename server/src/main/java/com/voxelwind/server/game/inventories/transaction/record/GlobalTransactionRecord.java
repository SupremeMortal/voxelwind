package com.voxelwind.server.game.inventories.transaction.record;

import com.voxelwind.server.network.session.PlayerSession;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class GlobalTransactionRecord extends TransactionRecord {

    @Override
    public void execute(PlayerSession session) {
        // TODO
    }
}
