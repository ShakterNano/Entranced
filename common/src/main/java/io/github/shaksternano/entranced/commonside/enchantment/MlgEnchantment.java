package io.github.shaksternano.entranced.commonside.enchantment;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.shaksternano.entranced.commonside.Entranced;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

/**
 * A bucket that isn't empty and has this enchantment will be moved to the player's hand when the player falls
 * from a height that would damage them.
 */
public class MlgEnchantment extends ConfigurableEnchantment {

    public MlgEnchantment(EnchantmentTarget enchantmentTarget) {
        super(Entranced.getConfig().getMlgRarity(), enchantmentTarget, EquipmentSlot.values());
    }

    public MlgEnchantment() {
        this(EnchantmentTarget.VANISHABLE);
    }

    @Override
    public boolean isTreasure() {
        return Entranced.getConfig().isMlgTreasure();
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return Entranced.getConfig().isMlgSoldByVillagers();
    }

    @Override
    public boolean isEnabled() {
        return Entranced.getConfig().isMlgEnabled();
    }

    @Override
    protected int minPower() {
        return Entranced.getConfig().getMlgMinPower();
    }

    @Override
    protected int maxPowerAboveMin() {
        return Entranced.getConfig().getMlgMaxPowerAboveMin();
    }

    @Override
    public @NotNull String getPath() {
        return "mlg";
    }

    @ExpectPlatform
    public static MlgEnchantment newEnchantment() {
        throw new AssertionError();
    }
}
