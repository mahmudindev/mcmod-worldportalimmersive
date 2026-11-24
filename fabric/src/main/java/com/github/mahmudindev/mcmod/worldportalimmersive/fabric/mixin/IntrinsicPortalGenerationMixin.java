package com.github.mahmudindev.mcmod.worldportalimmersive.fabric.mixin;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportalimmersive.base.IBlockPos;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.portal.custom_portal_gen.CustomPortalGeneration;
import qouteall.imm_ptl.peripheral.portal_generation.IntrinsicPortalGeneration;

import java.util.ArrayList;
import java.util.Map;

@Mixin(IntrinsicPortalGeneration.class)
public abstract class IntrinsicPortalGenerationMixin {
    @Unique
    private static final CustomPortalGeneration INTRINSIC = new CustomPortalGeneration(
            Lists.newArrayList(CustomPortalGeneration.ANY_DIMENSION),
            CustomPortalGeneration.THE_SAME_DIMENSION,
            1, 1,
            false,
            IntrinsicPortalGeneration.intrinsicNetherPortalForm,
            null, new ArrayList<>(), new ArrayList<>()
    );
    @Unique
    private static final CustomPortalGeneration DILIGENT = new CustomPortalGeneration(
            Lists.newArrayList(CustomPortalGeneration.ANY_DIMENSION),
            CustomPortalGeneration.THE_SAME_DIMENSION,
            1, 1,
            false,
            IntrinsicPortalGeneration.diligentNetherPortalForm,
            null, new ArrayList<>(), new ArrayList<>()
    );

    @Inject(
            method = "onFireLitOnObsidian",
            at = @At(
                    value = "INVOKE",
                    target = "Lqouteall/imm_ptl/core/portal/custom_portal_gen/CustomPortalGeneration;perform(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private static void onFireLitOnObsidianSupport(
            ServerLevel fromWorld,
            BlockPos firePos,
            Entity triggeringEntity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ((IBlockPos) firePos).worldportalimmersive$setPortalSupported(true);
    }

    @Inject(
            method = "onFireLitOnObsidian",
            at = @At(value = "CONSTANT", args = "intValue=0"),
            cancellable = true
    )
    private static void onFireLitOnObsidianOther(
            ServerLevel fromWorld,
            BlockPos firePos,
            Entity triggeringEntity,
            CallbackInfoReturnable<Boolean> cir,
            @Local(name = "mode") IPGlobal.NetherPortalMode mode
    ) {
        switch (mode) {
            case normal, adaptive:
                break;
            default:
                return;
        }

        CustomPortalGeneration gen = mode == IPGlobal.NetherPortalMode.normal
                ? INTRINSIC
                : DILIGENT;
        ((IBlockPos) firePos).worldportalimmersive$setPortalSupported(true);
        cir.setReturnValue(gen.perform(fromWorld, firePos, triggeringEntity));
    }

    @Inject(method = "onFireLitOnObsidian", at = @At("TAIL"))
    private static void onFireLitOnObsidianFinish(
            ServerLevel fromWorld,
            BlockPos firePos,
            Entity triggeringEntity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        ((IBlockPos) firePos).worldportalimmersive$clear();
    }

    @ModifyVariable(
            method = "onCrouchingPlayerIgnite",
            at = @At("STORE"),
            name = "dimensionCorrect"
    )
    private static boolean onCrouchingPlayerIgniteSupport(boolean dimensionCorrect) {
        Map<ResourceLocation, PortalConfig> portalConfigs = PortalManager.getPortalConfigs();
        for (Map.Entry<ResourceLocation, PortalConfig> entry : portalConfigs.entrySet()) {
            if (dimensionCorrect) {
                break;
            }

            PortalConfig portal = entry.getValue();

            ResourceLocation mode = portal.getModeLocation();
            if (mode != null && !mode.equals(PortalConfig.DEFAULT_MODE)) {
                continue;
            }

            dimensionCorrect = true;
        }

        return dimensionCorrect;
    }
}
