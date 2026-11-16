package com.github.mahmudindev.mcmod.worldportalimmersive.forge.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.mixin.BaseFireBlockAccessor;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportalimmersive.WorldPortalImmersive;
import com.github.mahmudindev.mcmod.worldportalimmersive.base.IBlockPortalShape;
import com.github.mahmudindev.mcmod.worldportalimmersive.base.IBlockPos;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.platform_specific.IPRegistry;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.custom_portal_gen.CustomPortalGeneration;
import qouteall.imm_ptl.core.portal.custom_portal_gen.form.NetherPortalLikeForm;
import qouteall.imm_ptl.core.portal.nether_portal.BlockPortalShape;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(NetherPortalLikeForm.class)
public class NetherPortalLikeFormMixin {
    @Inject(
            method = "perform",
            at = @At(
                    value = "INVOKE",
                    target = "Lqouteall/imm_ptl/core/portal/custom_portal_gen/CustomPortalGeneration;mapPosition(Lnet/minecraft/core/BlockPos;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/core/BlockPos;"
            ),
            cancellable = true,
            remap = false
    )
    private void performSupport(
            CustomPortalGeneration cpg,
            ServerLevel fromWorld,
            BlockPos startingPos,
            ServerLevel toWorld,
            Entity triggeringEntity,
            CallbackInfoReturnable<Boolean> cir,
            @Local(name = "fromWorld") ServerLevel fromWorldX,
            @Local(name = "toWorld") LocalRef<ServerLevel> toWorldRef,
            @Local(name = "fromShape") BlockPortalShape fromShape,
            @Share(
                    value = "isReturnPortal",
                    namespace = WorldPortalImmersive.MOD_ID
            ) LocalBooleanRef isReturnPortalRef
    ) {
        if (!((IBlockPos) startingPos).worldportalimmersive$isPortalSupported()) {
            return;
        }

        MinecraftServer server = fromWorldX.getServer();

        PortalData portalData = ((IServerLevel) fromWorldX).worldportal$getPortalData();

        BlockPos innerCenterVec = fromShape.innerAreaBox.getCenter();

        ResourceKey<Level> resourceKeyLevelC = portalData.getDimension(innerCenterVec);
        ServerLevel toWorldC = server.getLevel(resourceKeyLevelC);
        if (toWorldC != null) {
            toWorldRef.set(toWorldC);
            isReturnPortalRef.set(true);
            return;
        }

        Map<BlockPos, ResourceKey<Level>> dimensions = portalData.getDimensions();
        for (Map.Entry<BlockPos, ResourceKey<Level>> entry : dimensions.entrySet()) {
            BlockPos innerCenterVecC = entry.getKey();

            if (innerCenterVec.distSqr(innerCenterVecC) > Mth.square(128)) {
                continue;
            }

            if (!fromWorldX.getEntities(
                    IPRegistry.PORTAL.get(),
                    new AABB(innerCenterVecC),
                    entity -> {
                        return entity.blockPosition() == innerCenterVecC;
                    }
            ).isEmpty()) {
                continue;
            }

            resourceKeyLevelC = entry.getValue();
            toWorldC = server.getLevel(resourceKeyLevelC);
            if (toWorldC != null) {
                portalData.putDimension(innerCenterVec, resourceKeyLevelC);
                portalData.removeDimension(innerCenterVecC);

                toWorldRef.set(toWorldC);
                isReturnPortalRef.set(true);
                return;
            }
        }

        BlockPos minPos = fromShape.totalAreaBox.l;
        BlockPos maxPos = fromShape.totalAreaBox.h;
        BlockPos frameC1Pos;
        BlockPos frameC2Pos;
        BlockPos frameC3Pos;
        BlockPos frameC4Pos;
        switch (fromShape.axis) {
            case X:
                frameC1Pos = new BlockPos(minPos.getX(), minPos.getY(), minPos.getZ());
                frameC2Pos = new BlockPos(minPos.getX(), minPos.getY(), maxPos.getZ());
                frameC3Pos = new BlockPos(minPos.getX(), maxPos.getY(), minPos.getZ());
                frameC4Pos = new BlockPos(minPos.getX(), maxPos.getY(), maxPos.getZ());
                break;
            case Z:
                frameC1Pos = new BlockPos(minPos.getX(), minPos.getY(), minPos.getZ());
                frameC2Pos = new BlockPos(maxPos.getX(), minPos.getY(), minPos.getZ());
                frameC3Pos = new BlockPos(minPos.getX(), maxPos.getY(), minPos.getZ());
                frameC4Pos = new BlockPos(maxPos.getX(), maxPos.getY(), minPos.getZ());
                break;
            case Y:
                frameC1Pos = new BlockPos(minPos.getX(), minPos.getY(), minPos.getZ());
                frameC2Pos = new BlockPos(maxPos.getX(), minPos.getY(), minPos.getZ());
                frameC3Pos = new BlockPos(minPos.getX(), minPos.getY(), maxPos.getZ());
                frameC4Pos = new BlockPos(maxPos.getX(), minPos.getY(), maxPos.getZ());
                break;
            default:
                return;
        }

        ResourceLocation frameC1 = BuiltInRegistries.BLOCK.getKey(fromWorldX
                .getBlockState(frameC1Pos)
                .getBlock()
        );
        ResourceLocation frameC2 = BuiltInRegistries.BLOCK.getKey(fromWorldX
                .getBlockState(frameC2Pos)
                .getBlock()
        );
        ResourceLocation frameC3 = BuiltInRegistries.BLOCK.getKey(fromWorldX
                .getBlockState(frameC3Pos)
                .getBlock()
        );
        ResourceLocation frameC4 = BuiltInRegistries.BLOCK.getKey(fromWorldX
                .getBlockState(frameC4Pos)
                .getBlock()
        );

        Map<ResourceLocation, PortalConfig> portalConfigs = new HashMap<>();

        PortalManager.getPortalConfigs().forEach((k, v) -> {
            ResourceLocation mode = v.getModeLocation();
            if (fromShape.axis != Direction.Axis.Y) {
                if (mode != null && !mode.equals(PortalConfig.DEFAULT_MODE)) {
                    return;
                }
            } else {
                if (mode == null || !mode.equals(PortalConfig.HORIZONTAL_MODE)) {
                    return;
                }
            }

            Direction.Axis axis = fromShape.axis != Direction.Axis.Y
                    ? fromShape.axis
                    : Direction.Axis.X;

            ResourceLocation frameC1Target = v.getFrameC1(axis);
            if (frameC1Target != null && !frameC1.equals(frameC1Target)) {
                return;
            }

            ResourceLocation frameC2Target = v.getFrameC2(axis);
            if (frameC2Target != null && !frameC2.equals(frameC2Target)) {
                return;
            }

            ResourceLocation frameC3Target = v.getFrameC3(axis);
            if (frameC3Target != null && !frameC3.equals(frameC3Target)) {
                return;
            }

            ResourceLocation frameC4Target = v.getFrameC4(axis);
            if (frameC4Target != null && !frameC4.equals(frameC4Target)) {
                return;
            }

            portalConfigs.put(k, v);
        });

        ResourceKey<Level> resourceKeyLevel = fromWorldX.dimension();

        portalConfigs.keySet().removeIf(k -> {
            return resourceKeyLevel == portalConfigs.get(k).getDestinationKey();
        });

        if (!portalConfigs.isEmpty()) {
            int random = fromWorldX.getRandom().nextInt(portalConfigs.size());

            int i = 0;
            for (Map.Entry<ResourceLocation, PortalConfig> entry : portalConfigs.entrySet()) {
                if (i != random) {
                    i++;

                    continue;
                }

                ServerLevel toWorldB = server.getLevel(entry.getValue().getDestinationKey());
                if (toWorldB != null) {
                    toWorldRef.set(toWorldB);
                    return;
                }
            }
        }

        if (BaseFireBlockAccessor.invokeInPortalDimension(fromWorldX)) {
            ((IBlockPos) startingPos).worldportalimmersive$setPortalSupported(false);
            return;
        }

        cir.setReturnValue(false);
    }

