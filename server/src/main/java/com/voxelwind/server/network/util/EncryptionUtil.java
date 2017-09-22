package com.voxelwind.server.network.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.voxelwind.server.network.mcpe.packets.McpeServerToClientHandshake;
import io.netty.util.AsciiString;
import lombok.extern.log4j.Log4j2;
import javax.crypto.KeyAgreement;
import java.net.URI;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.util.Base64;

@Log4j2
public class EncryptionUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    private EncryptionUtil() {

    }

    public static byte[] getServerKey(KeyPair serverPair, PublicKey key, byte[] token) throws InvalidKeyException {
        byte[] sharedSecret = getSharedSecret(serverPair, key);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        digest.update(token);
        digest.update(sharedSecret);
        return digest.digest();
    }

    private static byte[] getSharedSecret(KeyPair serverPair, PublicKey clientKey) throws InvalidKeyException {
        KeyAgreement agreement;
        try {
            agreement = KeyAgreement.getInstance("ECDH");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        agreement.init(serverPair.getPrivate());
        agreement.doPhase(clientKey, true);
        return agreement.generateSecret();
    }

    public static McpeServerToClientHandshake createHandshakePacket(KeyPair pair, byte[] token) {
        ECPrivateKey privKey = (ECPrivateKey) pair.getPrivate();
        URI x5u = URI.create(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        McpeServerToClientHandshake handshake = new McpeServerToClientHandshake();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().claim("salt", Base64.getUrlEncoder().encodeToString(token)).build();
        SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES384).x509CertURL(x5u).build(),
                claimsSet
        );

        try {
            JWSSigner signer = new ECDSASigner(privKey);
            jwt.sign(signer);
        } catch (JOSEException e) {
            throw new RuntimeException("Unable to sign JWT", e);
        }
        handshake.setPayload(new AsciiString(jwt.serialize()));
        return handshake;
    }

    public static byte[] generateRandomToken() {
        byte[] token = new byte[16];
        secureRandom.nextBytes(token);
        return token;
    }
}
