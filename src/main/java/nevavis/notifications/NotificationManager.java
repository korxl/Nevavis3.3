package nevavis.notifications;

import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.render.Gfx;
import nevavis.render.Icons;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

import java.util.ArrayList;
import java.util.List;

/** Всплывающие уведомления (правый нижний угол): иконка, заголовок, описание, таймер, анимация. */
public final class NotificationManager {
	private static final int W = 176, H = 34, GAP = 6, MAX = 4, ANIM_MS = 260;

	private static final class Toast {
		String title, desc;
		int icon;
		long born, duration;
	}

	private static final List<Toast> TOASTS = new ArrayList<>();

	private NotificationManager() {
	}

	public static synchronized void push(String title, String desc, int icon) {
		var cfg = ConfigManager.get().data();
		if (!cfg.notifications) {
			return;
		}
		Toast t = new Toast();
		t.title = title;
		t.desc = desc;
		t.icon = icon;
		t.born = System.currentTimeMillis();
		t.duration = Math.max(800L, (long) (cfg.notificationSeconds * 1000f));
		TOASTS.add(t);
		while (TOASTS.size() > MAX) {
			TOASTS.remove(0);
		}
	}

	public static synchronized void render(DrawContext c) {
		if (TOASTS.isEmpty()) {
			return;
		}
		long now = System.currentTimeMillis();
		TOASTS.removeIf(t -> now - t.born > t.duration);
		int sw = c.getScaledWindowWidth(), sh = c.getScaledWindowHeight();
		int y = sh - 10 - H;
		for (int i = TOASTS.size() - 1; i >= 0; i--) {
			Toast t = TOASTS.get(i);
			long age = now - t.born;
			long left = t.duration - age;
			float in = Math.min(1f, age / (float) ANIM_MS);
			float out = Math.min(1f, left / (float) ANIM_MS);
			float p = Math.min(in, out);
			float e = p * p * (3f - 2f * p);
			int x = sw - 10 - Math.round(W * e);
			draw(c, t, x, y, e, 1f - left / (float) t.duration);
			y -= H + GAP;
		}
		Gfx.setFade(1f);
	}

	private static void draw(DrawContext c, Toast t, int x, int y, float alpha, float elapsed) {
		Gfx.setFade(alpha);
		int r = Math.min(Gfx.radius(), 8);
		Gfx.glow(c, x, y, W, H, r, ThemeManager.accent(), 0.5f);
		Gfx.glassPanel(c, x, y, W, H, r, 0.7f);
		Gfx.roundedRect(c, x + 6, y + 7, 20, 20, 6, Gfx.accent(0.25f));
		Icons.draw(c, t.icon, x + 12, y + 13, ThemeManager.accent());
		Gfx.text(c, Gfx.fit(t.title, W - 40), x + 32, y + 7, ThemeManager.text());
		Gfx.text(c, Gfx.fit(t.desc, W - 40), x + 32, y + 19, ThemeManager.dim());
		int bw = Math.round((W - 2 * r) * (1f - elapsed));
		Gfx.roundedGradient(c, x + r, y + H - 3, Math.max(0, bw), 2, 1, Gfx.accent(0.9f), Colors.withAlpha(ThemeManager.accent2(), 0.9f));
	}
}