    @ModifyArg(
            method = "perform",
            at = @At(
                    value = "INVOKE",
                    target = "Lqouteall/imm_ptl/core/portal/custom_portal_gen/form/NetherPortalLikeForm;getFrameMatchingFunc(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lqouteall/imm_ptl/core/portal/nether_portal/BlockPortalShape;)Ljava/util/function/Function;"
            ),
            index = 2,
            remap = false
    )
    private BlockPortalShape performGetFrameMatchingFuncPrepare(
            BlockPortalShape fromShape,
            @Local(argsOnly = true) BlockPos startingPos
    ) {
        if (((IBlockPos) startingPos).worldportalimmersive$isPortalSupported()) {
            ((IBlockPortalShape) fromShape).worldportalimmersive$setPortalSupported(true);
        }

        return fromShape;
    }

    @ModifyArg(
            method = "perform",
            at = @At(
                    value = "INVOKE",
                    target = "Lqouteall/imm_ptl/core/portal/nether_portal/NetherPortalGeneration;startGeneratingPortal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lqouteall/imm_ptl/core/portal/nether_portal/BlockPortalShape;Lnet/minecraft/core/BlockPos;ILjava/util/function/Predicate;Ljava/util/function/Consumer;Ljava/util/function/Consumer;Ljava/util/function/Supplier;Ljava/util/function/BooleanSupplier;Ljava/util/function/Function;)V"
            ),
            index = 6,
            remap = false
    )
    private Consumer<BlockPortalShape> performNewFrameGenerateFuncPrepare(
            Consumer<BlockPortalShape> newFrameGenerateFunc,
            @Local(argsOnly = true) BlockPos startingPos,
            @Share(
                    value = "toShape",
                    namespace = WorldPortalImmersive.MOD_ID
            ) LocalRef<BlockPortalShape> toShapeRef
    ) {
        if (((IBlockPos) startingPos).worldportalimmersive$isPortalSupported()) {
            return (toShape) -> {
                newFrameGenerateFunc.accept(toShape);

                toShapeRef.set(toShape);
            };
        }

        return newFrameGenerateFunc;
    }

