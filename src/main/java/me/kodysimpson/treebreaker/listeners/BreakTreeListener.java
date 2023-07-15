package me.kodysimpson.treebreaker.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BreakTreeListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){

        //Make sure the player is using an axe
        Material type = e.getPlayer().getInventory().getItemInMainHand().getType();

        if (!type.name().endsWith("_AXE")){
            return;
        }

        //Make sure the block is a log
        Material blockType = e.getBlock().getType();
        if (!isLog(blockType)){
            return;
        }

        //Set that will hold all the blocks that need to be broken. Does not allow duplicates
        HashSet<Block> blocksToBreak = new HashSet<>();
        AtomicInteger leafCount = new AtomicInteger(0);
        AtomicInteger nonTreeBlockCount = new AtomicInteger(0);
        breakAdjacentLogs(e.getBlock(), blocksToBreak, leafCount, nonTreeBlockCount);

        System.out.println(leafCount + " leaves");
        System.out.println(nonTreeBlockCount + " non-tree blocks");

        //Now, we need to make a decision on whether this is a real tree or not
        //First, there needs to be a minimum number of leaves.
        //Secondly, there cannot be too many non-tree blocks.

        if (leafCount.get() < 25 || nonTreeBlockCount.get() > 100){
            System.out.println("Not a tree");
            return;
        }

        int[][][] blocks3DArray = blocksTo3DArray(blocksToBreak);

        //Break all the blocks
        for (Block block : blocksToBreak) {
            if (isLog(block.getType())){
                block.breakNaturally();
            }
        }

        //Print the 3D array
        for (int[][] ints : blocks3DArray) {
            for (int[] anInt : ints) {
                for (int i : anInt) {
                    System.out.print(i);
                }
                System.out.println();
            }
            System.out.println();
        }

    }

    //Blocks to 3D array. We are going to feature extract the blocks to a 3D array.
    // so that we can use ML. We will use the 3D array to train a model to recognize
    // trees. We will then use the model to predict whether a set of blocks is a tree or not.
    public int[][][] blocksTo3DArray(Set<Block> blocks){
        //Empty spaces are represented by a 0.
        //Logs are represented by a 1.
        //Leaves are represented by a 2.

        //Determine the size of the "bounding box" of the tree
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (Block block : blocks) {
            if (block.getX() < minX){
                minX = block.getX();
            }
            if (block.getY() < minY){
                minY = block.getY();
            }
            if (block.getZ() < minZ){
                minZ = block.getZ();
            }
            if (block.getX() > maxX){
                maxX = block.getX();
            }
            if (block.getY() > maxY){
                maxY = block.getY();
            }
            if (block.getZ() > maxZ){
                maxZ = block.getZ();
            }
        }

        //Now that we know the size of the bounding box, we can create the 3D array
        int[][][] blocks3DArray = new int[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];

        //Now we need to fill the 3D array with the blocks
        for (Block block : blocks) {
            blocks3DArray[block.getX() - minX][block.getY() - minY][block.getZ() - minZ] = isLog(block.getType()) ? 1 : 2;
        }

        return blocks3DArray;
    }

    private void breakAdjacentLogs(Block block, Set<Block> blocksToBreak, AtomicInteger leafCount, AtomicInteger nonTreeBlockCount){
        if (!isLog(block.getType())){

            if (isLeaf(block.getType())){
                blocksToBreak.add(block);
                leafCount.incrementAndGet();
            } else if(!block.getType().isAir()) {
                nonTreeBlockCount.incrementAndGet();
            }

            return;
        }

        //See if this is a block we have already checked
        if (blocksToBreak.contains(block)){
            return;
        }

        blocksToBreak.add(block);

        for (Block adjacentBlock : getAdjacentBlocks(block)) {
            breakAdjacentLogs(adjacentBlock, blocksToBreak, leafCount, nonTreeBlockCount);
        }
    }


    private boolean isLog(Material material) {
        return material.name().endsWith("_LOG");
    }

    private boolean isLeaf(Material material) {
        return material.name().endsWith("_LEAVES");
    }

    //Return all blocks in a 1 block radius around the given block,
    //even if they are not directly adjacent such as diagonally
    private Block[] getAdjacentBlocks(Block block) {
        return new Block[] {
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
