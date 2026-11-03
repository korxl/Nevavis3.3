package nevavis.gui.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.gui.components.Card;
import nevavis.gui.components.Toggle;
import nevavis.modules.ModuleManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

import java.util.ArrayList;
import java.util.List;

/** Главная: статистика, быстрые переключатели, последние изменения. */
public final class HomePage extends Page {
	private static final int CARD_H = 44, GAP = 8, COLS = 3, ROW_H = 24;

	private final String[] vals = new String[]{"-", "-", "-", "-", "0", "-", "-", "-"};
	private final List<Card> cards = new ArrayList<>();
	private final List<Toggle> toggles = new ArrayList<>();
	private long lastRefresh;
	private int toggleHeaderY, recentHeaderY, recentY;

	public HomePage() {
		String[] titles = {"FPS", "Ping", "XYZ", "Biome", "Active modules", "Theme", "Music track", "NevaVis online"};
		for (int i = 0; i < titles.length; i++) {
			final int idx = i;
			Card card = new Card(titles[i], () -> vals[idx]);
			cards.add(card);
			comps.add(card);
		}
		NevaVisConfig cfg = ConfigManager.get().data();
		toggles.add(new Toggle("GUI animations", () -> cfg.guiAnimations, v -> cfg.guiAnimations = v));
		toggles.add(new Toggle("Glow effects", () -> cfg.glowEnabled, v -> cfg.glowEnabled = v));
		toggles.add(new Toggle("Low performance mode", () -> cfg.lowPerformance, v -> cfg.lowPerformance = v));
		comps.addAll(toggles);
	}

	@Override
	public String title() {
		return "Home";
	}

	@Override
	protected void update() {
		long now = System.currentTimeMillis();
		if (now - lastRefresh < 250) {
			return;
		}
		lastRefresh = now;
		MinecraftClient mc = MinecraftClient.getInstance();
		vals[0] = Integer.toString(mc.getCurrentFps());
		if (mc.player != null && mc.world != null) {
			String ping = "-";
			if (mc.getNetworkHandler() != null) {
				PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
				if (e != null) {
					ping = e.getLatency() + " ms";
				}
			}
			vals[1] = ping;
			vals[2] = (int) Math.floor(mc.player.getX()) + " " + (int) Math.floor(mc.player.getY())
					+ " " + (int) Math.floor(mc.player.getZ());
			vals[3] = mc.world.getBiome(mc.player.getBlockPos()).getKey()
					.map(k -> k.getValue().getPath().replace('_', ' ')).orElse("unknown");
		} else {
			vals[1] = "-";
			vals[2] = "-";
			vals[3] = "-";
		}
		vals[4] = Integer.toString(ModuleManager.activeCount());
		vals[5] = ThemeManager.currentName();
		nevavis.music.Track tr = nevavis.music.MusicPlayer.get().current();
		vals[6] = tr == null ? "Nothing playing" : tr.title();
		vals[7] = nevavis.presence.PresenceManager.statusText();
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		int cw = (usable - GAP * (COLS - 1)) / COLS;
		int cy = top;
		for (int i = 0; i < cards.size(); i++) {
			int col = i % COLS;
			cards.get(i).bounds(x + col * (cw + GAP), cy, cw, CARD_H);
			if (col == COLS - 1 || i == cards.size() - 1) {
				cy += CARD_H + GAP;
			}
		}
		toggleHeaderY = cy + 4;
		cy += 20;
		for (Toggle t : toggles) {
			t.bounds(x, cy, usable, ROW_H);
			cy += ROW_H + 4;
		}
		recentHeaderY = cy + 6;
		recentY = cy + 24;
		int lines = Math.max(1, ConfigManager.get().recent().size());
		return recentY + lines * 12 + 8;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		Gfx.text(c, "QUICK TOGGLES", x + 2, toggleHeaderY, ThemeManager.accent());
		Gfx.text(c, "RECENT CHANGES", x + 2, recentHeaderY, ThemeManager.accent());
		List<String> recent = ConfigManager.get().recent();
		if (recent.isEmpty()) {
			Gfx.text(c, "No changes yet", x + 4, recentY, ThemeManager.dim());
			return;
		}
		int ty = recentY;
		for (String s : recent) {
			Gfx.text(c, Gfx.fit(s, w - 20), x + 4, ty, ThemeManager.dim());
			ty += 12;
		}
	}
}
