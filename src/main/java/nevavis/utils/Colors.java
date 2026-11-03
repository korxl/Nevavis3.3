package nevavis.utils;

/** Операции с ARGB-цветами (0xAARRGGBB). */
public final class Colors {
	private Colors() {
	}

	public static int opaque(int rgb) {
		return 0xFF000000 | (rgb & 0xFFFFFF);
	}

	public static int withAlpha(int color, float alpha) {
		int a = (int) (Math.max(0f, Math.min(1f, alpha)) * 255f + 0.5f);
		return (a << 24) | (color & 0xFFFFFF);
	}

	public static int red(int c) {
		return (c >> 16) & 255;
	}

	public static int green(int c) {
		return (c >> 8) & 255;
	}

	public static int blue(int c) {
		return c & 255;
	}

	public static int rgb(int r, int g, int b) {
		return 0xFF000000 | (clamp255(r) << 16) | (clamp255(g) << 8) | clamp255(b);
	}

	private static int clamp255(int v) {
		return Math.max(0, Math.min(255, v));
	}

	/** HSV (h, s, v в диапазоне 0..1) → непрозрачный ARGB. */
	public static int hsv(float h, float s, float v) {
		h = h - (float) Math.floor(h);
		float hh = h * 6f;
		int i = (int) Math.floor(hh);
		float f = hh - i;
		float p = v * (1f - s), q = v * (1f - s * f), t = v * (1f - s * (1f - f));
		float r, g, b;
		switch (i % 6) {
			case 0 -> { r = v; g = t; b = p; }
			case 1 -> { r = q; g = v; b = p; }
			case 2 -> { r = p; g = v; b = t; }
			case 3 -> { r = p; g = q; b = v; }
			case 4 -> { r = t; g = p; b = v; }
			default -> { r = v; g = p; b = q; }
		}
		return rgb((int) (r * 255f + 0.5f), (int) (g * 255f + 0.5f), (int) (b * 255f + 0.5f));
	}

	public static int lerp(int a, int b, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int aa = a >>> 24, ba = b >>> 24;
		int ar = (a >> 16) & 255, br = (b >> 16) & 255;
		int ag = (a >> 8) & 255, bg = (b >> 8) & 255;
		int ab = a & 255, bb = b & 255;
		int ra = (int) (aa + (ba - aa) * t + 0.5f);
		int rr = (int) (ar + (br - ar) * t + 0.5f);
		int rg = (int) (ag + (bg - ag) * t + 0.5f);
		int rb = (int) (ab + (bb - ab) * t + 0.5f);
		return (ra << 24) | (rr << 16) | (rg << 8) | rb;
	}
}
