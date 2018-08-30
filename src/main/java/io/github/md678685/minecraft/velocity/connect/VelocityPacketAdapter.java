package io.github.md678685.minecraft.velocity.connect;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import io.github.md678685.minecraft.velocity.connect.util.HashUtil;
import io.github.md678685.minecraft.velocity.connect.util.ProtocolUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class VelocityPacketAdapter extends PacketAdapter {

    private static final String VELOCITY_CHANNEL_PREFIX = "velocity";
    private static final String VELOCITY_CHANNEL_KEY = "player_info";

    private final VelocityConnect connnectPlugin;

    private final ProtocolManager protocolManager;

    // The IDs sent in Login Plugin Requests and expected in corresponding Responses, mapped to username from Login Start
    // We can retrieve the username from Login Start, but not from other packets.
    private Map<Integer, String> messageIds = new HashMap<>();

    VelocityPacketAdapter(VelocityConnect plugin, ProtocolManager protocolManager) {
        super(plugin, ListenerPriority.LOWEST, PacketType.Login.Client.START, PacketType.Login.Client.CUSTOM_PAYLOAD);
        this.protocolManager = protocolManager;
        this.connnectPlugin = plugin;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (event.getPacketType() == PacketType.Login.Client.START) {
            this.onLoginStart(event);
        } else if (event.getPacketType() == PacketType.Login.Client.CUSTOM_PAYLOAD) {
            this.onLoginCustomPayload(event);
        }
    }

    /**
     * Receives Login Start packets, and immediately sends out Login Plugin Request to the client.
     * TODO: Is this the right place to send the Login Plugin Request?
     *
     * @param event The ProtocolLib packet event.
     */
    private void onLoginStart(PacketEvent event) {
        WrappedGameProfile profile = WrappedGameProfile.fromHandle(event.getPacket().getModifier().withType(MinecraftReflection.getGameProfileClass()).read(0));
        String username = profile.getName();
        PacketContainer loginMessage = createLoginMessage(username);

        try {
            plugin.getLogger().info("Attempting to send Velocity message for player: " + username);
            protocolManager.sendServerPacket(event.getPlayer(), loginMessage);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to send plugin message!");
            e.printStackTrace();
        }
    }

    /**
     * Receives Login Plugin Response packets, decodes them and handles them appropriately.
     *
     * @param event The ProtocolLib packet event.
     */
    private void onLoginCustomPayload(PacketEvent event) {
        PacketContainer packet = event.getPacket();

        int id = packet.getIntegers().read(0);
        ByteBuf data = (ByteBuf) packet.getModifier().withType(ByteBuf.class).read(0);
        boolean success = data != null;

        if (success && messageIds.containsKey(id)) {
            String username = messageIds.get(id);
            plugin.getLogger().info("Packet understood and ID valid for player: " + username);
            messageIds.remove(id);

            CompletableFuture<WrappedGameProfile> future = new CompletableFuture<>();
            connnectPlugin.profileMap.put(username, future);

            handleVelocityPayload(event, data, future);
        }
    }

    private void handleVelocityPayload(PacketEvent event, ByteBuf payload, CompletableFuture<WrappedGameProfile> future) {
        // Read the signature at the start of the payload
        byte[] sig = new byte[32];
        payload.readBytes(sig, 0, 32);

        // Copy the rest of the data for verification
        byte[] data = new byte[payload.readableBytes()];
        payload.getBytes(32, data);

        boolean valid = HashUtil.verifySignature(connnectPlugin.getForwardingSecret(), data, sig);

        if (!valid) {
            plugin.getLogger().warning("Could not verify Velocity packet signature!");
            future.complete(null);
        }

        try {
            // Deserialize the GameProfile data.
            String address = ProtocolUtil.readString(payload); // TODO: IP forwarding?
            UUID uuid = ProtocolUtil.readUUID(payload);
            String name = ProtocolUtil.readString(payload);
            int properties = ProtocolUtil.readVarInt(payload);

            // Create new profile based on deserialized data.
            WrappedGameProfile profile = new WrappedGameProfile(uuid, name);

            // Decode and add the properties
            for (int i = 0; i < properties; i++) {
                String pName = ProtocolUtil.readString(payload);
                String pValue = ProtocolUtil.readString(payload);
                String pSignature = null;

                if (payload.readBoolean()) {
                    pSignature = ProtocolUtil.readString(payload);
                }
                WrappedSignedProperty property = WrappedSignedProperty.fromValues(pName, pValue, pSignature);

                profile.getProperties().put(pName, property);
            }

            // Provide the profile to the PlayerListener
            future.complete(profile);
        } catch (IOException e) {
            future.complete(null);

            plugin.getLogger().severe("Failed to decode GameProfile!(?)");
            e.printStackTrace();
        }
    }

    /**
     * Create a Login Plugin Request packet, ready to send to the proxy/client.
     *
     * @param username The username to associate with the packet's ID
     * @return The ProtocolLib PacketContainer to send back to the proxy/client.
     */
    private PacketContainer createLoginMessage(String username) {
        PacketContainer loginMessage = protocolManager.createPacket(PacketType.Login.Server.CUSTOM_PAYLOAD);

        int nextId = ThreadLocalRandom.current().nextInt();
        loginMessage.getIntegers().write(0, nextId);
        messageIds.put(nextId, username);

        loginMessage.getMinecraftKeys().write(0, new MinecraftKey(VELOCITY_CHANNEL_PREFIX, VELOCITY_CHANNEL_KEY));
        loginMessage.getModifier().withType(MinecraftReflection.getByteBufClass()).write(0, MinecraftReflection.getPacketDataSerializer(Unpooled.buffer()));

        return loginMessage;
    }
}