    @Inject(
            method = "perform",
            at = @At(
                    value = "INVOKE",
                    target = "Lqouteall/imm_ptl/core/portal/nether_portal/NetherPortalGeneration;startGeneratingPortal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lqouteall/imm_ptl/core/portal/nether_portal/BlockPortalShape;Lnet/minecraft/core/BlockPos;ILjava/util/function/Predicate;Ljava/util/function/Consumer;Ljava/util/function/Consumer;Ljava/util/function/Supplier;Ljava/util/function/BooleanSupplier;Ljava/util/function/Function;)V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void performSupportStartGeneratingPortalFinish(
            CustomPortalGeneration cpg,
            ServerLevel fromWorld,
            BlockPos startingPos,
            ServerLevel toWorld,
            Entity triggeringEntity,
            CallbackInfoReturnable<Boolean> cir,
            @Local(name = "fromShape") BlockPortalShape fromShape,
            @Share(
                    value = "isReturnPortal",
                    namespace = WorldPortalImmersive.MOD_ID
            ) LocalBooleanRef isReturnPortalRef,
            @Share(
                    value = "toShape",
                    namespace = WorldPortalImmersive.MOD_ID
            ) LocalRef<BlockPortalShape> toShapeRef
    ) {
        if (!((IBlockPos) startingPos).worldportalimmersive$isPortalSupported()) {
            return;
        }

        PortalData portalDataA = ((IServerLevel) fromWorld).worldportal$getPortalData();

        BlockPos innerCenterVecA = fromShape.innerAreaBox.getCenter();

        if (isReturnPortalRef.get()) {
            portalDataA.putDimension(innerCenterVecA, toWorld.dimension());
        }

        BlockPortalShape toShape = toShapeRef.get();

        if (toShape == null) {
            return;
        }

        PortalData portalDataB = ((IServerLevel) toWorld).worldportal$getPortalData();

        BlockPos innerCenterVecB = toShape.innerAreaBox.getCenter();

        if (!isReturnPortalRef.get()) {
            portalDataB.putDimension(innerCenterVecB, fromWorld.dimension());
        }

        ((IBlockPortalShape) fromShape).worldportalimmersive$clear();
    }

    @ModifyExpressionValue(
            method = "lambda$getFrameMatchingFunc$7",
            at = @At(
                    value = "INVOKE",
                    target = "Lqouteall/imm_ptl/core/portal/nether_portal/BlockPortalShape;matchShapeWithMovedFirstFramePos(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos$MutableBlockPos;)Lqouteall/imm_ptl/core/portal/nether_portal/BlockPortalShape;"
            ),
            remap = false
    )
    private static BlockPortalShape getFrameMatchingFuncSupport(
            BlockPortalShape original,
            @Local(argsOnly = true) BlockPortalShape fromShape,
            @Local(argsOnly = true, ordinal = 0) ServerLevel fromWorld,
            @Local(argsOnly = true, ordinal = 1) ServerLevel toWorld
    ) {
        if (original == null) {
            return null;
        }

        BlockPos innerCenterVec = original.innerAreaBox.getCenter();
        List<Portal> portals = toWorld.getEntities(
                IPRegistry.PORTAL.get(),
                new AABB(innerCenterVec),
                entity -> {
                    return entity.blockPosition() == innerCenterVec;
                }
        );

        if (portals.isEmpty()) {
            return original;
        }

        Portal portal = portals.get(0);
        if (((IBlockPortalShape) fromShape).worldportalimmersive$isPortalSupported()) {
            if (portals.size() != 1) {
                return null;
            }

            if (portal.dimensionTo != fromWorld.dimension()) {
                return null;
            }
        } else {
            if (portals.size() > 1) {
                return original;
            }

            boolean fromOverworld = fromWorld.dimension() == Level.OVERWORLD;
            boolean toNether = toWorld.dimension() == Level.NETHER;
            if (fromOverworld && toNether) {
                if (portal.dimensionTo != Level.OVERWORLD) {
                    return null;
                }
            }

            boolean fromNether = fromWorld.dimension() == Level.NETHER;
            boolean toOverworld = toWorld.dimension() == Level.OVERWORLD;
            if (fromNether && toOverworld) {
                if (portal.dimensionTo != Level.NETHER) {
                    return null;
                }
            }
        }

        return original;
    }
}
