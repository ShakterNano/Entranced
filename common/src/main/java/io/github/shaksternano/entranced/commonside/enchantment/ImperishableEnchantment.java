package io.github.shaksternano.entranced.commonside.enchantment;

import io.github.shaksternano.entranced.commonside.Entranced;
import io.github.shaksternano.entranced.commonside.config.ImperishableBlacklists;
import io.github.shaksternano.entranced.commonside.registry.EntrancedEnchantments;
import io.github.shaksternano.entranced.commonside.util.EnchantmentUtil;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

public final class ImperishableEnchantment extends ConfigurableEnchantment {

    public ImperishableEnchantment() {
        super(Entranced.getConfig().getImperishableRarity(), EnchantmentTarget.VANISHABLE, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }


    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return !ImperishableBlacklists.isItemBlacklistedGlobally(stack) && super.isAcceptableItem(stack);
    }

    @Override
    public boolean isTreasure() {
        return Entranced.getConfig().isImperishableTreasure();
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return Entranced.getConfig().isImperishableSoldByVillagers();
    }

    @Override
    public boolean isEnabled() {
        return Entranced.getConfig().isImperishableEnabled();
    }

    @Override
    protected int minPower() {
        return Entranced.getConfig().getImperishableMinPower();
    }

    @Override
    protected int maxPowerAboveMin() {
        return Entranced.getConfig().getImperishableMaxPowerAboveMin();
    }

    @Override
    public @NotNull String getId() {
        return "imperishable";
    }

    // Removes the "(Broken)" string from the name of tools with Imperishable at 0 durability, so it doesn't mess with anvil renaming.
    public static String itemNameRemoveBroken(Text textName, ItemStack stack) {
        String trimmedName = textName.getString();

        if (EnchantmentUtil.isBrokenImperishable(stack)) {
            TranslatableText broken = new TranslatableText("item.name." + EntrancedEnchantments.IMPERISHABLE.getTranslationKey() + ".broken");
            int brokenLength = broken.getString().length();
            trimmedName = trimmedName.substring(0, trimmedName.length() - brokenLength);
        }

        return trimmedName;
    }
}
