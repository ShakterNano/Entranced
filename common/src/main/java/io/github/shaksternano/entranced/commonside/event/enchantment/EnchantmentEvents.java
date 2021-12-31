package io.github.shaksternano.entranced.commonside.event.enchantment;

import dev.architectury.event.events.common.LifecycleEvent;
import io.github.shaksternano.entranced.commonside.config.EnchantmentAllowLists;
import io.github.shaksternano.entranced.mixin.commonloader.commonside.accessor.DimensionTypeAccessor;

public final class EnchantmentEvents {

    private EnchantmentEvents() {}

    public static void registerServerEvents() {
        // Initialises all enchantment blacklists and whitelists when the world loads.
        LifecycleEvent.SERVER_LEVEL_LOAD.register(world -> {
            if (world.getDimension().equals(DimensionTypeAccessor.getOverworld())) {
                EnchantmentAllowLists.initAllowLists();
            }
        });
    }
}
