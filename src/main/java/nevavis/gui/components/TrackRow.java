package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.music.MusicPlayer;
import nevavis.music.Track;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

/** Строка трека в списке. Клик — воспроизвести; справа необязательная кнопка действия ("+" или "x"). */
public final class TrackRow extends UiComponent {
	private static final int ACT = 16;

	private final Track track;
	private final Runnable onPlay;
	private final String actionLabel;
	private final Runnable action;

	public TrackRow(Track track, Runnable onPlay, String actionLabel, Runnable action) {
		this.track = track;
		this.onPlay = onPlay;
		this.actionLabel = actionLabel;
		this.action = action;
	}

	public static String time(long ms) {
		if (ms <= 0) {
			return "--:--";
		}
		long s = ms / 1000;
		return String.format("%d:%02d", s / 60, s % 60);
	}

	private boolean inAction(double mx, double my) {
		return action != null && mx >= x + w - ACT - 4 && mx < x + w - 4 && my >= y + (h - ACT) / 2.0 && my < y + (h + ACT) / 2.0;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		Track cur = MusicPlayer.get().current();
		boolean active = cur != null && cur.file().equals(track.file());
		int r = Math.min(Gfx.radius(), 8);
		float hv = hover.value;
		int bg = Colors.lerp(Gfx.surface(0.6f), Gfx.accent(active ? 0.45f : 0.30f), active ? 1f : hv);
		Gfx.roundedRect(c, x, y, w, h, r, bg);
		if (active) {
			Gfx.roundedOutline(c, x, y, w, h, r, Gfx.accent(0.9f));
		}
		int right = x + w - 8 - (action != null ? ACT + 6 : 0);
		String dur = time(track.durationMs());
		int durW = Gfx.width(dur);
		int maxText = w - durW - 30 - (action != null ? ACT + 6 : 0);
		Gfx.text(c, Gfx.fit(track.title(), maxText), x + 10, y + 4,
				active ? ThemeManager.text() : Colors.lerp(ThemeManager.text(), ThemeManager.accent(), hv * 0.4f));
		Gfx.text(c, Gfx.fit(track.artist(), maxText), x + 10, y + 15, ThemeManager.dim());
		Gfx.rightAligned(c, dur, right, y + (h - 8) / 2, ThemeManager.dim());
		if (action != null) {
			int ax = x + w - ACT - 4;
			int ay = y + (h - ACT) / 2;
			boolean over = inAction(mx, my);
			Gfx.roundedRect(c, ax, ay, ACT, ACT, 5, over ? Gfx.accent(0.8f) : Gfx.accent(0.3f));
			Gfx.centered(c, actionLabel, ax + ACT / 2, ay + (ACT - 8) / 2, ThemeManager.text());
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != 0 || !enabled || !contains(mx, my)) {
			return false;
		}
		if (inAction(mx, my)) {
			action.run();
		} else {
			onPlay.run();
		}
		return true;
	}
}
