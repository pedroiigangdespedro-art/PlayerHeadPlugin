package fr.pedroii.playerhead;

import org.bukkit.plugin.java.JavaPlugin;

public class PlayerHeadPlugin extends JavaPlugin {

    private static PlayerHeadPlugin instance;
    private DisguiseManager disguiseManager;

    @Override
    public void onEnable() {
        instance = this;
        disguiseManager = new DisguiseManager(this);

        getServer().getPluginManager().registerEvents(new HeadDropListener(this), this);
        getServer().getPluginManager().registerEvents(new HeadEquipListener(this, disguiseManager), this);

        getCommand("playerhead").setExecutor((sender, command, label, args) -> {
            sender.sendMessage("§6[PlayerHead] §fv" + getDescription().getVersion() + " §7by §bYT_Pedro_II");
            sender.sendMessage("§7Drop player heads on PvP kill. Wear to disguise!");
            return true;
        });

        getLogger().info("PlayerHeadPlugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (disguiseManager != null) {
            disguiseManager.restoreAll();
        }
        getLogger().info("PlayerHeadPlugin disabled.");
    }

    public static PlayerHeadPlugin getInstance() {
        return instance;
    }

    public DisguiseManager getDisguiseManager() {
        return disguiseManager;
    }
}
