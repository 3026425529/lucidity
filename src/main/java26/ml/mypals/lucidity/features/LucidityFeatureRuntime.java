package ml.mypals.lucidity.features;

import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;

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

    private LucidityFeatureRuntime() {}

    public static void tick(Minecraft client) {
        ticks++;
        if (client.level == null || client.player == null) {
            targetBlock = null;
            targetName = "";
            targetEntity = null;
            nearbyEntities = nearbyItems = nearbyDragons = nearbyInvisible = 0;
            return;
        }

        AABB nearby = client.player.getBoundingBox().inflate(16.0);
        nearbyEntities = client.level.getEntities(client.player, nearby, entity -> entity != client.player).size();
        nearbyItems = client.level.getEntities(client.player, nearby, entity -> entity instanceof ItemEntity).size();
        nearbyInvisible = client.level.getEntities(client.player, nearby, Entity::isInvisible).size();
        nearbyDragons = client.level.getEntities(client.player,
                client.player.getBoundingBox().inflate(256.0), entity -> entity instanceof EnderDragon).size();

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
        };
    }

    /** Emits lightweight outlines using the 26.3 gizmo API. */
    public static void emitGizmos() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
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
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.INVISIBLE_ENTITY_OVERRIDE)) {
            client.level.getEntities(client.player, client.player.getBoundingBox().inflate(16.0), Entity::isInvisible)
                    .forEach(entity -> Gizmos.cuboid(entity.getBoundingBox(), GizmoStyle.stroke(0xFF66CCFF, 1.5F)).setAlwaysOnTop());
        }
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
}



