package io.github.shaksternano.entranced.commonside.config;

import io.github.shaksternano.entranced.commonside.Entranced;
import io.github.shaksternano.entranced.commonside.util.CollectionUtil;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;

import java.util.HashSet;
import java.util.Set;

public final class InfinityBucketWhitelists {

    private InfinityBucketWhitelists() {}

    private static final Set<Fluid> fluidWhitelist = new HashSet<>();
    private static final Set<Block> blockWhitelist = new HashSet<>();
    private static final Set<Item> itemWhitelist = new HashSet<>();

    // Produces Item whitelists from the Item ID string whitelists in the config
    public static void initWhitelists() {
        fluidWhitelist.clear();
        blockWhitelist.clear();
        itemWhitelist.clear();

        for (String fluidId : Entranced.getConfig().getInfinityFluidWhitelist()) {
            CollectionUtil.addFluidToSet(fluidId, fluidWhitelist);
        }

        for (String blockId : Entranced.getConfig().getInfinityBlockWhitelist()) {
            CollectionUtil.addBlockToSet(blockId, blockWhitelist);
        }

        for (String itemId : Entranced.getConfig().getInfinityItemWhitelist()) {
            CollectionUtil.addItemToSet(itemId, itemWhitelist);
        }
    }

    /*
    Returns true if a fluid is whitelisted. Otherwise, returns false.
    The water check is to allow water to be set as whitelisted by default.
    Putting water in the config list by default causes a new "minecraft:water"
    entry to be added to the config whitelist everytime the game is launched.
     */
    public static boolean isFluidWhitelisted(Fluid fluid) {
        return (Entranced.getConfig().isInfinityAffectsWater() && fluid == Fluids.WATER) || fluidWhitelist.contains(fluid);
    }

    // Returns true if a block is whitelisted. Otherwise, returns false.
    public static boolean isBlockWhitelisted(Block block) {
        return blockWhitelist.contains(block);
    }

    // Returns true if an item is whitelisted. Otherwise, returns false.
    public static boolean isItemWhitelisted(Item item) {
        return itemWhitelist.contains(item);
    }
}