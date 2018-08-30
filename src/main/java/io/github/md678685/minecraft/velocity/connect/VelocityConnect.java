package io.github.md678685.minecraft.velocity.connect;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class VelocityConnect extends JavaPlugin {

    final Map<String, CompletableFuture<WrappedGameProfile>> profileMap = new HashMap<>();

    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        registerListeners();
    }

    private void registerListeners() {
        protocolManager.addPacketListener(new VelocityPacketAdapter(this, protocolManager));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    // Config methods

    String getForwardingSecret() {
        return getConfig().getString("velocity.forwarding_secret", "5up3r53cr3t");
    }

    boolean getAllowUnauthenticatedConnections() {
        return getConfig().getBoolean("connect.allow_unauthenticated_connections", false);
    }

}
