package nevavis.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import nevavis.config.ConfigManager;
import nevavis.music.Lyrics;
import nevavis.music.MusicPlayer;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

/**
 * Текст песни «в небе»: строка привязана к направлению в мире (над горизонтом) и при повороте головы
 * смещается как объект в небе; медленно следует за взглядом, чтобы не потеряться. Рисуется как 2D-проекция.
 */
public final class SkyLyrics {
	private static float anchorYaw;
	private static boolean init;

	private SkyLyrics() {
	}

	private static float wrap(float a) {
		a %= 360f;
		if (a > 180f) {
			a -= 360f;
		}
		if (a < -180f) {
			a += 360f;
		}
		return a;
	}

	public static void render(DrawContext c, MinecraftClient mc, float dt) {
		if (!ConfigManager.get().data().skyLyrics || mc.currentScreen != null || mc.player == null) {
			return;
		}
		MusicPlayer mp = MusicPlayer.get();
		Lyrics ly = Lyrics.of(mp.current());
		if (ly == null || !mp.isPlaying()) {
			return;
		}
		int cur = ly.indexAt(mp.positionMs());
		if (cur < 0) {
			return;
		}
		float yaw = mc.player.getYaw();
		if (!init) {
			anchorYaw = yaw;
			init = true;
		}
		anchorYaw += wrap(yaw - anchorYaw) * Math.min(1f, dt * 0.6f);

		int sw = c.getScaledWindowWidth(), sh = c.getScaledWindowHeight();
		float fov = Math.max(30f, mc.options.getFov().getValue());
		float pxPerDeg = sh / fov;
		float x = sw / 2f + wrap(anchorYaw - yaw) * pxPerDeg;
		float y = sh / 2f + (-32f - mc.player.getPitch()) * pxPerDeg;

		drawLine(c, ly.line(cur - 1), x, y - 26, 1.0f, 0.35f);
		drawLine(c, ly.line(cur), x, y, 2.0f, 1f);
		drawLine(c, ly.line(cur + 1), x, y + 24, 1.0f, 0.35f);
	}

	private static void drawLine(DrawContext c, String text, float cx, float cy, float scale, float alpha) {
		if (text.isEmpty()) {
			return;
		}
		int sw = c.getScaledWindowWidth();
		float edge = Math.min(1f, Math.min(cx, sw - cx) / (sw * 0.15f));
		float a = alpha * Math.max(0f, edge);
		if (a < 0.03f || cy < -20 || cy > c.getScaledWindowHeight() + 20) {
			return;
		}
		int w = Gfx.width(text);
		MatrixStack m = c.getMatrices();
		m.push();
		m.translate(cx, cy, 0f);
		m.scale(scale, scale, 1f);
		int glow = Colors.withAlpha(ThemeManager.accent(), a * 0.35f);
		Gfx.text(c, text, -w / 2 + 1, 1, glow, false);
		Gfx.text(c, text, -w / 2, 0, Colors.withAlpha(0xFFFFFF, a), false);
		m.pop();
	}
}
