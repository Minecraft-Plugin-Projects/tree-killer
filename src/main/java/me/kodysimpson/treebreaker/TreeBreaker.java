package me.kodysimpson.treebreaker;

import me.kodysimpson.treebreaker.listeners.BreakTreeListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class TreeBreaker extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        getServer().getPluginManager().registerEvents(new BreakTreeListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
