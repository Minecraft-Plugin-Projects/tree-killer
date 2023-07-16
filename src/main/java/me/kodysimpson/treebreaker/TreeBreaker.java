package me.kodysimpson.treebreaker;

import me.kodysimpson.treebreaker.commands.ToggleCommand;
import me.kodysimpson.treebreaker.listeners.BreakTreeListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class TreeBreaker extends JavaPlugin {

    private TreeBreaker plugin;
    //Players who will not be able to break trees in one hit
    private final HashSet<UUID> disabledPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        // Plugin startup logic

        this.plugin = this;

        getCommand("treekiller").setExecutor(new ToggleCommand(this));

        getServer().getPluginManager().registerEvents(new BreakTreeListener(this), this);

        //config stuff
        getConfig().options().copyDefaults();
        saveDefaultConfig();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public HashSet<UUID> getDisabledPlayers() {
        return disabledPlayers;
    }
}
