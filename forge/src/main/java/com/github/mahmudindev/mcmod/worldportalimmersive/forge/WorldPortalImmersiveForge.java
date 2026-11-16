package com.github.mahmudindev.mcmod.worldportalimmersive.forge;

import com.github.mahmudindev.mcmod.worldportalimmersive.WorldPortalImmersive;
import net.minecraftforge.fml.common.Mod;

@Mod(WorldPortalImmersive.MOD_ID)
public final class WorldPortalImmersiveForge {
    public WorldPortalImmersiveForge() {
        // Run our common setup.
        WorldPortalImmersive.init();
    }
}
