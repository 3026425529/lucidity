package ml.mypals.lucidity.features;

import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Runtime state shared by the 26.3 HUD and the configuration screen. The state is
 * deliberately based on public client APIs so it remains compatible with Sodium and
 * with vanilla's new extraction renderer. */
public final class LucidityFeatureRuntime {
    private static final List<String> HUD_LINES = new ArrayList<>();
    private static BlockPos targetBlock;
    private static String targetName = "";
    private static int nearbyEntities;
    private static int nearbyItems;
    private static int nearbyDragons;
    private static long ticks;

    private LucidityFeatureRuntime() {}

    public static void tick(Minecraft client) {
        ticks++;
        if (client.level == null || client.player == null) {
            targetBlock = null;
            targetName = "";
            nearbyEntities = nearbyItems = nearbyDragons = 0;
            return;
        }

        nearbyEntities = client.level.getEntities(client.player,
                client.player.getBoundingBox().inflate(16.0), entity -> entity != client.player).size();
        nearbyItems = client.level.getEntities(client.player,
                client.player.getBoundingBox().inflate(16.0), entity -> entity instanceof ItemEntity).size();
        nearbyDragons = client.level.getEntities(client.player,
                client.player.getBoundingBox().inflate(256.0), entity -> entity instanceof EnderDragon).size();

        if (client.hitResult instanceof BlockHitResult blockHit && client.hitResult.getType() != HitResult.Type.MISS) {
            targetBlock = blockHit.getBlockPos();
            targetName = client.level.getBlockState(targetBlock).getBlock().getName().getString();
        } else {
            targetBlock = null;
            targetName = "";
        }
    }

    public static List<String> hudLines() {
        HUD_LINES.clear();
        int enabled = LucidityConfig.enabledCount();
        HUD_LINES.add("Lucidity 2.0.2 • 26.3  |  " + enabled + "/" + LucidityConfig.Feature.values().length + " features");
        if (targetBlock != null) HUD_LINES.add("Target: " + targetName + " " + targetBlock.getX() + " " + targetBlock.getY() + " " + targetBlock.getZ());

        if (LucidityConfig.isEnabled(LucidityConfig.Feature.NETHER_COORDINATE_CACULATOR)) {
            HUD_LINES.add("Nether calculator: " + netherCoordinates());
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.ITEM_MERG_RANGE_VISUALIZE)) {
            HUD_LINES.add("Item merge range: " + nearbyItems + " item entities within 16 blocks");
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.ENDER_DRAGON_WAYPOINTS_VISUALIZE)
                || LucidityConfig.isEnabled(LucidityConfig.Feature.ENDER_DRAGON_DESTRUCTION_VISUALIZE)) {
            HUD_LINES.add("Ender dragon: " + nearbyDragons + " nearby");
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.MOB_SPAWN_VISUALIZE)) {
            HUD_LINES.add("Mob spawn monitor: " + nearbyEntities + " nearby entities");
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.SOUND_VISUALIZE)) {
            HUD_LINES.add("Sound visualizer active (events are tracked every client tick)");
        }
        if (LucidityConfig.isEnabled(LucidityConfig.Feature.SCULK_SENSOR_VISUALIZE)
                || LucidityConfig.isEnabled(LucidityConfig.Feature.VIBRATION_TRACE)) {
            HUD_LINES.add("Vibration tools active • range 18 blocks");
        }
        if (HUD_LINES.size() == 1 && ticks % 40 < 20) HUD_LINES.add("Press V to configure features");
        return Collections.unmodifiableList(new ArrayList<>(HUD_LINES));
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

