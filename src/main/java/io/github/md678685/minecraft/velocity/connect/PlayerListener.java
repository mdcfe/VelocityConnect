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

    private static Class<? extends PlayerProfile> craftPlayerProfileClass = null;

    private final VelocityConnect connectPlugin;

    PlayerListener(VelocityConnect plugin) {
        this.connectPlugin = plugin;
    }

    private static Class<? extends PlayerProfile> getCraftPlayerProfileClass() {
        if (craftPlayerProfileClass == null) {
            try {
                craftPlayerProfileClass = Class.forName("com.destroystokyo.paper.profile.CraftPlayerProfile").asSubclass(PlayerProfile.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return craftPlayerProfileClass;
    }

    private static ConstructorAccessor getCraftPlayerProfileConstructor() {
        return Accessors.getConstructorAccessor(getCraftPlayerProfileClass(), MinecraftReflection.getGameProfileClass());
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
                event.setPlayerProfile((PlayerProfile) getCraftPlayerProfileConstructor().invoke(profile.getHandle()));
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
