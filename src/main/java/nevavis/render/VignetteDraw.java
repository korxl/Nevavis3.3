package nevavis.render;

import net.minecraft.client.gui.DrawContext;

/** Цветная виньетка по краям экрана (ступенчатые полосы с затуханием). */
public final class VignetteDraw {
	private VignetteDraw() {
	}

	public static void edges(DrawContext c, int rgb, float strength, int size) {
		if (strength <= 0.01f) {
			return;
		}
		rgb &= 0xFFFFFF;
		int w = c.getScaledWindowWidth();
		int h = c.getScaledWindowHeight();
		for (int i = 0; i < size; i += 2) {
			float f = 1f - i / (float) size;
			int a = (int) (255f * Math.min(1f, strength) * f * f);
			if (a < 4) {
				break;
			}
			int col = (a << 24) | rgb;
			c.fill(0, i, w, i + 2, col);
			c.fill(0, h - i - 2, w, h - i, col);
			c.fill(i, i + 2, i + 2, h - i - 2, col);
			c.fill(w - i - 2, i + 2, w - i, h - i - 2, col);
		}
	}
}
