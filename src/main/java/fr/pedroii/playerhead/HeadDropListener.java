package fr.pedroii.playerhead;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class HeadDropListener implements Listener {

    private final PlayerHeadPlugin plugin;

    public HeadDropListener(PlayerHeadPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) {
            return;
        }

        ItemStack head = createPlayerHead(victim);
        victim.getWorld().dropItemNaturally(victim.getLocation(), head);
        killer.sendMessage("§6[PlayerHead] §fYou got §b" + victim.getName() + "§f's head!");
    }

    public static ItemStack createPlayerHead(Player victim) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        PlayerProfile profile = Bukkit.createProfile(victim.getUniqueId(), victim.getName());
        PlayerProfile onlineProfile = victim.getPlayerProfile();
        for (ProfileProperty prop : onlineProfile.getProperties()) {
            profile.setProperty(prop);
        }

        meta.setPlayerProfile(profile);
        meta.setDisplayName("§b" + victim.getName() + "§f's Head");

        org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey(PlayerHeadPlugin.getInstance(), "head_owner_uuid");
        org.bukkit.NamespacedKey nameKey = new org.bukkit.NamespacedKey(PlayerHeadPlugin.getInstance(), "head_owner_name");
        meta.getPersistentDataContainer().set(ownerKey,
                org.bukkit.persistence.PersistentDataType.STRING,
                victim.getUniqueId().toString());
        meta.getPersistentDataContainer().set(nameKey,
                org.bukkit.persistence.PersistentDataType.STRING,
                victim.getName());

        head.setItemMeta(meta);
        return head;
    }

    public static boolean isPluginHead(ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return false;
        if (!item.hasItemMeta()) return false;
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey(PlayerHeadPlugin.getInstance(), "head_owner_uuid");
        return meta.getPersistentDataContainer().has(ownerKey, org.bukkit.persistence.PersistentDataType.STRING);
    }

    public static UUID getHeadOwnerUUID(ItemStack item) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        org.bukkit.NamespacedKey ownerKey = new org.bukkit.NamespacedKey(PlayerHeadPlugin.getInstance(), "head_owner_uuid");
        String uuidStr = meta.getPersistentDataContainer().get(ownerKey, org.bukkit.persistence.PersistentDataType.STRING);
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    }

    public static String getHeadOwnerName(ItemStack item) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        org.bukkit.NamespacedKey nameKey = new org.bukkit.NamespacedKey(PlayerHeadPlugin.getInstance(), "head_owner_name");
        return meta.getPersistentDataContainer().get(nameKey, org.bukkit.persistence.PersistentDataType.STRING);
    }
}
