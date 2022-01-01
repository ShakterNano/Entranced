package io.github.shaksternano.entranced.mixin.commonloader.commonside.enchantment.infinity.bucket;

import io.github.shaksternano.entranced.commonside.util.BucketUtil;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemDispenserBehavior.class)
abstract class ItemDispenserBehaviorMixin {

    // The ItemStack being dispensed doesn't change if it is a bucket with Infinity.
    @Inject(method = "dispense", at = @At("RETURN"), cancellable = true)
    private void dispenseInfinity(BlockPointer blockPointer, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack bucketStack = BucketUtil.infinityBucketKeepFluid(itemStack);

        if (bucketStack != null) {
            cir.setReturnValue(bucketStack);
        }
    }
}