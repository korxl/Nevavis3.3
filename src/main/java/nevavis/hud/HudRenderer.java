package nevavis.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.gui.screens.HudEditorScreen;
import nevavis.modules.ModuleManager;
import nevavis.music.MusicPlayer;
import nevavis.music.Track;
import nevavis.notifications.NotificationManager;
import nevavis.render.CrosshairRenderer;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.List;

/** Рисует всё поверх игры: эффекты модулей, элементы HUD, прицел, панель музыки, уведомления. */
public final class HudRenderer {
	public static final List<String> MUSIC_VARIANTS = List.of("Glass", "Minimal", "Neon", "Compact", "Island");
	private static final int MUSIC_SHOW_MS = 5000;

	private static final SmoothValue MUSIC_SHOW = new SmoothValue(0f);
	private static final float[] SHOWN_BANDS = new float[8];
	private static long lastNs = System.nanoTime();

	private HudRenderer() {
	}

	public static void render(DrawContext c, float tickDelta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1_000_000_000f);
		lastNs = now;

		Gfx.setFade(1f);
		if (!mc.options.hudHidden && mc.player != null) {
			ClickTracker.poll(mc);
			ModuleManager.hud(c, mc, tickDelta);
			if (!(mc.currentScreen instanceof HudEditorScreen)) {
				HudManager.render(c, mc);
			}
			drawCrosshair(c, mc);
			SkyLyrics.render(c, mc, dt);
			drawMusic(c, mc, dt);
		}
		NotificationManager.render(c);
		Gfx.setFade(1f);
	}

	private static void drawCrosshair(DrawContext c, MinecraftClient mc) {
		NevaVisConfig.CrosshairConfig k = ConfigManager.get().data().crosshair;
		if (!k.enabled || mc.currentScreen != null || !mc.options.getPerspective().isFirstPerson()) {
			return;
		}
		float t = (System.currentTimeMillis() % 100000L) / 1000f;
		float dyn = Math.min(1f, SessionStats.speed() / 6f);
		CrosshairRenderer.draw(c, c.getScaledWindowWidth() / 2, c.getScaledWindowHeight() / 2, k, t, dyn);
	}

	private static void drawMusic(DrawContext c, MinecraftClient mc, float dt) {
		MusicPlayer mp = MusicPlayer.get();
		Track t = mp.current();
		boolean want = ConfigManager.get().data().music.hudEnabled && t != null && mp.isActive()
				&& (System.currentTimeMillis() - mp.changedAt() < MUSIC_SHOW_MS || mc.currentScreen != null);
		MUSIC_SHOW.approach(want ? 1f : 0f, dt, 10f);
		float s = MUSIC_SHOW.value;
		if (s < 0.02f || t == null) {
			return;
		}
		String variant = ConfigManager.get().data().music.hudVariant;
		int w, h, r;
		boolean eq = true, artist = true, progress = true;
		float glow = 0.5f, bg = 0.88f;
		switch (variant) {
			case "Minimal" -> { w = 190; h = 24; r = 5; eq = false; glow = 0f; bg = 0.45f; }
			case "Neon" -> { w = 214; h = 32; r = 8; glow = 1f; bg = 0.92f; }
			case "Compact" -> { w = 150; h = 20; r = 10; artist = false; progress = false; glow = 0.3f; }
			case "Island" -> { w = 196; h = 30; r = 15; glow = 0.15f; bg = 0.97f; }
			default -> { w = 214; h = 32; r = 10; }
		}
		int sw = c.getScaledWindowWidth();
		int x = (sw - w) / 2;
		int y = 6 - Math.round((1f - s) * (h + 12));
		Gfx.setFade(s);
		Gfx.glow(c, x, y, w, h, r, ThemeManager.accent(), glow);
		if ("Glass".equals(variant)) {
			Gfx.glassPanel(c, x, y, w, h, r, 0.8f);
		} else {
			Gfx.roundedRect(c, x, y, w, h, r, Colors.withAlpha("Island".equals(variant) ? 0x000000 : ThemeManager.bg(), bg));
			boolean neon = "Neon".equals(variant);
			Gfx.roundedOutline(c, x, y, w, h, r, "Minimal".equals(variant) ? Gfx.accent(0.3f) : Gfx.accent(neon ? 1f : 0.7f));
			if (neon) {
				Gfx.roundedOutline(c, x - 1, y - 1, w + 2, h + 2, r + 1, Gfx.accent2(0.6f));
			}
		}

		int tx = x + 12;
		if (eq) {
			float[] b = mp.bands();
			int maxH = h - 12;
			for (int i = 0; i < 8; i++) {
				SHOWN_BANDS[i] += (b[i] - SHOWN_BANDS[i]) * Math.min(1f, dt * 18f);
				int bh = 2 + Math.round(SHOWN_BANDS[i] * maxH);
				Gfx.roundedGradient(c, x + 12 + i * 4, y + h / 2 + maxH / 2 - bh, 3, bh, 1, Gfx.accent(0.95f), Gfx.accent2(0.95f));
			}
			tx = x + 12 + 8 * 4 + 6;
		}
		int tw = w - (tx - x) - 12;
		String line2 = mp.isPlaying() ? t.artist() : "Paused - " + t.artist();
		if (artist) {
			Gfx.text(c, Gfx.fit(t.title(), tw), tx, y + (h >= 28 ? 6 : 4), ThemeManager.text());
			Gfx.text(c, Gfx.fit(line2, tw), tx, y + (h >= 28 ? 16 : 13), ThemeManager.dim());
		} else {
			Gfx.text(c, Gfx.fit(t.title(), tw), tx, y + (h - 8) / 2, ThemeManager.text());
		}
		long dur = mp.durationMs();
		if (progress && dur > 0) {
			int pw = w - 2 * r;
			Gfx.roundedRect(c, x + r, y + h - 4, pw, 2, 1, Colors.withAlpha(ThemeManager.dim(), 0.3f));
			int fw = Math.round(pw * Math.min(1f, mp.positionMs() / (float) dur));
			if (fw > 0) {
				Gfx.roundedGradient(c, x + r, y + h - 4, fw, 2, 1, Gfx.accent(1f), Gfx.accent2(1f));
			}
		}
		Gfx.setFade(1f);
	}
}
