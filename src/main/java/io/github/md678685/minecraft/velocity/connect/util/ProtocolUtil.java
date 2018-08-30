package io.github.md678685.minecraft.velocity.connect.util;

import com.comphenix.protocol.utility.StreamSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class ProtocolUtil {

    public static int readVarInt(ByteBuf buf) throws IOException {
        return StreamSerializer.getDefault().deserializeVarInt(new DataInputStream(new ByteBufInputStream(buf)));
    }

    public static String readString(ByteBuf buf) throws IOException {
        return StreamSerializer.getDefault().deserializeString(new DataInputStream(new ByteBufInputStream(buf)), 32767);
    }

    public static UUID readUUID(ByteBuf buf) {
        long mostSigBits = buf.readLong();
        long leastSigBits = buf.readLong();
        return new UUID(mostSigBits, leastSigBits);
    }

}
