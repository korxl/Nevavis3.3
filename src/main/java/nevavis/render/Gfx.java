package nevavis.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import nevavis.config.NevaVisConfig;
import nevavis.config.ConfigManager;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

/**
 * Примитивы 2D-рендера для NevaVis: скруглённые панели, обводка, свечение, текст.
 * Всё строится из DrawContext.fill / fillGradient (без своих шейдеров).
 * Глобальный коэффициент прозрачности (fade) используется для анимации появления.
 */
public final class Gfx {
	private static float fade = 1f;
	private static final int[][] INSETS = new int[41][];

	// Преобразование «виртуальные координаты окна → экранные GUI-координаты» (нужно для scissor)
	private static float trCx, trCy, trScale = 1f, trVw, trVh;

	private Gfx() {
	}

	// ---------- состояние ----------

	public static void setFade(float f) {
		fade = Math.max(0f, Math.min(1f, f));
	}

	public static float fade() {
		return fade;
	}

	public static void setTransform(float cx, float cy, float scale, float vw, float vh) {
		trCx = cx;
		trCy = cy;
		trScale = scale;
		trVw = vw;
		trVh = vh;
	}

	public static float sx(float vx) {
		return trCx + (vx - trVw / 2f) * trScale;
	}

	public static float sy(float vy) {
		return trCy + (vy - trVh / 2f) * trScale;
	}

	public static int mul(int argb) {
		int a = (int) ((argb >>> 24) * fade + 0.5f);
		return (a << 24) | (argb & 0xFFFFFF);
	}

	private static NevaVisConfig cfg() {
		return ConfigManager.get().data();
	}

	public static int radius() {
		return Math.max(0, Math.min(16, cfg().cornerRadius));
	}

	private static int[] insets(int r) {
		int[] arr = INSETS[r];
		if (arr == null) {
			arr = new int[r];
			for (int i = 0; i < r; i++) {
				double yc = r - i - 0.5;
				double hc = Math.sqrt(r * (double) r - yc * yc);
				arr[i] = (int) Math.round(r - hc);
			}
			INSETS[r] = arr;
		}
		return arr;
	}

	// ---------- цвета темы ----------

	/** Цвет стеклянной поверхности с учётом настройки Transparency. */
	public static int surface(float relativeAlpha) {
		return Colors.withAlpha(ThemeManager.surface(), relativeAlpha * cfg().transparency);
	}

	public static int accent(float alpha) {
		return Colors.withAlpha(ThemeManager.accent(), alpha);
	}

	public static int accent2(float alpha) {
		return Colors.withAlpha(ThemeManager.accent2(), alpha);
	}

	// ---------- прямоугольники ----------

	public static void fill(DrawContext c, int x1, int y1, int x2, int y2, int color) {
		int m = mul(color);
		if ((m >>> 24) == 0 || x2 <= x1 || y2 <= y1) {
			return;
		}
		c.fill(x1, y1, x2, y2, m);
	}

	public static void roundedRect(DrawContext c, int x, int y, int w, int h, int r, int color) {
		int m = mul(color);
		if ((m >>> 24) == 0 || w <= 0 || h <= 0) {
			return;
		}
		r = Math.min(r, Math.min(w, h) / 2);
		if (r <= 0) {
			c.fill(x, y, x + w, y + h, m);
			return;
		}
		int[] ins = insets(r);
		for (int i = 0; i < r; i++) {
			int in = ins[i];
			c.fill(x + in, y + i, x + w - in, y + i + 1, m);
			c.fill(x + in, y + h - 1 - i, x + w - in, y + h - i, m);
		}
		c.fill(x, y + r, x + w, y + h - r, m);
	}

	/** Вертикальный градиент в скруглённом прямоугольнике. */
	public static void roundedGradient(DrawContext c, int x, int y, int w, int h, int r, int top, int bottom) {
		if (w <= 0 || h <= 0) {
			return;
		}
		r = Math.min(r, Math.min(w, h) / 2);
		if (r <= 0) {
			c.fillGradient(x, y, x + w, y + h, mul(top), mul(bottom));
			return;
		}
		int[] ins = insets(r);
		for (int i = 0; i < r; i++) {
			int in = ins[i];
			int ct = mul(Colors.lerp(top, bottom, (i + 0.5f) / h));
			int cb = mul(Colors.lerp(top, bottom, (h - i - 0.5f) / h));
			if ((ct >>> 24) != 0) {
				c.fill(x + in, y + i, x + w - in, y + i + 1, ct);
			}
			if ((cb >>> 24) != 0) {
				c.fill(x + in, y + h - 1 - i, x + w - in, y + h - i, cb);
			}
		}
		if (h - 2 * r > 0) {
			c.fillGradient(x, y + r, x + w, y + h - r,
					mul(Colors.lerp(top, bottom, (float) r / h)),
					mul(Colors.lerp(top, bottom, (float) (h - r) / h)));
		}
	}

