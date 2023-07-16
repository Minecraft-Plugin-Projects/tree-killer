package me.kodysimpson.treebreaker.listeners;

import me.kodysimpson.treebreaker.TreeBreaker;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BreakTreeListener implements Listener {

    private final TreeBreaker plugin;

    public BreakTreeListener(TreeBreaker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        //Make sure the player is using an axe
        Material type = e.getPlayer().getInventory().getItemInMainHand().getType();

        if (!type.name().endsWith("_AXE")) {
            return;
        }

        //Make sure the block is a log
        Material originalBlockType = e.getBlock().getType();
        if (!isLog(originalBlockType)) {
            return;
        }

        if (!e.getPlayer().hasPermission("treekiller.break") || plugin.getDisabledPlayers().contains(e.getPlayer().getUniqueId())) {
            return;
        }

        //Set that will hold all the blocks that need to be broken. Does not allow duplicates
        LinkedHashSet<Block> blocksToBreak = new LinkedHashSet<>();
        var leafCount = new AtomicInteger(0);
        var nonTreeBlockCount = new AtomicInteger(0);
        breakAdjacentLogs(originalBlockType, e.getBlock(), blocksToBreak, leafCount, nonTreeBlockCount);

        //Now, we need to make a decision on whether this is a real tree or not
        //First, there needs to be a minimum number of leaves.
        //Secondly, there cannot be too many non-tree blocks.

        System.out.println("Leaf count: " + leafCount.get());
        System.out.println("Non-tree block count: " + nonTreeBlockCount.get());
        System.out.println("Blocks to break: " + blocksToBreak.size());

        if (leafCount.get() < getMinimumLeafCount(originalBlockType) || nonTreeBlockCount.get() > 100) {
            System.out.println("Not a tree");
            return;
        }

        //Break all the blocks. The delay should be faster if the amount of logs
        //is greater.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (blocksToBreak.isEmpty()) {
                    this.cancel();
                    return;
                }

                Block block = blocksToBreak.iterator().next();
                block.breakNaturally();
                e.getPlayer().playSound(block.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1f, 1f);
                blocksToBreak.remove(block);
            }
        }.runTaskTimer(plugin, 0, 0);

    }

    private int getMinimumLeafCount(Material treeType){
        switch(treeType){
            case CRIMSON_STEM:
            case WARPED_STEM:
                return 7;
            default:
                return 25;
        }
    }

    private void breakAdjacentLogs(Material originalType, Block block, Set<Block> blocksToBreak, AtomicInteger leafCount, AtomicInteger nonTreeBlockCount) {
        if (!isLog(block.getType()) || block.getType() != originalType) {

            if (isLeaf(block.getType())) {
                leafCount.incrementAndGet();
            } else if (!block.getType().isAir() && !isTreeMaterialType(block.getType())) {
                nonTreeBlockCount.incrementAndGet();
            }

            return;
        }

        //See if this is a block we have already checked
        if (blocksToBreak.contains(block)) {
            return;
        }

        blocksToBreak.add(block);

        for (Block adjacentBlock : getAdjacentBlocks(block)) {
            breakAdjacentLogs(originalType, adjacentBlock, blocksToBreak, leafCount, nonTreeBlockCount);
        }
    }

    private boolean isTreeMaterialType(Material material) {
        var treeMaterials = plugin.getConfig().getStringList("tree-materials");
        return treeMaterials.contains(material.name());
    }


    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG") || material.name().endsWith("_STEM");
    }

    private boolean isLeaf(Material material) {
        return material.name().endsWith("_LEAVES") || material == Material.WARPED_WART_BLOCK || material == Material.NETHER_WART_BLOCK;
    }

    //Return all blocks in a 1 block radius around the given block,
    //even if they are not directly adjacent such as diagonally
    private Block[] getAdjacentBlocks(Block block) {
        return new Block[]{
                block.getRelative(1, 0, 0),
                block.getRelative(-1, 0, 0),
                block.getRelative(0, 1, 0),
                block.getRelative(0, -1, 0),
                block.getRelative(0, 0, 1),
                block.getRelative(0, 0, -1),
                block.getRelative(1, 1, 0),
                block.getRelative(-1, 1, 0),
                block.getRelative(0, 1, 1),
                block.getRelative(0, 1, -1),
                block.getRelative(1, 0, 1),
                block.getRelative(-1, 0, 1),
                block.getRelative(1, 0, -1),
                block.getRelative(-1, 0, -1),
                block.getRelative(1, -1, 0),
                block.getRelative(-1, -1, 0),
                block.getRelative(0, -1, 1),
                block.getRelative(0, -1, -1),
                block.getRelative(1, 1, 1),
                block.getRelative(-1, 1, 1),
                block.getRelative(1, 1, -1),
                block.getRelative(-1, 1, -1),
                block.getRelative(1, -1, 1),
                block.getRelative(-1, -1, 1),
                block.getRelative(1, -1, -1),
                block.getRelative(-1, -1, -1),
        };
    }

}
