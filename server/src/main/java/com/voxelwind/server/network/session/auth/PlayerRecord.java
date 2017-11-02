package com.voxelwind.server.network.session.auth;

import com.voxelwind.api.server.Skin;
import lombok.Data;

import java.util.UUID;

@Data
public class PlayerRecord {
    private final UUID uuid;
    private long xuid;
    private long entityId;
    private String name;
    private Skin skin;
}