	/** Обводка толщиной 1 px по скруглённому контуру. */
	public static void roundedOutline(DrawContext c, int x, int y, int w, int h, int r, int color) {
		int m = mul(color);
		if ((m >>> 24) == 0 || w <= 1 || h <= 1) {
			return;
		}
		r = Math.min(r, Math.min(w, h) / 2);
		if (r <= 0) {
			c.fill(x, y, x + w, y + 1, m);
			c.fill(x, y + h - 1, x + w, y + h, m);
			c.fill(x, y + 1, x + 1, y + h - 1, m);
			c.fill(x + w - 1, y + 1, x + w, y + h - 1, m);
			return;
		}
		int[] ins = insets(r);
		c.fill(x + ins[0], y, x + w - ins[0], y + 1, m);
		c.fill(x + ins[0], y + h - 1, x + w - ins[0], y + h, m);
		for (int i = 1; i < r; i++) {
			int from = ins[i];
			int to = Math.max(from + 1, ins[i - 1]);
			c.fill(x + from, y + i, x + to, y + i + 1, m);
			c.fill(x + w - to, y + i, x + w - from, y + i + 1, m);
			c.fill(x + from, y + h - 1 - i, x + to, y + h - i, m);
			c.fill(x + w - to, y + h - 1 - i, x + w - from, y + h - i, m);
		}
		c.fill(x, y + r, x + 1, y + h - r, m);
		c.fill(x + w - 1, y + r, x + w, y + h - r, m);
	}

	/** Свечение: несколько расходящихся колец с затуханием. Отключается в Low Performance Mode. */
	public static void glow(DrawContext c, int x, int y, int w, int h, int r, int color, float strength) {
		NevaVisConfig cf = cfg();
		if (!cf.glowEnabled || cf.lowPerformance || strength <= 0.01f) {
			return;
		}
		int layers = Math.round(4f * cf.glowIntensity);
		for (int k = 1; k <= layers; k++) {
			float f = 1f - (k - 1f) / layers;
			float a = strength * 0.35f * f * f;
			if (a < 0.01f) {
				break;
			}
			roundedOutline(c, x - k, y - k, w + 2 * k, h + 2 * k, Math.min(40, r + k), Colors.withAlpha(color, a));
		}
	}

	/** Стеклянная панель: градиентная заливка, светлая кромка сверху и обводка акцентом. */
	public static void glassPanel(DrawContext c, int x, int y, int w, int h, int r, float outlineAlpha) {
		int top = Colors.lerp(surface(0.95f), Colors.withAlpha(0xFFFFFF, 0.10f * cfg().transparency), 0.35f);
		int bottom = surface(0.75f);
		roundedGradient(c, x, y, w, h, r, top, bottom);
		fill(c, x + r, y + 1, x + w - r, y + 2, Colors.withAlpha(0xFFFFFF, 0.10f));
		roundedOutline(c, x, y, w, h, r, accent(outlineAlpha));
	}

	// ---------- текст ----------

	public static TextRenderer tr() {
		return MinecraftClient.getInstance().textRenderer;
	}

	public static int width(String s) {
		return tr().getWidth(s);
	}

	public static void text(DrawContext c, String s, int x, int y, int color) {
		text(c, s, x, y, color, false);
	}

	public static void text(DrawContext c, String s, int x, int y, int color, boolean shadow) {
		int m = mul(color);
		if ((m >>> 24) < 8 || s == null || s.isEmpty()) {
			return; // альфа < 8 vanilla рисует как непрозрачный
		}
		c.drawText(tr(), s, x, y, m, shadow);
	}

	public static void centered(DrawContext c, String s, int cx, int y, int color) {
		text(c, s, cx - width(s) / 2, y, color, false);
	}

	public static void rightAligned(DrawContext c, String s, int rightX, int y, int color) {
		text(c, s, rightX - width(s), y, color, false);
	}

	/** Обрезает строку до maxW пикселей, добавляя «..». */
	public static String fit(String s, int maxW) {
		if (width(s) <= maxW) {
			return s;
		}
		String t = s;
		while (t.length() > 1 && width(t + "..") > maxW) {
			t = t.substring(0, t.length() - 1);
		}
		return t + "..";
	}
}
