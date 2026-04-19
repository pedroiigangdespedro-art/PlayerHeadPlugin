package fr.pedroii.playerhead;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class HeadEquipListener implements Listener {

    private final PlayerHeadPlugin plugin;
    private final DisguiseManager disguiseManager;

    public HeadEquipListener(PlayerHeadPlugin plugin, DisguiseManager disguiseManager) {
        this.plugin = plugin;
        this.disguiseManager = disguiseManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        boolean affectsHelmet = false;

        // Clic direct sur le slot casque (slot 39 dans l'inventaire joueur)
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            affectsHelmet = true;
        }
        // Shift-clic depuis n'importe quel slot (peut envoyer item vers casque)
        if (event.isShiftClick()) {
            affectsHelmet = true;
        }
        // Slot 39 = casque dans l'inventaire joueur
        if (event.getRawSlot() == 5 || event.getRawSlot() == 39) {
            affectsHelmet = true;
        }

        if (!affectsHelmet) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) checkHelmetSlot(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Vérifie si le drag touche le slot casque
        if (!event.getRawSlots().contains(5) && !event.getRawSlots().contains(39)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) checkHelmetSlot(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (disguiseManager.isDisguised(player)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    disguiseManager.restorePlayer(player, false);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) checkHelmetSlot(player);
            }
        }.runTaskLater(plugin, 5L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (disguiseManager.isDisguised(player)) {
            disguiseManager.restorePlayer(player, false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) checkHelmetSlot(player);
            }
        }.runTaskLater(plugin, 10L);
    }

    private void checkHelmetSlot(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (HeadDropListener.isPluginHead(helmet)) {
            handleEquip(player, helmet);
        } else {
            if (disguiseManager.isDisguised(player)) {
                disguiseManager.restorePlayer(player, true);
            }
        }
    }

    private void handleEquip(Player player, ItemStack headItem) {
        UUID ownerUUID = HeadDropListener.getHeadOwnerUUID(headItem);
        String ownerName = HeadDropListener.getHeadOwnerName(headItem);

        if (ownerUUID == null || ownerName == null) return;

        if (ownerUUID.equals(player.getUniqueId())) {
            player.sendMessage("§6[PlayerHead] §fThat's your own head... creepy.");
            return;
        }

        PlayerProfile ownerProfile = Bukkit.createProfile(ownerUUID, ownerName);

        Player onlineOwner = Bukkit.getPlayer(ownerUUID);
        if (onlineOwner != null) {
            for (com.destroystokyo.paper.profile.ProfileProperty prop : onlineOwner.getPlayerProfile().getProperties()) {
                ownerProfile.setProperty(prop);
            }
        } else {
            if (headItem.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
                PlayerProfile skullProfile = skullMeta.getPlayerProfile();
                if (skullProfile != null) {
                    for (com.destroystokyo.paper.profile.ProfileProperty prop : skullProfile.getProperties()) {
                        ownerProfile.setProperty(prop);
                    }
                }
            }
        }

        disguiseManager.applyDisguise(player, ownerName, ownerProfile);
        player.sendMessage("§6[PlayerHead] §fYou are now disguised as §b" + ownerName + "§f!");
    }
}
