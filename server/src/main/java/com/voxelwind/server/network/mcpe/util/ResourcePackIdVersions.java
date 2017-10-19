package com.voxelwind.server.network.mcpe.util;

import lombok.NonNull;
import lombok.Value;

@Value
public class ResourcePackIdVersions {
    private String id;
    private String version;

    public ResourcePackIdVersions(@NonNull String id, @NonNull String version) {
        this.id = id;
        this.version = version;
    }
}
