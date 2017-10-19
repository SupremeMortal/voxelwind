package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.mcpe.util.ResourcePackIdVersions;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public class McpeResourcePackClientResponse implements NetworkPackage {
    private byte responseStatus;
    private short resourcePackIdVersions;
    @Getter
    private final List<ResourcePackIdVersions> packIds = new ArrayList<>();


    @Override
    public void decode(ByteBuf buffer) {
        responseStatus = buffer.readByte();
        int idCount = buffer.readShortLE();
        for (int i = 0; i < idCount; i++) {
            packIds.add(new ResourcePackIdVersions(
                    McpeUtil.readVarintLengthString(buffer),
                    McpeUtil.readVarintLengthString(buffer)
            ));
        }
    }

    @Override
    public void encode(ByteBuf buffer) {
        throw new UnsupportedOperationException();
    }
}
