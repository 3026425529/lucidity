package ml.mypals.lucidity.features;

import ml.mypals.lucidity.config.LucidityConfig;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Captures client sound events and exposes short-lived 26.3 Gizmo markers. */
public final class LuciditySoundListener implements SoundEventListener {
    public static final LuciditySoundListener INSTANCE = new LuciditySoundListener();
    private static final List<Marker> MARKERS = new ArrayList<>();

    private LuciditySoundListener() {}

    @Override
    public void onPlaySound(SoundInstance sound, WeighedSoundEvents event, float volume) {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.SOUND_VISUALIZE)) return;
        String subtitle = event.getSubtitle() == null ? "sound" : event.getSubtitle().getString();
        synchronized (MARKERS) {
            MARKERS.add(new Marker(new Vec3(sound.getX(), sound.getY(), sound.getZ()), subtitle, LucidityFeatureRuntime.ticks()));
        }
    }

    public static void tick() {
        long now = LucidityFeatureRuntime.ticks();
        synchronized (MARKERS) {
            MARKERS.removeIf(marker -> now - marker.tick() > 200 || !LucidityConfig.isEnabled(LucidityConfig.Feature.SOUND_VISUALIZE));
        }
    }

    public static void emitGizmos() {
        if (!LucidityConfig.isEnabled(LucidityConfig.Feature.SOUND_VISUALIZE)) return;
        synchronized (MARKERS) {
            for (Marker marker : MARKERS) {
                Gizmos.cuboid(new net.minecraft.world.phys.AABB(marker.position(), marker.position()).inflate(0.2),
                        GizmoStyle.stroke(0xFFFF55FF, 1.5F)).setAlwaysOnTop();
                Gizmos.billboardText(marker.subtitle(), marker.position().add(0, 0.3, 0), TextGizmo.Style.forColorAndCentered(0xFFFFAAFF))
                        .setAlwaysOnTop();
            }
        }
    }

    private record Marker(Vec3 position, String subtitle, long tick) {}
}

