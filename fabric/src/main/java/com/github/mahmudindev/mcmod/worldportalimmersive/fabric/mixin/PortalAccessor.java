package com.github.mahmudindev.mcmod.worldportalimmersive.fabric.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import qouteall.imm_ptl.core.portal.Portal;

@Mixin(Portal.class)
public interface PortalAccessor {
    @Accessor
    ResourceKey<Level> getDimensionTo();
}
