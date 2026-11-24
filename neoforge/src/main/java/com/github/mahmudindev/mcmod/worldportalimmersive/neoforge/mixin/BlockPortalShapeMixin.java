package com.github.mahmudindev.mcmod.worldportalimmersive.neoforge.mixin;

import com.github.mahmudindev.mcmod.worldportalimmersive.base.IBlockPortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import qouteall.imm_ptl.core.portal.nether_portal.BlockPortalShape;

@Mixin(BlockPortalShape.class)
public abstract class BlockPortalShapeMixin implements IBlockPortalShape {
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
