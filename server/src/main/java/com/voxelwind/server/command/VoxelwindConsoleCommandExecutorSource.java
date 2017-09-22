package com.voxelwind.server.command;

import com.google.common.base.Preconditions;
import com.voxelwind.api.server.command.sources.ConsoleCommandExecutorSource;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;

@Log4j2
public class VoxelwindConsoleCommandExecutorSource implements ConsoleCommandExecutorSource {

    @Nonnull
    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public void sendMessage(@Nonnull String text) {
        Preconditions.checkNotNull(text, "text");
        log.info(text);
    }
}
