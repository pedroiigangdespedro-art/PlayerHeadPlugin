package fr.pedroii.playerhead;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DisguiseManager {

    private final Plugin plugin;
    private final Map<UUID, DisguiseData> disguisedPlayers = new HashMap<>();

    public DisguiseManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void applyDisguise(Player wearer, String ownerName, PlayerProfile ownerProfile) {
        UUID wearerUUID = wearer.getUniqueId();

        if (disguisedPlayers.containsKey(wearerUUID)) {
            DisguiseData existing = disguisedPlayers.get(wearerUUID);
            if (existing.disguisedAs().equals(ownerName)) return;
            restorePlayer(wearer, false);
        }

        PlayerProfile originalProfile = wearer.getPlayerProfile();
        Component originalDisplayName = wearer.displayName();
        Component originalPlayerListName = wearer.playerListName();

        disguisedPlayers.put(wearerUUID, new DisguiseData(
                ownerName, originalProfile, originalDisplayName, originalPlayerListName
        ));

        PlayerProfile fakeProfile = Bukkit.createProfile(wearerUUID, ownerName);
        for (ProfileProperty prop : ownerProfile.getProperties()) {
            fakeProfile.setProperty(prop);
        }

        applyProfileWithRespawn(wearer, fakeProfile, ownerName);
    }

    public void restorePlayer(Player player, boolean notify) {
        UUID uuid = player.getUniqueId();
        DisguiseData data = disguisedPlayers.remove(uuid);
        if (data == null) return;

        applyProfileWithRespawn(player, data.originalProfile(), data.originalProfile().getName());

        if (notify) {
            player.sendMessage("§6[PlayerHead] §fYour disguise has been removed.");
        }
    }

    public void restoreAll() {
        new HashSet<>(disguisedPlayers.keySet()).forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) restorePlayer(p, false);
        });
        disguisedPlayers.clear();
    }

    public boolean isDisguised(Player player) {
        return disguisedPlayers.containsKey(player.getUniqueId());
    }

    public String getDisguisedName(Player player) {
        DisguiseData data = disguisedPlayers.get(player.getUniqueId());
        return data != null ? data.disguisedAs() : null;
    }

    private void applyProfileWithRespawn(Player player, PlayerProfile profile, String displayName) {
        player.setPlayerProfile(profile);

        Component nameComponent = Component.text(displayName).color(NamedTextColor.WHITE);
        player.customName(nameComponent);
        player.setCustomNameVisible(true);
        player.playerListName(nameComponent);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            other.hidePlayer(plugin, player);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (other.equals(player)) continue;
                    other.showPlayer(plugin, player);
                }
            }
        }.runTaskLater(plugin, 5L);
    }

    private record DisguiseData(
            String disguisedAs,
            PlayerProfile originalProfile,
            Component originalDisplayName,
            Component originalPlayerListName
    ) {}
}
