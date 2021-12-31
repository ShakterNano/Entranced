package io.github.shaksternano.entranced.mixin.commonloader.client.debug;

import dev.architectury.networking.NetworkManager;
import io.github.shaksternano.entranced.commonside.Entranced;
import io.github.shaksternano.entranced.commonside.network.debug.DebugNetworking;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(value = MinecraftClient.class, priority = 2000)
abstract class MinecraftClientMixin {

    // For debugging.
    @Redirect(method = "handleInputEvents", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", opcode = Opcodes.PUTFIELD), require = 0)
    private void debugModeKeypress(PlayerInventory getInventory, int slot) {
        if (Entranced.getConfig().isDebugMode()) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(slot);
            NetworkManager.sendToServer(DebugNetworking.DEBUG_HOTBAR_SLOT, buf);
        } else {
            getInventory.selectedSlot = slot;
        }
    }
}
