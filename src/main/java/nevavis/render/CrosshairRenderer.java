package nevavis.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig.CrosshairConfig;
import nevavis.utils.Colors;
import org.joml.Quaternionf;

import java.util.List;

/** Рисует пользовательский прицел (в игре и в предпросмотре редактора). */
public final class CrosshairRenderer {
	public static final List<String> TYPES = List.of("Dot", "Circle", "Cross", "Dynamic", "Animated", "Rainbow", "Glow");

	private CrosshairRenderer() {
	}

	/** t — время в секундах (для анимаций), dyn — 0..1 «движение» для типа Dynamic. */
	public static void draw(DrawContext c, int cx, int cy, CrosshairConfig k, float t, float dyn) {
		MatrixStack m = c.getMatrices();
		m.push();
		m.translate(cx, cy, 0f);
		boolean animated = "Animated".equals(k.type);
		float rot = k.rotation + (animated ? t * 60f * k.animationSpeed : 0f);
		if (rot != 0f) {
			m.multiply(new Quaternionf().rotateZ((float) Math.toRadians(rot)));
		}
		boolean rainbow = k.rainbow || "Rainbow".equals(k.type);
		int base = rainbow ? Colors.hsv(t * 0.3f * k.animationSpeed, 1f, 1f) : Colors.opaque(k.color);
		float gap = k.gap + ("Dynamic".equals(k.type) ? dyn * 6f : 0f)
				+ (animated ? (float) Math.sin(t * 3f * k.animationSpeed) * 2f : 0f);
		float glow = "Glow".equals(k.type) ? Math.max(k.glow, 0.6f) : k.glow;
		boolean lowPerf = ConfigManager.get().data().lowPerformance;

		if (glow > 0f && !lowPerf) {
			for (int l = 3; l >= 1; l--) {
				shape(c, k, gap, k.thickness + l * 1.5f, Colors.withAlpha(base, glow * 0.22f / l * k.alpha));
			}
		}
		if (k.outline) {
			shape(c, k, gap, k.thickness + 2f, Colors.withAlpha(0x000000, 0.8f * k.alpha));
		}
		shape(c, k, gap, k.thickness, Colors.withAlpha(base, k.alpha));
		m.pop();
	}

	private static void shape(DrawContext c, CrosshairConfig k, float gap, float thick, int color) {
		int t = Math.max(1, Math.round(thick));
		int lo = -(t / 2);
		int hi = lo + t;
		int g = Math.round(gap);
		int s = Math.max(1, Math.round(k.size));
		int ext = Math.round((thick - k.thickness) / 2f);

		switch (k.type) {
			case "Dot" -> {
				int d = Math.max(1, Math.round(k.size / 3f + (thick - k.thickness) / 2f));
				Gfx.fill(c, -d, -d, d + 1, d + 1, color);
			}
			case "Circle" -> {
				int r = s + g;
				int n = Math.max(24, (int) Math.round(2 * Math.PI * r));
				for (int i = 0; i < n; i++) {
					double a = 2 * Math.PI * i / n;
					int x = (int) Math.round(Math.cos(a) * r);
					int y = (int) Math.round(Math.sin(a) * r);
					Gfx.fill(c, x + lo, y + lo, x + lo + t, y + lo + t, color);
				}
			}
			default -> {
				Gfx.fill(c, g - ext, lo, g + s + ext, hi, color);
				Gfx.fill(c, -g - s - ext, lo, -g + ext, hi, color);
				Gfx.fill(c, lo, -g - s - ext, hi, -g + ext, color);
				Gfx.fill(c, lo, g - ext, hi, g + s + ext, color);
			}
		}
	}
}
