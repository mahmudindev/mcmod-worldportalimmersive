package com.github.mahmudindev.mcmod.worldportalimmersive.neoforge;

import com.github.mahmudindev.mcmod.worldportalimmersive.WorldPortalImmersive;
import net.neoforged.fml.common.Mod;

@Mod(WorldPortalImmersive.MOD_ID)
public final class WorldPortalImmersiveNeoForge {
    public WorldPortalImmersiveNeoForge() {
        // Run our common setup.
        WorldPortalImmersive.init();
    }
}
