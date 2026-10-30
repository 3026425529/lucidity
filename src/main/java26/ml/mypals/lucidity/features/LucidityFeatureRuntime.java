package ml.mypals.lucidity.features;

import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.TextGizmo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Runtime state shared by the 26.3 HUD and configuration screen. It uses only public
 * client APIs, so the feature layer remains compatible with Sodium and the extraction renderer. */
public final class LucidityFeatureRuntime {
    private static final List<String> HUD_LINES = new ArrayList<>();
    private static BlockPos targetBlock;
    private static String targetName = "";
    private static Entity targetEntity;
    private static int nearbyEntities;
    private static int nearbyItems;
    private static int nearbyDragons;
    private static int nearbyInvisible;
    private static long ticks;
    private static final List<BlockPos> sculkSensors = new ArrayList<>();
    private static final List<BlockPos> spawnCandidates = new ArrayList<>();
    private static final List<BlockPos> worldEaterTargets = new ArrayList<>();

    private LucidityFeatureRuntime() {}

    public static void tick(Minecraft client) {
        ticks++;
        LuciditySoundListener.tick();
        if (client.level == null || client.player == null) {
            targetBlock = null;
            targetName = "";
            targetEntity = null;
            nearbyEntities = nearbyItems = nearbyDragons = nearbyInvisible = 0;
            sculkSensors.clear();
            return;
        }

        AABB nearby = client.player.getBoundingBox().inflate(16.0);
        nearbyEntities = client.level.getEntities(client.player, nearby, entity -> entity != client.player).size();
        nearbyItems = client.level.getEntities(client.player, nearby, entity -> entity instanceof ItemEntity).size();
        nearbyInvisible = client.level.getEntities(client.player, nearby, Entity::isInvisible).size();
        nearbyDragons = client.level.getEntities(client.player,
                client.player.getBoundingBox().inflate(256.0), entity -> entity instanceof EnderDragon).size();
        if (ticks % 10 == 0) refreshSculkSensors(client);
        if (ticks % 10 == 0) refreshSpawnCandidates(client);
        if (ticks % 5 == 0) refreshWorldEaterTargets(client);

        targetEntity = null;
        if (client.hitResult instanceof BlockHitResult blockHit && client.hitResult.getType() != HitResult.Type.MISS) {
            targetBlock = blockHit.getBlockPos();
            targetName = client.level.getBlockState(targetBlock).getBlock().getName().getString();
        } else {
            targetBlock = null;
            targetName = "";
            if (client.hitResult instanceof EntityHitResult entityHit && client.hitResult.getType() != HitResult.Type.MISS) {
                targetEntity = entityHit.getEntity();
            }
        }
    }

