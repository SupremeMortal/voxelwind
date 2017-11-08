package com.voxelwind.server.command.builtin;

import com.voxelwind.api.game.entities.components.PlayerData;
import com.voxelwind.api.game.util.TextFormat;
import com.voxelwind.api.server.Player;
import com.voxelwind.api.server.command.CommandExecutor;
import com.voxelwind.api.server.command.CommandExecutorSource;
import com.voxelwind.api.server.player.GameMode;

public class GamemodeCommand implements CommandExecutor {
    @Override
    public void execute(CommandExecutorSource source, String[] args) throws Exception {
        if (source instanceof Player) {
            if (args.length == 0) {
                ((Player) source).sendMessage(TextFormat.RED + "/gamemode <gamemode> [player]");
                return;
            }
            Player player = (Player) source;

            GameMode gameMode = GameMode.parse(args[0]);
            if (args.length > 1) {
                for (Player p : player.getServer().getAllOnlinePlayers()) {
                    if (p.getName().equalsIgnoreCase(args[1])) {
                        player = p;
                        break;
                    }
                }
            }
            player.ensureAndGet(PlayerData.class).setGameMode(gameMode);
        }
    }
}
