package io.github.md678685.minecraft.velocity.connect;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerListener implements Listener {

    private static ConstructorAccessor craftPlayerProfileConstructor = null;

    private final VelocityConnect connectPlugin;

    PlayerListener(VelocityConnect plugin) {
        this.connectPlugin = plugin;
    }

    private static ConstructorAccessor getCraftPlayerProfileConstructor() {
        if (craftPlayerProfileConstructor == null) {
            try {
                Class<? extends PlayerProfile> craftPlayerProfileClass = Class.forName("com.destroystokyo.paper.profile.CraftPlayerProfile").asSubclass(PlayerProfile.class);
                craftPlayerProfileConstructor = Accessors.getConstructorAccessor(craftPlayerProfileClass, MinecraftReflection.getGameProfileClass());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return craftPlayerProfileConstructor;
    }

    private static PlayerProfile getPlayerProfile(WrappedGameProfile profile) {
        return (PlayerProfile) getCraftPlayerProfileConstructor().invoke(profile.getHandle());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (connectPlugin.profileMap.containsKey(event.getName())) {
            CompletableFuture<WrappedGameProfile> profileFuture = connectPlugin.profileMap.get(event.getName());
            WrappedGameProfile profile = profileFuture.join();
            connectPlugin.profileMap.remove(event.getName());

            if (profile == null) {
                connectPlugin.getLogger().info("Failed to authenticate player: " + event.getName());
            } else {
                connectPlugin.getLogger().info("Found profile for player: " + event.getName());
                event.setPlayerProfile(getPlayerProfile(profile));
                return;
            }
        } else {
            connectPlugin.getLogger().info("Profile not found for player: " + event.getName());
        }

        if (!connectPlugin.getAllowUnauthenticatedConnections()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }

}
