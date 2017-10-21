package com.voxelwind.server.network.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.voxelwind.api.server.event.session.SessionLoginEvent;
import com.voxelwind.server.VoxelwindServer;
import com.voxelwind.server.game.inventories.transaction.*;
import com.voxelwind.server.jni.CryptoUtil;
import com.voxelwind.server.network.mcpe.packets.*;
import com.voxelwind.server.network.mcpe.util.VersionUtil;
import com.voxelwind.server.network.raknet.handler.NetworkPacketHandler;
import com.voxelwind.server.network.session.auth.ClientData;
import com.voxelwind.server.network.session.auth.JwtPayload;
import com.voxelwind.server.network.session.auth.TemporarySession;
import com.voxelwind.server.network.util.EncryptionUtil;
import com.voxelwind.server.network.util.NativeCodeFactory;
import io.netty.util.AsciiString;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Log4j2
public class InitialNetworkPacketHandler implements NetworkPacketHandler {
    private static final boolean CAN_USE_ENCRYPTION = CryptoUtil.isJCEUnlimitedStrength() || NativeCodeFactory.cipher.isLoaded();
    private static final String MOJANG_PUBLIC_KEY_BASE64 =
            "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V";
    private static final PublicKey MOJANG_PUBLIC_KEY;

    static {
        try {
            MOJANG_PUBLIC_KEY = generateKey(MOJANG_PUBLIC_KEY_BASE64);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private final McpeSession session;

    public InitialNetworkPacketHandler(McpeSession session) {
        this.session = session;
    }

    private static PublicKey generateKey(String b64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(b64)));
    }

    @Override
    public void handle(McpeSubClientLogin packet){
        verifyLogin(packet.getChainData(), packet.getSkinData());
    }

    @Override
    public void handle(McpeLogin packet) {
        if (!VersionUtil.isCompatible(packet.getProtocolVersion())) {
            Optional<InetSocketAddress> address = session.getRemoteAddress();
            int[] compatible = VersionUtil.getCompatibleProtocolVersions();
            if (address.isPresent()) {
                log.error("Client {} has protocol version {}, not one of {}", address.get(), packet.getProtocolVersion(),
                        Arrays.toString(compatible));
            } else {
                log.error("Client has protocol version {}, not {}", packet.getProtocolVersion(),
                        Arrays.toString(compatible));
            }

            List<String> friendly = new ArrayList<>();
            for (int i : compatible) {
                String human = VersionUtil.getHumanVersionName(i);
                if (human != null) {
                    friendly.add(human);
                }
            }
            String joined = Joiner.on(", ").join(friendly);
            int lastComma = joined.lastIndexOf(',');
            String fixed = lastComma == -1 ? joined : joined.substring(0, lastComma) + " or " + joined.substring(lastComma + 1);
            session.disconnect("This server requires Minecraft: Pocket Edition " + fixed);
            return;
        }

        verifyLogin(packet.getChainData(), packet.getSkinData());
    }

    @Override
    public void handle(McpeClientToServerHandshake packet) {
        initializePlayerSession();
    }

    @Override
    public void handle(McpeRequestChunkRadius packet) {
        throw new IllegalStateException("Got unexpected McpeRequestChunkRadius");
    }
    @Override
    public void handle(McpeAdventureSettings packet) {
        throw new IllegalStateException("Got unexpected McpeAdventureSettings");
    }

    @Override
    public void handle(McpePlayerAction packet) {
        throw new IllegalStateException("Got unexpected McpePlayerAction");
    }

    @Override
    public void handle(McpeAnimate packet) {
        throw new IllegalStateException("Got unexpected McpeAnimate");
    }

    @Override
    public void handle(McpeText packet) {
        throw new IllegalStateException("Got unexpected McpeText");
    }

    @Override
    public void handle(McpeMovePlayer packet) {
        throw new IllegalStateException("Got unexpected McpeMovePlayer");
    }

    @Override
    public void handle(McpeContainerClose packet) {
        throw new IllegalStateException("Got unexpected McpeContainerClose");
    }

    @Override
    public void handle(McpeInventorySlot packet) {
        throw new IllegalStateException("Got unexpected McpeInventorySlot");
    }

    @Override
    public void handle(McpeMobEquipment packet) {
        throw new IllegalStateException("Got unexpected McpeMobEquipment");
    }

    @Override
    public void handle(McpeInventoryTransaction packet) {
        throw new IllegalStateException("Got unexpected McpeRemoveBlock");
    }

    @Override
    public void handle(McpeResourcePackClientResponse packet) {
        throw new IllegalStateException("Got unexpected McpeResourcePackClientResponse");
    }

    @Override
    public void handle(McpeCommandRequest packet) {
        throw new IllegalStateException("Got unexpected McpeCommandRequest");
    }

    public void handle(NormalTransaction transaction) {
        throw new IllegalStateException("Got unexpected NormalTransaction");
    }

    public void handle(InventoryMismatchTransaction transaction) {
        throw new IllegalStateException("Got unexpected InventoryMismatchTransaction");
    }