    private static void refreshSculkSensors(Minecraft client) {
        sculkSensors.clear();
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.SCULK_SENSOR_VISUALIZE)
                && !LucidityConfig.isEnabled(LucidityConfig.Feature.VIBRATION_TRACE)) return;
        BlockPos center = client.player.blockPosition();
        int range = 18;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -range, -range), center.offset(range, range, range))) {
            var block = client.level.getBlockState(pos).getBlock();
            if (block == Blocks.SCULK_SENSOR || block == Blocks.CALIBRATED_SCULK_SENSOR || block == Blocks.SCULK_SHRIEKER) {
                sculkSensors.add(pos.immutable());
            }
        }
    }

    private static void refreshSpawnCandidates(Minecraft client) {
        spawnCandidates.clear();
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.MOB_SPAWN_VISUALIZE)) return;
        BlockPos center = client.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-16, -4, -16), center.offset(16, 4, 16))) {
            if (client.level.getBlockState(pos).isAir() && client.level.getMaxLocalRawBrightness(pos) <= 7
                    && client.level.getBlockState(pos.below()).isSolidRender()) {
                spawnCandidates.add(pos.immutable());
            }
            if (spawnCandidates.size() >= 256) return;
        }
    }

    private static void refreshWorldEaterTargets(Minecraft client) {
        worldEaterTargets.clear();
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.WORLD_EATER_MINE_HELPER)) return;
        Vec3 start = client.player.getEyePosition();
        Vec3 direction = client.player.getViewVector(1.0F);
        for (int i = 1; i <= 32; i++) {
            BlockPos pos = BlockPos.containing(start.add(direction.scale(i)));
            BlockState state = client.level.getBlockState(pos);
            if (!state.isAir() && state.getDestroySpeed(client.level, pos) >= 0.0F) worldEaterTargets.add(pos);
        }
    }

    public static List<String> hudLines() {
        HUD_LINES.clear();
        int enabled = LucidityConfig.enabledCount();
        HUD_LINES.add("Lucidity 2.0.2 • 26.3  |  " + enabled + "/" + LucidityConfig.Feature.values().length + " features");
        if (targetBlock != null) HUD_LINES.add("Target: " + targetName + " " + targetBlock.getX() + " " + targetBlock.getY() + " " + targetBlock.getZ());
        if (targetEntity != null) HUD_LINES.add("Entity: " + targetEntity.getName().getString());

        List<String> active = new ArrayList<>();
        for (LucidityConfig.Feature feature : LucidityConfig.Feature.values()) {
            if (LucidityConfig.isEnabled(feature)) active.add(feature.label() + ": " + featureStatus(feature));
        }
        if (!active.isEmpty()) {
            int start = (int) ((ticks / 80) % active.size());
            int count = Math.min(6, active.size());
            for (int offset = 0; offset < count; offset++) HUD_LINES.add(active.get((start + offset) % active.size()));
        } else if (ticks % 40 < 20) {
            HUD_LINES.add("Press V to configure features");
        }
        return Collections.unmodifiableList(new ArrayList<>(HUD_LINES));
    }

    private static String featureStatus(LucidityConfig.Feature feature) {
        return switch (feature) {
            case SCULK_SENSOR_VISUALIZE, VIBRATION_TRACE -> "range 18 blocks";
            case BOAT_VIEW_RESTRICTION -> "camera restriction enabled";
            case BODY_YAW -> playerRotation();
            case FLUID_TRANSPARENCY_OVERRIDE -> "transparency override enabled";
            case CREAKING_HEART_INDICATOR -> "heart indicator enabled";
            case ITEM_MERG_RANGE_VISUALIZE -> nearbyItems + " item entities within 16 blocks";
            case ENDER_DRAGON_WAYPOINTS_VISUALIZE, ENDER_DRAGON_DESTRUCTION_VISUALIZE -> nearbyDragons + " dragons within 256 blocks";
            case WITHER_DESTRUCTION_VISUALIZE -> "destruction range monitor enabled";
            case NETHER_COORDINATE_CACULATOR -> netherCoordinates();
            case WORLD_EATER_MINE_HELPER -> "target rules active";
            case VAULT_ITEM_DISPLAY -> targetName.isEmpty() ? "look at a vault or trial spawner" : targetName;
            case B36_TARGET_PREVIEW -> targetName.isEmpty() ? "look at a target block" : targetName;
            case FLUID_SOURCE_HIGHLIGHT -> "target fluid source when looking at a block";
            case ACCURATE_ENTITY_SHADOW -> "shadow correction enabled";
            case CONTAINER_SIGNAL_PREVIEW -> targetName.isEmpty() ? "look at a container" : targetName;
            case EXTENDED_HITBOX -> targetEntity == null ? "look at an entity" : targetEntity.getName().getString();
            case BLOCK_NO_RANDOM_OFFSET -> "deterministic block placement enabled";
            case ZALGO_TEXT_DEOBF -> "text cleanup enabled";
            case FALLING_BLOCK_PREVIEW -> targetBlock == null ? "look at a block" : "preview ready";
            case SOUND_VISUALIZE -> "sound events tracked on client ticks";
            case BLOCK_EVENT_VISUALIZE -> targetBlock == null ? "look at a block" : "target event monitor ready";
            case MOB_SPAWN_VISUALIZE -> nearbyEntities + " nearby entities";
            case INVISIBLE_ENTITY_OVERRIDE -> nearbyInvisible + " invisible entities within 16 blocks";
            case MICROTIMING_MARKER_VISUALIZER -> "client tick " + ticks;
            case EXPLOSION_VISUALIZE -> "explosive entities and beds/anchors monitored";
            case IMAGE_RENDERING -> LucidityImageRenderer.loadedCount() + " image textures loaded";
            case SELECTIVE_RENDERING -> "gizmo selection overlay enabled";
        };
    }

    /** Emits lightweight outlines using the 26.3 gizmo API. */
    public static void emitGizmos() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        LuciditySoundListener.emitGizmos();
        if (targetBlock != null) {
            for (LucidityConfig.Feature feature : LucidityConfig.Feature.values()) {
                if (!LucidityConfig.isEnabled(feature)) continue;
                int color = switch (feature) {
                    case FLUID_SOURCE_HIGHLIGHT, FLUID_TRANSPARENCY_OVERRIDE -> 0xFF3399FF;
                    case SCULK_SENSOR_VISUALIZE, VIBRATION_TRACE, MICROTIMING_MARKER_VISUALIZER -> 0xFFAA44FF;
                    case WORLD_EATER_MINE_HELPER, FALLING_BLOCK_PREVIEW -> 0xFFFFAA22;
                    case VAULT_ITEM_DISPLAY, CONTAINER_SIGNAL_PREVIEW -> 0xFFFFDD33;
                    default -> 0xFF55FF88;
                };
                Gizmos.cuboid(targetBlock, GizmoStyle.strokeAndFill(color, 1.5F, (color & 0x00FFFFFF) | 0x30000000)).setAlwaysOnTop();
            }
            if (LucidityConfig.isEnabled(LucidityConfig.Feature.SELECTIVE_RENDERING)) {
                int chunkX = Math.floorDiv(targetBlock.getX(), 16) * 16;
                int chunkZ = Math.floorDiv(targetBlock.getZ(), 16) * 16;
                Gizmos.cuboid(new AABB(chunkX, targetBlock.getY() - 16, chunkZ,
                        chunkX + 16, targetBlock.getY() + 16, chunkZ + 16),
                        GizmoStyle.stroke(0xFF55AAFF, 2.0F)).setAlwaysOnTop();
                Gizmos.billboardTextOverBlock("Selective region", targetBlock, 0xFF55AAFF, 0, 1.0F).setAlwaysOnTop();
            }
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.SCULK_SENSOR_VISUALIZE)
                || LucidityConfig.isEnabled(LucidityConfig.Feature.VIBRATION_TRACE)) {
            for (BlockPos pos : sculkSensors) {
                Gizmos.cuboid(pos, GizmoStyle.stroke(0xFFAA44FF, 1.5F)).setAlwaysOnTop();
                Gizmos.circle(net.minecraft.world.phys.Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0), 18.0F, GizmoStyle.stroke(0x66AA44FF, 1.0F)).setAlwaysOnTop();
            }
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.ENDER_DRAGON_WAYPOINTS_VISUALIZE)
                && client.level.dimension().toString().contains("end")) {
            for (int i = 0; i < 24; i++) {
                double radius = i < 12 ? 60.0 : i < 20 ? 40.0 : 20.0;
                double angle = -Math.PI + (i < 12 ? 2.0 * Math.PI / 12.0 * i : i < 20 ? Math.PI / 4.0 * (i - 12) : Math.PI / 2.0 * (i - 20));
                int x = (int) Math.floor(radius * Math.cos(angle));
                int z = (int) Math.floor(radius * Math.sin(angle));
                int y = Math.max(73, client.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY() + (i < 12 ? 5 : i < 20 ? 15 : 5));
                BlockPos node = new BlockPos(x, y, z);
                Gizmos.cuboid(node, GizmoStyle.stroke(i < 12 ? 0xFFFFAA33 : 0xFF33AAFF, 1.5F)).setAlwaysOnTop();
            }
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.EXTENDED_HITBOX) && targetEntity != null) {
            Gizmos.cuboid(targetEntity.getBoundingBox(), GizmoStyle.stroke(0xFFFF3333, 2.0F)).setAlwaysOnTop();
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.ITEM_MERG_RANGE_VISUALIZE)) {
            client.level.getEntities(client.player, client.player.getBoundingBox().inflate(16.0), entity -> entity instanceof ItemEntity)
                    .forEach(entity -> Gizmos.cuboid(entity.getBoundingBox(), GizmoStyle.stroke(0xFFFFFF33, 1.0F)).setAlwaysOnTop());
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.ENDER_DRAGON_WAYPOINTS_VISUALIZE)
                || LucidityConfig.isEnabled(LucidityConfig.Feature.ENDER_DRAGON_DESTRUCTION_VISUALIZE)) {
            client.level.getEntities(client.player, client.player.getBoundingBox().inflate(256.0), entity -> entity instanceof EnderDragon)
                    .forEach(entity -> {
                        Gizmos.cuboid(entity.getBoundingBox(), GizmoStyle.stroke(0xFFFF55FF, 2.0F)).setAlwaysOnTop();
                        Gizmos.billboardTextOverMob(entity, 0xFFFFFFFF, "Ender Dragon", 0, 1.0F).setAlwaysOnTop();
                    });
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.WITHER_DESTRUCTION_VISUALIZE)) {
            client.level.getEntities(client.player, client.player.getBoundingBox().inflate(64.0), entity -> entity instanceof WitherBoss)
                    .forEach(entity -> Gizmos.cuboid(entity.getBoundingBox().inflate(3.0), GizmoStyle.stroke(0xFFFF4444, 2.0F)).setAlwaysOnTop());
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.EXPLOSION_VISUALIZE)) {
            client.level.getEntities(client.player, client.player.getBoundingBox().inflate(16.0), entity ->
                            entity instanceof net.minecraft.world.entity.item.PrimedTnt
                                    || entity instanceof net.minecraft.world.entity.vehicle.minecart.MinecartTNT
                                    || entity instanceof net.minecraft.world.entity.boss.enderdragon.EndCrystal
                                    || entity instanceof WitherBoss)
                    .forEach(entity -> {
                        double radius = entity instanceof WitherBoss ? 7.0 : entity instanceof net.minecraft.world.entity.boss.enderdragon.EndCrystal ? 6.0 : 4.0;
                        Gizmos.cuboid(entity.getBoundingBox().inflate(radius), GizmoStyle.stroke(0xFFFF6622, 1.5F)).setAlwaysOnTop();
                        Gizmos.billboardTextOverMob(entity, 0xFFFFAA44, "Explosion r=" + radius, 0, 1.0F).setAlwaysOnTop();
                    });
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.MOB_SPAWN_VISUALIZE)) {
            Gizmos.cuboid(client.player.getBoundingBox().inflate(8.0, 4.0, 8.0), GizmoStyle.stroke(0xFF66FF66, 1.0F)).setAlwaysOnTop();
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.BODY_YAW)) {
            var start = client.player.position().add(0.0, 0.2, 0.0);
            var end = start.add(-Math.sin(Math.toRadians(client.player.getYRot())) * 1.5, 0.0, Math.cos(Math.toRadians(client.player.getYRot())) * 1.5);
            Gizmos.arrow(start, end, 0xFF55FFFF, 2.0F).setAlwaysOnTop();
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.MICROTIMING_MARKER_VISUALIZER)) {
            Gizmos.billboardText("tick " + ticks, client.player.position().add(0.0, 2.6, 0.0), net.minecraft.gizmos.TextGizmo.Style.whiteAndCentered()).setAlwaysOnTop();
        }
        if (targetBlock != null && LucidityConfig.isEnabled(LucidityConfig.Feature.FLUID_SOURCE_HIGHLIGHT)
                && client.level.getFluidState(targetBlock).is(FluidTags.WATER) || targetBlock != null && LucidityConfig.isEnabled(LucidityConfig.Feature.FLUID_SOURCE_HIGHLIGHT)
                && client.level.getFluidState(targetBlock).is(FluidTags.LAVA)) {
            Gizmos.cuboid(targetBlock, GizmoStyle.stroke(0xFF3399FF, 2.0F)).setAlwaysOnTop();
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.INVISIBLE_ENTITY_OVERRIDE)) {
            client.level.getEntities(client.player, client.player.getBoundingBox().inflate(16.0), Entity::isInvisible)
                    .forEach(entity -> Gizmos.cuboid(entity.getBoundingBox(), GizmoStyle.stroke(0xFF66CCFF, 1.5F)).setAlwaysOnTop());
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.ACCURATE_ENTITY_SHADOW)) {
            client.level.getEntities(client.player, client.player.getBoundingBox().inflate(24.0), entity -> entity != client.player)
                    .forEach(entity -> {
                        AABB box = entity.getBoundingBox();
                        AABB shadow = new AABB(box.minX, box.minY + 0.015, box.minZ, box.maxX, box.minY + 0.035, box.maxZ);
                        Gizmos.cuboid(shadow, GizmoStyle.fill(0x55000000)).setAlwaysOnTop();
                    });
        }

        emitCreakingHearts(client);
        emitVaultAndSpawnerInfo(client);
        emitContainerSignal(client);
        emitFallingBlockPreview(client);
        emitPistonTarget(client);
        emitSpawnCandidates();
        emitWorldEaterTargets();
        emitBoatRestriction(client);
        emitFluidTransparency(client);
        emitBlockEventInfo(client);
    }

    private static void emitCreakingHearts(Minecraft client) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.CREAKING_HEART_INDICATOR)) return;
        BlockPos center = client.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-24, -12, -24), center.offset(24, 12, 24))) {
            if (client.level.getBlockState(pos).is(Blocks.CREAKING_HEART)) {
                Gizmos.cuboid(pos, GizmoStyle.strokeAndFill(0xFFFF6688, 2.0F, 0x33550044)).setAlwaysOnTop();
                Gizmos.circle(Vec3.atCenterOf(pos), 32.0F, GizmoStyle.stroke(0x55FF6688, 1.0F)).setAlwaysOnTop();
                Gizmos.billboardTextOverBlock("Creaking heart", pos, 0xFFFFAACC, 0, 0.8F).setAlwaysOnTop();
            }
        }
    }

    private static void emitVaultAndSpawnerInfo(Minecraft client) {
        if (targetBlock == null) return;
        BlockState state = client.level.getBlockState(targetBlock);
        BlockEntity entity = client.level.getBlockEntity(targetBlock);
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.VAULT_ITEM_DISPLAY) && state.is(Blocks.VAULT)) {
            String item = "vault item unavailable";
            if (entity instanceof VaultBlockEntity vault && vault.getSharedData().hasDisplayItem()) {
                item = vault.getSharedData().getDisplayItem().getHoverName().getString();
            }
            Gizmos.billboardTextOverBlock(item, targetBlock, 0xFFFFDD33, 1, 0.9F).setAlwaysOnTop();
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.VAULT_ITEM_DISPLAY) && state.is(Blocks.TRIAL_SPAWNER)) {
            String text = entity instanceof TrialSpawnerBlockEntity spawner ? "trial: " + spawner.getState().toString() : "trial spawner";
            Gizmos.billboardTextOverBlock(text, targetBlock, 0xFFFFDD33, 1, 0.8F).setAlwaysOnTop();
        }
    }

    private static void emitContainerSignal(Minecraft client) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.CONTAINER_SIGNAL_PREVIEW) || targetBlock == null) return;
        BlockState state = client.level.getBlockState(targetBlock);
        int signal = Math.max(state.getSignal(client.level, targetBlock, Direction.UP),
                state.getAnalogOutputSignal(client.level, targetBlock, Direction.UP));
        if (signal > 0 || !state.isAir()) {
            Gizmos.billboardTextOverBlock("signal " + signal, targetBlock, 0xFFFF6633, 2, 0.8F).setAlwaysOnTop();
            Gizmos.cuboid(targetBlock, GizmoStyle.stroke(signal > 0 ? 0xFFFF3322 : 0xFF777777, 1.2F)).setAlwaysOnTop();
        }
    }

    private static void emitFallingBlockPreview(Minecraft client) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.FALLING_BLOCK_PREVIEW) || targetBlock == null) return;
        BlockState state = client.level.getBlockState(targetBlock);
        if (!(state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.RED_SAND || state.getBlock() == Blocks.GRAVEL
                || state.getBlock() == Blocks.SUSPICIOUS_SAND || state.getBlock() == Blocks.SUSPICIOUS_GRAVEL)) return;
        BlockPos landing = targetBlock;
        while (landing.getY() > -63 && client.level.getBlockState(landing.below()).isAir()) landing = landing.below();
        Gizmos.cuboid(landing, GizmoStyle.strokeAndFill(0xFFFFAA22, 2.0F, 0x33442200)).setAlwaysOnTop();
        Gizmos.line(Vec3.atCenterOf(targetBlock), Vec3.atCenterOf(landing), 0xFFFFAA22, 1.5F).setAlwaysOnTop();
    }

    private static void emitPistonTarget(Minecraft client) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.B36_TARGET_PREVIEW) || targetBlock == null) return;
        BlockState state = client.level.getBlockState(targetBlock);
        if (state.getBlock() != Blocks.PISTON && state.getBlock() != Blocks.STICKY_PISTON && state.getBlock() != Blocks.PISTON_HEAD
                && state.getBlock() != Blocks.MOVING_PISTON) return;
        Direction facing = state.hasProperty(PistonBaseBlock.FACING) ? state.getValue(PistonBaseBlock.FACING) : Direction.NORTH;
        BlockPos head = targetBlock.relative(facing);
        Gizmos.cuboid(head, GizmoStyle.strokeAndFill(0xFF33AAFF, 2.0F, 0x33224466)).setAlwaysOnTop();
        Gizmos.arrow(Vec3.atCenterOf(targetBlock), Vec3.atCenterOf(head), 0xFF33AAFF, 2.0F).setAlwaysOnTop();
    }

    private static void emitSpawnCandidates() {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.MOB_SPAWN_VISUALIZE)) return;
        for (BlockPos pos : spawnCandidates) Gizmos.cuboid(pos, GizmoStyle.stroke(0xFF66FF66, 1.0F)).setAlwaysOnTop();
    }

    private static void emitWorldEaterTargets() {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.WORLD_EATER_MINE_HELPER)) return;
        for (BlockPos pos : worldEaterTargets) Gizmos.cuboid(pos, GizmoStyle.stroke(0xFFFFAA22, 1.5F)).setAlwaysOnTop();
        if (!worldEaterTargets.isEmpty()) Gizmos.billboardTextOverBlock("mine path " + worldEaterTargets.size(), worldEaterTargets.get(0), 0xFFFFAA22, 0, 0.8F).setAlwaysOnTop();
    }

    private static void emitBoatRestriction(Minecraft client) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.BOAT_VIEW_RESTRICTION) || !(client.player.getVehicle() instanceof AbstractBoat boat)) return;
        Vec3 center = boat.position().add(0.0, 0.8, 0.0);
        float yaw = boat.getYRot();
        Vec3 last = center;
        for (int i = 0; i <= 24; i++) {
            double angle = Math.toRadians(yaw - 105.0 + i * 210.0 / 24.0);
            Vec3 next = center.add(-Math.sin(angle) * 2.0, 0.0, Math.cos(angle) * 2.0);
            Gizmos.line(last, next, 0xFFFFFFFF, 1.5F).setAlwaysOnTop();
            last = next;
        }
        Gizmos.billboardText("boat view arc", center.add(0, 0.5, 0), TextGizmo.Style.whiteAndCentered()).setAlwaysOnTop();
    }

    private static void emitFluidTransparency(Minecraft client) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.FLUID_TRANSPARENCY_OVERRIDE) || targetBlock == null) return;
        if (!client.level.getFluidState(targetBlock).isEmpty()) {
            Gizmos.cuboid(targetBlock, GizmoStyle.strokeAndFill(0xFF3399FF, 1.0F, 0x443399FF)).setAlwaysOnTop();
            Gizmos.billboardTextOverBlock("fluid 50%", targetBlock, 0xFF99CCFF, 0, 0.8F).setAlwaysOnTop();
        }
    }

    private static void emitBlockEventInfo(Minecraft client) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.BLOCK_EVENT_VISUALIZE) || targetBlock == null) return;
        BlockState state = client.level.getBlockState(targetBlock);
        Gizmos.billboardTextOverBlock(state.getBlock().getName().getString() + " event", targetBlock, 0xFFFF55AA, 3, 0.75F).setAlwaysOnTop();
    }
    private static String playerRotation() {
        Minecraft client = Minecraft.getInstance();
        return client.player == null ? "unavailable" : String.format("yaw %.1f°", client.player.getYRot());
    }

    public static String netherCoordinates() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return "unavailable";
        int x = client.player.blockPosition().getX();
        int z = client.player.blockPosition().getZ();
        String dimension = client.level.dimension().toString();
        if (dimension.contains("nether")) return (x * 8) + ", " + (z * 8);
        return Math.floorDiv(x, 8) + ", " + Math.floorDiv(z, 8);
    }

    public static long ticks() { return ticks; }

    /** Returns whether a terrain block belongs to the currently selected 16x16x32 region. */
    public static boolean isSelectivePosition(BlockPos pos) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.SELECTIVE_RENDERING) || targetBlock == null) return false;
        int chunkX = Math.floorDiv(targetBlock.getX(), 16) * 16;
        int chunkZ = Math.floorDiv(targetBlock.getZ(), 16) * 16;
        return pos.getX() >= chunkX && pos.getX() < chunkX + 16
                && pos.getZ() >= chunkZ && pos.getZ() < chunkZ + 16
                && pos.getY() >= targetBlock.getY() - 16 && pos.getY() <= targetBlock.getY() + 16;
    }
}





