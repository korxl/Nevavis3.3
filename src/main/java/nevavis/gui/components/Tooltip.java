package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

/** Подсказка у курсора. Компоненты вызывают show() во время кадра, экран рисует в конце. */
public final class Tooltip {
	private static String pending;

	private Tooltip() {
	}

	public static void show(String text) {
		pending = text;
	}

	public static void renderAndClear(DrawContext c, double mx, double my, int boundsW, int boundsH) {
		String t = pending;
		pending = null;
		if (t == null) {
			return;
		}
		int tw = Gfx.width(t) + 10;
		int th = 16;
		int tx = (int) mx + 10;
		int ty = (int) my + 12;
		if (tx + tw > boundsW - 4) {
			tx = boundsW - 4 - tw;
		}
		if (ty + th > boundsH - 4) {
			ty = (int) my - th - 6;
		}
		Gfx.roundedRect(c, tx, ty, tw, th, Math.min(Gfx.radius(), 6), Gfx.surface(1f));
		Gfx.roundedRect(c, tx, ty, tw, th, Math.min(Gfx.radius(), 6), Gfx.accent(0.10f));
		Gfx.roundedOutline(c, tx, ty, tw, th, Math.min(Gfx.radius(), 6), Gfx.accent(0.7f));
		Gfx.text(c, t, tx + 5, ty + 4, ThemeManager.text());
	}

	public static void clear() {
		pending = null;
	}
}