    public void handle(ItemUseTransaction transaction) {
        throw new IllegalStateException("Got unexpected ItemUseTransaction");
    }

    public void handle(ItemUseOnEntityTransaction transaction) {
        throw new IllegalStateException("Got unexpected ItemUseOnEntityTransaction");
    }

    public void handle(ItemReleaseTransaction transaction) {
        throw new IllegalStateException("Got unexpected ItemReleaseTransaction");
    }

    private void verifyLogin(AsciiString chainData, AsciiString skinData){
        JsonNode certData;
        try {
            certData = VoxelwindServer.MAPPER.readTree(chainData.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Certificate JSON can not be read.");
        }

        // Verify the JWT chain data.
        JsonNode certChainData = certData.get("chain");
        if (certChainData.getNodeType() != JsonNodeType.ARRAY) {
            throw new RuntimeException("Certificate data is not valid");
        }

        try {
            boolean trustedChain = validateChainData(certChainData);
            if (!trustedChain) {
                session.disconnect("This server requires that you sign in with Xbox Live.");
                return;
            }

            JwtPayload payload = VoxelwindServer.MAPPER.convertValue(
                    getPayload(certChainData.get(certChainData.size() - 1).asText()), JwtPayload.class);

            session.setAuthenticationProfile(payload.getExtraData());

            // Get the key to use for verifying the client data and encrypting the connection
            PublicKey key = generateKey(payload.getIdentityPublicKey());

            // Set the client data.
            ClientData clientData = getClientData(key, skinData.toString());
            session.setClientData(clientData);

            if (CAN_USE_ENCRYPTION) {
                startEncryptionHandshake(key);
            } else {
                initializePlayerSession();
            }
        } catch (Exception e) {
            log.error("Unable to initialize player session", e);
            session.disconnect("Internal server error");
        }
    }
    private void startEncryptionHandshake(PublicKey key) throws Exception {
        // Generate a fresh key for each session
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp384r1"));
        KeyPair serverKeyPair = generator.generateKeyPair();

        // Enable encryption server-side
        byte[] token = EncryptionUtil.generateRandomToken();
        byte[] serverKey = EncryptionUtil.getServerKey(serverKeyPair, key, token);
        session.enableEncryption(serverKey);

        // Now send the packet to enable encryption on the client
        session.sendImmediatePackage(EncryptionUtil.createHandshakePacket(serverKeyPair, token));
    }

    private void initializePlayerSession() {
        TemporarySession apiSession = new TemporarySession(session);
        SessionLoginEvent event = new SessionLoginEvent(apiSession);
        session.getServer().getEventManager().fire(event);

        if (event.willDisconnect()) {
            session.disconnect(event.getDisconnectReason());
            return;
        }

        McpePlayStatus status = new McpePlayStatus();
        status.setStatus(McpePlayStatus.Status.LOGIN_SUCCESS);
        session.addToSendQueue(status);

        PlayerSession playerSession = session.initializePlayerSession(session.getServer().getDefaultLevel());
        session.setHandler(playerSession.getPacketHandler());

        McpeResourcePacksInfo info = new McpeResourcePacksInfo();
        session.addToSendQueue(info);
    }

    // Verify whether client has sent valid and trusted certificate chain
    private boolean validateChainData(JsonNode data) throws Exception {
        Preconditions.checkArgument(data.getNodeType() == JsonNodeType.ARRAY, "chain data provided is not an array");

        PublicKey lastKey = null;
        boolean trustedChain = false;
        for (JsonNode node : data) {
            JWSObject object = JWSObject.parse(node.asText());

            if (!trustedChain) {
                trustedChain = verify(MOJANG_PUBLIC_KEY, object);
            }

            if (lastKey != null) {
                if (!verify(lastKey, object)) {
                    throw new JOSEException("Unable to verify key in chain.");
                }
            }

            JsonNode payloadNode = VoxelwindServer.MAPPER.readTree(object.getPayload().toString());
            JsonNode ipkNode = payloadNode.get("identityPublicKey");
            Verify.verify(ipkNode != null && ipkNode.getNodeType() == JsonNodeType.STRING, "identityPublicKey node is missing in chain");
            lastKey = generateKey(ipkNode.asText());
        }

        return trustedChain;
    }

    private ClientData getClientData(PublicKey key, String clientData) throws Exception {
        JWSObject object = JWSObject.parse(clientData);
        if (!verify(key, object)) {
            throw new IllegalArgumentException("Unable to verify client data.");
        }

        JsonNode payload = getPayload(clientData);

        return VoxelwindServer.MAPPER.convertValue(payload, ClientData.class);
    }

    private JsonNode getPayload(String token) throws IOException {
        String payload = token.split("\\.")[1];
        return VoxelwindServer.MAPPER.readTree(Base64.getDecoder().decode(payload));
    }

    private boolean verify(PublicKey key, JWSObject object) throws JOSEException {
        JWSVerifier verifier = new DefaultJWSVerifierFactory().createJWSVerifier(object.getHeader(), key);
        return object.verify(verifier);
    }
}
