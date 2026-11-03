package nevavis.themes;

import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.notifications.NotificationManager;
import nevavis.render.Icons;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.List;

/** Список тем, текущая тема и плавный переход между темами. */
public final class ThemeManager {
	public static final String CUSTOM = "Custom";

	private static final int BG = 0, SURFACE = 1, ACCENT = 2, ACCENT2 = 3, TEXT = 4, DIM = 5;

	private static final List<Theme> BUILTIN = List.of(
			new Theme("NevaVis Purple", 0x0A0710, 0x1A1230, 0xA855F7, 0x3B82F6, 0xF3EEFF, 0x9A8FB8),
			new Theme("Midnight Blue", 0x060B18, 0x0F1A33, 0x3B82F6, 0x22D3EE, 0xEAF2FF, 0x8CA0C4),
			new Theme("Cyberpunk", 0x0B0614, 0x1C0F2E, 0xFF2BD6, 0x00F0FF, 0xFFF1FF, 0xB68AC9),
			new Theme("Crimson", 0x120507, 0x2A0D12, 0xE11D48, 0xFB7185, 0xFFEEF1, 0xC08A93),
			new Theme("Emerald", 0x04100B, 0x0B2A1D, 0x10B981, 0x2DD4BF, 0xE9FFF6, 0x86B8A5),
			new Theme("Arctic", 0x0A1218, 0x15283A, 0x7DD3FC, 0xE0F2FE, 0xF0FAFF, 0x8DB0C4),
			new Theme("Monochrome", 0x0A0A0A, 0x1C1C1C, 0xE5E5E5, 0x8A8A8A, 0xFFFFFF, 0x8C8C8C),
			new Theme("Aurora", 0x050F14, 0x0F2430, 0x34D399, 0xA855F7, 0xEFFFFB, 0x88B5B0),
			new Theme("Sunset", 0x140812, 0x2E1426, 0xFB923C, 0xF43F5E, 0xFFF3EA, 0xC59A8C),
			new Theme("Blood Moon", 0x0C0203, 0x240608, 0xB91C1C, 0xF97316, 0xFFE9E7, 0xA77470)
	);

	private static final int[] from = new int[6];
	private static final int[] to = new int[6];
	private static final int[] cur = new int[6];
	private static float progress = 1f;
	private static String currentName = BUILTIN.get(0).name();

	private ThemeManager() {
	}

	public static List<Theme> builtin() {
		return BUILTIN;
	}

	/** Вызывается после загрузки конфига. Устанавливает тему без анимации. */
	public static void init() {
		Theme t = resolve(ConfigManager.get().data().theme);
		currentName = t.name();
		fill(to, t);
		System.arraycopy(to, 0, cur, 0, 6);
		System.arraycopy(to, 0, from, 0, 6);
		progress = 1f;
	}

	public static Theme custom() {
		NevaVisConfig c = ConfigManager.get().data();
		int bg = c.customBg & 0xFFFFFF;
		int accent = c.customAccent & 0xFFFFFF;
		int surface = Colors.lerp(Colors.opaque(bg), Colors.opaque(accent), 0.14f) & 0xFFFFFF;
		return new Theme(CUSTOM, bg, surface, accent, c.customAccent2 & 0xFFFFFF, 0xF5F5FF, 0x9A9AB0);
	}

	private static Theme resolve(String name) {
		if (CUSTOM.equals(name)) {
			return custom();
		}
		for (Theme t : BUILTIN) {
			if (t.name().equals(name)) {
				return t;
			}
		}
		return BUILTIN.get(0);
	}

	private static void fill(int[] arr, Theme t) {
		arr[BG] = Colors.opaque(t.bg());
		arr[SURFACE] = Colors.opaque(t.surface());
		arr[ACCENT] = Colors.opaque(t.accent());
		arr[ACCENT2] = Colors.opaque(t.accent2());
		arr[TEXT] = Colors.opaque(t.text());
		arr[DIM] = Colors.opaque(t.dim());
	}

	public static void apply(Theme t) {
		System.arraycopy(cur, 0, from, 0, 6);
		fill(to, t);
		progress = 0f;
		currentName = t.name();
		ConfigManager.get().data().theme = t.name();
		ConfigManager.get().noteChange("Theme: " + t.name());
		NotificationManager.push("Theme Changed", t.name(), Icons.THEMES);
	}

	/** Применяет пользовательскую тему из конфига (вызывается при изменении цветов). */
	public static void applyCustom() {
		Theme t = custom();
		System.arraycopy(cur, 0, from, 0, 6);
		fill(to, t);
		progress = 0f;
		currentName = CUSTOM;
		ConfigManager.get().data().theme = CUSTOM;
	}

	public static void applyByName(String name) {
		apply(resolve(name));
	}

	public static String currentName() {
		return currentName;
	}

	/** Обновляет промежуточные цвета; вызывать раз в кадр. */
	public static void update(float dt) {
		if (progress >= 1f) {
			return;
		}
		progress = Math.min(1f, progress + dt * 3f * SmoothValue.speedMul());
		float e = progress * progress * (3f - 2f * progress);
		for (int i = 0; i < 6; i++) {
			cur[i] = Colors.lerp(from[i], to[i], e);
		}
	}

	public static int bg() {
		return cur[BG];
	}

	public static int surface() {
		return cur[SURFACE];
	}

	public static int accent() {
		return cur[ACCENT];
	}

	public static int accent2() {
		return cur[ACCENT2];
	}

	public static int text() {
		return cur[TEXT];
	}

	public static int dim() {
		return cur[DIM];
	}
}
