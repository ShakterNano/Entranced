package io.github.shaksternano.entranced.mixin.commonloader.commonside.enchantment.imperishable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.networking.NetworkManager;
import io.github.shaksternano.entranced.commonside.Entranced;
import io.github.shaksternano.entranced.commonside.config.ImperishableBlacklists;
import io.github.shaksternano.entranced.commonside.network.enchantment.ImperishableNetworking;
import io.github.shaksternano.entranced.commonside.registry.EntrancedEnchantments;
import io.github.shaksternano.entranced.commonside.util.EnchantmentUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();

    @Shadow public abstract boolean isDamageable();

    @Shadow public abstract int getDamage();

    @Shadow public abstract void setDamage(int damage);

    @Shadow public abstract int getMaxDamage();

    // Items don't break when they reach 0 durability.
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "damage(ILjava/util/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void imperishableDurability(int amount, Random random, @Nullable ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir, int newDamage) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ImperishableBlacklists.isItemProtected(stack, ImperishableBlacklists.ProtectionType.BREAK_PROTECTION)) {
            if (!(getItem() instanceof ElytraItem)) {
                if (isDamageable()) {
                    if (EnchantmentUtil.hasEnchantment(stack, EntrancedEnchantments.IMPERISHABLE) || Entranced.getConfig().isEnchantmentNotNeededToPreventBreaking()) {
                        if (newDamage > getMaxDamage()) {
                            setDamage(getMaxDamage());
                        } else {
                            if (player != null) {
                                if (getDamage() < getMaxDamage() && newDamage == getMaxDamage()) {
                                    int itemId = Item.getRawId(getItem());
                                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                                    buf.writeInt(itemId);
                                    NetworkManager.sendToPlayer(player, ImperishableNetworking.EQUIPMENT_BREAK_EFFECTS, buf);
                                }
                            }

                            setDamage(newDamage);
                        }

                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }

    // Tool specific drops such cobblestone do not drop when mined by a tool with Imperishable at 0 durability.
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "isSuitableFor", at = @At("HEAD"), cancellable = true)
    private void imperishableSuitableFor(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ImperishableBlacklists.isItemProtected(stack, ImperishableBlacklists.ProtectionType.BREAK_PROTECTION)) {
            if (EnchantmentUtil.isBrokenImperishable(stack)) {
                cir.setReturnValue(false);
            }
        }
    }

    // Tools with Imperishable do not have increased mining speed when at 0 durability.
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "getMiningSpeedMultiplier", at = @At("HEAD"), cancellable = true)
    private void imperishableNoDurabilitySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ImperishableBlacklists.isItemProtected(stack, ImperishableBlacklists.ProtectionType.BREAK_PROTECTION)) {
            if (EnchantmentUtil.isBrokenImperishable(stack)) {
                cir.setReturnValue(1.0F);
            }
        }
    }

    // Items with Imperishable do not give bonus attributes such as attack damage on a sword when at 0 durability.
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "getAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void imperishableAttributeModifiers(EquipmentSlot equipmentSlot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ImperishableBlacklists.isItemProtected(stack, ImperishableBlacklists.ProtectionType.BREAK_PROTECTION)) {
            if (EnchantmentUtil.isBrokenImperishable(stack)) {
                cir.setReturnValue(ImmutableMultimap.of());
            }
        }
    }

    // Item specific right click block actions are cancelled if the item has Imperishable and is at 0 durability.
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void imperishableUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ImperishableBlacklists.isItemProtected(stack, ImperishableBlacklists.ProtectionType.BREAK_PROTECTION)) {
            PlayerEntity player = context.getPlayer();
            boolean userIsCreative = false;
            if (player != null) {
                userIsCreative = player.isCreative();
            }

            if (!userIsCreative) {
                if (EnchantmentUtil.isBrokenImperishable(stack)) {
                    cir.setReturnValue(ActionResult.PASS);
                }
            }
        }
    }

    // Item specific right click entity are cancelled if the item has Imperishable and is at 0 durability.
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
    private void imperishableUseOnEntity(PlayerEntity user, LivingEntity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ImperishableBlacklists.isItemProtected(stack, ImperishableBlacklists.ProtectionType.BREAK_PROTECTION)) {
            if (!user.isCreative()) {
                if (EnchantmentUtil.isBrokenImperishable(stack)) {
                    cir.setReturnValue(ActionResult.PASS);
                }
            }
        }
    }

    // Adds "(Broken)" to the name of an item with Imperishable at 0 durability.
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void imperishableBrokenName(CallbackInfoReturnable<Text> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ImperishableBlacklists.isItemProtected(stack, ImperishableBlacklists.ProtectionType.BREAK_PROTECTION)) {
            if (EnchantmentUtil.isBrokenImperishable(stack)) {
                TranslatableText broken = new TranslatableText("item.name." + EntrancedEnchantments.IMPERISHABLE.getTranslationKey() + ".broken");
                broken.formatted(Formatting.RED);

                cir.setReturnValue(((MutableText) cir.getReturnValue()).append(broken));
            }
        }
    }
}
