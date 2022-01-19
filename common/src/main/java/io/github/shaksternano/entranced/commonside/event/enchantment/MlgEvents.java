package io.github.shaksternano.entranced.commonside.event.enchantment;

import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import io.github.shaksternano.entranced.commonside.network.enchantment.MlgNetworking;
import io.github.shaksternano.entranced.commonside.registry.EntrancedEnchantments;
import io.github.shaksternano.entranced.commonside.util.EnchantmentUtil;
import io.github.shaksternano.entranced.mixin.commonloader.commonside.accessor.AbstractBlockAccessor;
import io.github.shaksternano.entranced.mixin.commonloader.commonside.accessor.BucketItemAccessor;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MlgEvents {

    private MlgEvents() {}

    public static void registerServerEvents() {
        // A filled bucket with the MLG enchantment in the player's inventory will automatically get put in the player's hand when falling from a height that would damage the player.
        TickEvent.PLAYER_POST.register(player -> {
            if (EntrancedEnchantments.MLG.isEnabled()) {
                if (!player.world.isClient) {
                    if (!player.isCreative() && !player.isSpectator()) {
                        if (!player.isOnGround()) {
                            boolean safeFall = false;

                            // Check for a fall that would damage the player.
                            for (int fallDistance = 0; fallDistance <= player.getSafeFallDistance(); fallDistance++) {
                                Block block = player.world.getBlockState(player.getBlockPos().down(fallDistance)).getBlock();
                                if (((AbstractBlockAccessor) block).entranced$isCollidable()) {
                                    safeFall = true;
                                    break;
                                }
                            }

                            if (!safeFall) {
                                PlayerInventory inventory = player.getInventory();

                                // Locate a filled bucket with the MLG enchantment in the player's inventory.
                                int inventoryIndex = -1;
                                for (int newIndex = 0; newIndex < inventory.main.size(); newIndex++) {
                                    ItemStack stack = inventory.main.get(newIndex);
                                    Item item = stack.getItem();
                                    if (!stack.isEmpty()) {
                                        if (item instanceof FluidModificationItem) {
                                            if (!(item instanceof BucketItem && !(((BucketItemAccessor) item).entranced$getFluid() instanceof FlowableFluid))) {
                                                if (EnchantmentUtil.hasEnchantment(stack, EntrancedEnchantments.MLG)) {
                                                    inventoryIndex = newIndex;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                // Put the filled bucket with the MLG enchantment in the player's hand.
                                if (inventoryIndex != -1) {
                                    ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

                                    if (PlayerInventory.isValidHotbarIndex(inventoryIndex)) {
                                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                                        buf.writeInt(inventoryIndex);

                                        NetworkManager.sendToPlayer(serverPlayer, MlgNetworking.CLIENT_HOTBAR_SWAP, buf);
                                    } else {
                                        serverPlayer.getInventory().swapSlotWithHotbar(inventoryIndex);

                                        serverPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, serverPlayer.getInventory().selectedSlot, serverPlayer.getInventory().getStack(serverPlayer.getInventory().selectedSlot)));
                                        serverPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, inventoryIndex, serverPlayer.getInventory().getStack(inventoryIndex)));
                                        serverPlayer.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(serverPlayer.getInventory().selectedSlot));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
