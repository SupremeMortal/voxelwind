package com.voxelwind.server.network.raknet;

import lombok.experimental.UtilityClass;

import javax.xml.bind.DatatypeConverter;

@UtilityClass
public class RakNetConstants {
    public static final byte[] RAKNET_UNCONNECTED_MAGIC = DatatypeConverter.parseHexBinary("00ffff00fefefefefdfdfdfd12345678");
    public static final byte RAKNET_PROTOCOL_VERSION = 8; // Mojangs version.
    public static final short MINIMUM_MTU_SIZE = 400;
    public static final short MAXIMUM_MTU_SIZE = 1492;
    public static final int MAX_ENCAPSULATED_HEADER_SIZE = 9;
    public static final int MAX_MESSAGE_HEADER_SIZE = 23;
}
