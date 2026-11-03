package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.gui.components.Button;
import nevavis.gui.components.KeybindSelector;
import nevavis.gui.components.Slider;
import nevavis.gui.components.Tabs;
import nevavis.gui.components.Toggle;
import nevavis.keybinds.KeybindManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

import java.util.List;

/** Настройки интерфейса и клавиши. */
public final class SettingsPage extends Page {
	private final Tabs tabs;
	private int tab;
	private final Slider guiScale, animSpeed, glow, transparency, radius;
	private final Toggle guiAnim, glowOn, lowPerf, fpsOpt, notif, presOn, badgeOn, capeOn, lyricsOn;
	private final Slider hudScale, musicVol, notifSec;
	private final nevavis.gui.components.SearchBar url = new nevavis.gui.components.SearchBar("https://your-worker.workers.dev", 96);
	private final Button reset;
	private final KeybindSelector openKey;

	public SettingsPage() {
		NevaVisConfig cfg = ConfigManager.get().data();
		tabs = new Tabs(List.of("Interface", "Online", "Keybinds"), i -> {
			tab = i;
			rebuild();
			scroll.reset();
		});
		guiScale = new Slider("GUI Scale", 0.6f, 1.5f, 0.05f, () -> cfg.guiScale, v -> cfg.guiScale = v, "%.2fx");
		animSpeed = new Slider("Animation Speed", 0.25f, 3f, 0.05f, () -> cfg.animationSpeed, v -> cfg.animationSpeed = v, "%.2fx");
		glow = new Slider("Glow Intensity", 0f, 2f, 0.05f, () -> cfg.glowIntensity, v -> cfg.glowIntensity = v, "%.2f");
		transparency = new Slider("Transparency", 0.3f, 1f, 0.05f, () -> cfg.transparency, v -> cfg.transparency = v, "%.2f");
		radius = new Slider("Corner Radius", 0f, 14f, 1f, () -> (float) cfg.cornerRadius, v -> cfg.cornerRadius = v.intValue(), "%.0f px");
		guiAnim = new Toggle("GUI animations", () -> cfg.guiAnimations, v -> cfg.guiAnimations = v);
		glowOn = new Toggle("Glow effects", () -> cfg.glowEnabled, v -> cfg.glowEnabled = v);
		lowPerf = new Toggle("Low performance mode", () -> cfg.lowPerformance, v -> cfg.lowPerformance = v);
		lowPerf.tooltip("Disables glow rings to save FPS");
		fpsOpt = new Toggle("FPS optimization", () -> cfg.fpsOptimization, v -> cfg.fpsOptimization = v);
		notif = new Toggle("Notifications", () -> cfg.notifications, v -> cfg.notifications = v);
		hudScale = new Slider("HUD Scale", 0.5f, 2f, 0.05f, () -> cfg.hudScale, v -> cfg.hudScale = v, "%.2fx");
		notifSec = new Slider("Notification time", 1f, 8f, 0.5f, () -> cfg.notificationSeconds, v -> cfg.notificationSeconds = v, "%.1f s");
		musicVol = new Slider("Music volume", 0f, 1f, 0.05f, () -> nevavis.music.MusicPlayer.get().volume(), v -> nevavis.music.MusicPlayer.get().setVolume(v), "%.2f");
		presOn = new Toggle("Online counter (presence)", () -> cfg.presenceEnabled, v -> { cfg.presenceEnabled = v; nevavis.presence.PresenceManager.resetTimer(); });
		badgeOn = new Toggle("Show N badge on nametags", () -> cfg.showBadge, v -> cfg.showBadge = v);
		capeOn = new Toggle("NevaVis cape (for mod users)", () -> cfg.showCape, v -> cfg.showCape = v);
		lyricsOn = new Toggle("Lyrics in the sky (.lrc / .txt next to track)", () -> cfg.skyLyrics, v -> cfg.skyLyrics = v);
		url.setText(cfg.presenceUrl);
		reset = new Button("Reset to defaults", () -> {
			cfg.resetInterface();
			ThemeManager.applyByName(cfg.theme);
			ConfigManager.get().noteChange("Settings reset to defaults");
		});
		openKey = new KeybindSelector("Open NevaVis GUI", KeybindManager.openGui());
		rebuild();
	}

	private void rebuild() {
		comps.clear();
		comps.add(tabs);
		if (tab == 0) {
			comps.add(guiScale);
			comps.add(animSpeed);
			comps.add(glow);
			comps.add(transparency);
			comps.add(radius);
			comps.add(guiAnim);
			comps.add(glowOn);
			comps.add(lowPerf);
			comps.add(fpsOpt);
			comps.add(notif);
			comps.add(lyricsOn);
			comps.add(hudScale);
			comps.add(notifSec);
			comps.add(musicVol);
			comps.add(reset);
		} else if (tab == 1) {
			comps.add(presOn);
			comps.add(badgeOn);
			comps.add(capeOn);
			comps.add(url);
		} else {
			comps.add(openKey);
		}
	}

	@Override
	public String title() {
		return "Settings";
	}

	@Override
	public void onShow() {
		super.onShow();
		rebuild();
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		tabs.bounds(x, top, 170, 20);
		int cy = top + 30;
		if (tab == 0) {
			for (Slider s : new Slider[]{guiScale, animSpeed, glow, transparency, radius}) {
				s.bounds(x, cy, usable, 26);
				cy += 32;
			}
			cy += 4;
			for (Toggle t : new Toggle[]{guiAnim, glowOn, lowPerf, fpsOpt, notif, lyricsOn}) {
				t.bounds(x, cy, usable, 24);
				cy += 28;
			}
			for (Slider s : new Slider[]{hudScale, notifSec, musicVol}) {
				s.bounds(x, cy, usable, 26);
				cy += 32;
			}
			cy += 6;
			reset.bounds(x, cy, usable, 22);
			return cy + 30;
		}
		if (tab == 1) {
			presOn.bounds(x, cy, usable, 24);
			badgeOn.bounds(x, cy + 28, usable, 24);
			capeOn.bounds(x, cy + 56, usable, 24);
			url.bounds(x, cy + 108, usable, 20);
			nevavis.config.NevaVisConfig cfg = ConfigManager.get().data();
			if (!url.getText().equals(cfg.presenceUrl)) {
				cfg.presenceUrl = url.getText().trim();
				nevavis.presence.PresenceManager.resetTimer();
			}
			return cy + 170;
		}
		openKey.bounds(x, cy, usable, 26);
		return cy + 40;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		if (tab == 1) {
			Gfx.text(c, "Worker URL (see server/README.md):", x + 4, url.y - 12, ThemeManager.dim());
			Gfx.text(c, nevavis.presence.PresenceManager.statusText(), x + 4, url.y + 26, ThemeManager.accent());
		}
		if (tab == 2) {
			Gfx.text(c, "Click the field, then press a key. Esc cancels.", x + 4, openKey.y + 34, ThemeManager.dim());
		}
	}
}
