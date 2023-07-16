package me.kodysimpson.treebreaker.commands;

import me.kodysimpson.treebreaker.TreeBreaker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand implements CommandExecutor {

    private final TreeBreaker plugin;

    public ToggleCommand(TreeBreaker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player p){
            if (args.length > 0 && args[0].equalsIgnoreCase("toggle")){

                if (!p.hasPermission("treekiller.toggle")){
                    p.sendMessage("You do not have permission to use this command!");
                    return true;
                }

                if (plugin.getDisabledPlayers().contains(p.getUniqueId())){
                    plugin.getDisabledPlayers().remove(p.getUniqueId());
                    p.sendMessage("Tree Killer activated!");
                }else{
                    plugin.getDisabledPlayers().add(p.getUniqueId());
                    p.sendMessage("You can no longer break trees in one hit!");
                }
            }else{
                p.sendMessage("Usage: /treekiller toggle");
            }
        }else{
            sender.sendMessage("You must be a player to use this command!");
        }

        return true;
    }

}
