package com.voxelwind.server.jni.hash;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.jni.NativeCode;
import org.junit.Assert;
import org.junit.Test;

public class NativeHashTest
{

    private static final byte[] INPUT_1 = "Hello, world".getBytes();
    private static final byte[] INPUT_2 = "Voxelwind".getBytes();
    private static final byte[] EXPECTED_HASH_1 = new byte[]{
            74, -25, -61, -74, -84, 11, -17, -10, 113, -17, -88, -49, 87, 56, 97, 81, -64, 110, 88, -54, 83, -89, -115, -125, -13, 97, 7, 49, 108, -20, 18, 95
    };
    private static final byte[] EXPECTED_HASH_2 = new byte[]{
            33, 37, 33, 38, 79, 22, 54, -58, 118, 94, 57, -32, 85, 65, -105, 44, 64, 44, 94, 10, 90, -110, 32, 36, -2, -40, 58, -35, -36, -15, -43, 29
    };

    private final NativeCode<VoxelwindHash> factory = new NativeCode<>("native-hash", JavaHash.class, NativeHash.class);

    @Test
    public void doTest()
    {
        if ( NativeCode.isSupported() )
        {
            Assert.assertTrue( "Native code failed to load!", factory.load() );
            test( factory.newInstance() );
        }
        test( new JavaHash() );
    }

    private void test(VoxelwindHash hash)
    {
        System.out.println( "Testing: " + hash );

        ByteBuf buf1 = Unpooled.directBuffer();
        buf1.writeBytes(INPUT_1);
        hash.update( buf1 );
        byte[] out = hash.digest();

        Assert.assertArrayEquals( "First hash does not match", EXPECTED_HASH_1, out );

        // Test multiple hashes with same instance
        ByteBuf buf2 = Unpooled.directBuffer();
        buf2.writeBytes(INPUT_2);
        hash.update( buf2 );
        byte[] out2 = hash.digest();

        Assert.assertArrayEquals( "Second hash does not match", EXPECTED_HASH_2, out2 );

        buf1.release();
        buf2.release();
    }
}
