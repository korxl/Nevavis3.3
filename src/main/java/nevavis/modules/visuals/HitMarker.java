package nevavis.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.HitResult;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.utils.Colors;

/** Hit Marker: крестик у прицела, когда вы бьёте существо. */
public final class HitMarker extends Module {
	private boolean lastSwing;
	private long hitAt;

	public HitMarker() {
		super("hit_marker", "Hit Marker",
				"Shows an X at the crosshair when you hit an entity", ModuleCategory.ENTITIES);
		addSetting("hue", "Color (hue)", 0f, 1f, 0.01f, 0f);
		addSetting("size", "Size", 3f, 12f, 1f, 6f);
		addSetting("duration", "Duration (ms)", 100f, 600f, 25f, 250f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		ClientPlayerEntity p = mc.player;
		if (p == null) {
			return;
		}
		boolean swing = p.handSwinging;
		if (swing && !lastSwing && mc.crosshairTarget != null
				&& mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
			hitAt = System.currentTimeMillis();
		}
		lastSwing = swing;

		float dur = get("duration");
		float age = System.currentTimeMillis() - hitAt;
		if (age >= dur) {
			return;
		}
		int a = (int) (255f * (1f - age / dur));
		int rgb = Colors.hsv(get("hue"), 1f, 1f) & 0xFFFFFF;
		int col = (a << 24) | rgb;
		int cx = c.getScaledWindowWidth() / 2;
		int cy = c.getScaledWindowHeight() / 2;
		int size = (int) get("size");
		int gap = 3;
		for (int i = gap; i < gap + size; i++) {
			c.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, col);
			c.fill(cx - i - 1, cy + i, cx - i, cy + i + 1, col);
			c.fill(cx + i, cy - i - 1, cx + i + 1, cy - i, col);
			c.fill(cx - i - 1, cy - i - 1, cx - i, cy - i, col);
		}
	}
}
