package com.github.mahmudindev.mcmod.worldportalimmersive.mixin;

import com.github.mahmudindev.mcmod.worldportalimmersive.base.IBlockPos;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin implements IBlockPos {
    @Unique
    private Boolean isPortalSupported;

    @Override
    public boolean worldportalimmersive$isPortalSupported() {
        if (isPortalSupported != null) {
            return this.isPortalSupported;
        }

        return false;
    }

    @Override
    public void worldportalimmersive$setPortalSupported(boolean portalSupported) {
        this.isPortalSupported = portalSupported;
    }

    @Override
    public void worldportalimmersive$clear() {
        this.isPortalSupported = null;
    }
}
