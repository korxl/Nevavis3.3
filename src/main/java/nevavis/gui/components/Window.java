package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

/** Рамка главного окна: стеклянный фон, пульсирующее свечение, обводка. */
public final class Window {
	private Window() {
	}

	public static void draw(DrawContext c, int x, int y, int w, int h) {
		int r = Math.min(Gfx.radius() + 4, 18);
		float pulse = 0.75f + 0.25f * (float) Math.sin(System.currentTimeMillis() / 700.0);

		Gfx.glow(c, x, y, w, h, r, ThemeManager.accent(), 0.9f * pulse);

		int top = Colors.lerp(Gfx.surface(1f), Gfx.accent(0.25f), 0.25f);
		Gfx.roundedGradient(c, x, y, w, h, r, top, Colors.withAlpha(ThemeManager.bg(), 0.96f));
		Gfx.fill(c, x + r, y + 1, x + w - r, y + 2, Colors.withAlpha(0xFFFFFF, 0.12f));
		Gfx.roundedOutline(c, x, y, w, h, r, Colors.lerp(Gfx.accent(0.8f), Gfx.accent2(0.8f), 0.4f));
	}
}
