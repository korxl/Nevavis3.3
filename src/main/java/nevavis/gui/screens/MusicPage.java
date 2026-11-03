package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.gui.components.Button;
import nevavis.gui.components.Dropdown;
import nevavis.gui.components.SearchBar;
import nevavis.gui.components.Slider;
import nevavis.gui.components.Toggle;
import nevavis.gui.components.TrackRow;
import nevavis.hud.HudRenderer;
import nevavis.music.MusicLibrary;
import nevavis.music.MusicPlayer;
import nevavis.music.Track;
import nevavis.music.playlist.PlaylistStore;
import nevavis.notifications.NotificationManager;
import nevavis.render.Gfx;
import nevavis.render.Icons;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Музыкальный плеер: управление, громкость, стиль панели, плейлисты, поиск и список треков. */
public final class MusicPage extends Page {
	private static final int ROW_H = 26, GAP = 4;
	private static final String ALL = "All tracks";

	private final MusicPlayer player = MusicPlayer.get();
	private final Button prev, play, next, shuffle, repeat, create, delete;
	private final Slider volume;
	private final Toggle showPanel;
	private final Dropdown style, viewDrop, targetDrop;
	private final SearchBar search = new SearchBar("Search tracks...");
	private final SearchBar nameField = new SearchBar("New playlist name...", 24);

	private final List<String> viewOptions = new ArrayList<>();
	private final List<String> targetOptions = new ArrayList<>();
	private String view = ALL;
	private String target = "-";
	private String lastSig = null;
	private int rowCount;
	private int panelY, listY;
	private final float[] shown = new float[8];

	public MusicPage() {
		prev = new Button("Previous", player::previous);
		play = new Button(() -> player.isPlaying() ? "Pause" : "Play", player::togglePause);
		play.primary();
		next = new Button("Next", player::next);
		shuffle = new Button(() -> "Shuffle: " + (player.shuffle() ? "ON" : "OFF"), player::toggleShuffle);
		repeat = new Button(() -> "Repeat: " + player.repeat(), player::cycleRepeat);
		volume = new Slider("Volume", 0f, 1f, 0.01f, player::volume, player::setVolume, "%.2f");
		var mc = ConfigManager.get().data().music;
		showPanel = new Toggle("Now playing panel", () -> ConfigManager.get().data().music.hudEnabled,
				v -> ConfigManager.get().data().music.hudEnabled = v);
		style = new Dropdown("Panel style", HudRenderer.MUSIC_VARIANTS,
				() -> ConfigManager.get().data().music.hudVariant, v -> ConfigManager.get().data().music.hudVariant = v);
		viewDrop = new Dropdown("List", viewOptions, () -> view, v -> {
			view = v;
			lastSig = null;
		});
		targetDrop = new Dropdown("Add tracks to", targetOptions, () -> target, v -> target = v);
		create = new Button("Create playlist", this::createPlaylist);
		delete = new Button("Delete playlist", () -> {
			if (!ALL.equals(view)) {
				PlaylistStore.delete(view);
				view = ALL;
				lastSig = null;
			}
		}).danger(true);
		rebuildFixed();
	}

	private void createPlaylist() {
		String name = nameField.getText();
		if (PlaylistStore.create(name)) {
			view = name.replaceAll("[^\\p{L}\\p{N} _-]", "").trim();
			nameField.clear();
			lastSig = null;
			NotificationManager.push("Playlist created", view, Icons.MUSIC);
		} else {
			NotificationManager.push("Playlist", "Invalid or duplicate name", Icons.MUSIC);
		}
	}

	private void rebuildFixed() {
		comps.clear();
		comps.add(prev);
		comps.add(play);
		comps.add(next);
		comps.add(shuffle);
		comps.add(repeat);
		comps.add(volume);
		comps.add(showPanel);
		comps.add(nameField);
		comps.add(create);
		comps.add(delete);
		comps.add(search);
		comps.add(style);
		comps.add(viewDrop);
		comps.add(targetDrop);
	}

	@Override
	public String title() {
		return "Music";
	}

	@Override
	public void onShow() {
		super.onShow();
		lastSig = null;
	}

	private static List<Track> filter(List<Track> in, String q) {
		String s = q.toLowerCase(Locale.ROOT).trim();
		if (s.isEmpty()) {
			return in;
		}
		List<Track> out = new ArrayList<>();
		for (Track t : in) {
			if (t.title().toLowerCase(Locale.ROOT).contains(s) || t.artist().toLowerCase(Locale.ROOT).contains(s)) {
				out.add(t);
			}
		}
		return out;
	}

	@Override
	protected void update() {
		List<String> names = PlaylistStore.names();
		boolean playlist = !ALL.equals(view);
		String sig = MusicLibrary.version() + "|" + search.getText() + "|" + view + "|" + String.join(",", names)
				+ "|" + (playlist ? PlaylistStore.tracks(view).size() : -1);
		if (sig.equals(lastSig)) {
			return;
		}
		lastSig = sig;

		viewOptions.clear();
		viewOptions.add(ALL);
		viewOptions.addAll(names);
		if (!viewOptions.contains(view)) {
			view = ALL;
			playlist = false;
		}
		targetOptions.clear();
		targetOptions.addAll(names);
		if (!names.contains(target)) {
			target = names.isEmpty() ? "-" : names.get(0);
		}

		rebuildFixed();
		List<Track> base = playlist ? PlaylistStore.tracks(view) : MusicLibrary.tracks();
		List<Track> list = filter(base, search.getText());
		final List<Track> ctx = List.copyOf(list);
		for (Track t : list) {
			Runnable onPlay = () -> player.play(t, ctx);
			if (playlist) {
				comps.add(new TrackRow(t, onPlay, "x", () -> PlaylistStore.remove(view, t)));
			} else if (!names.isEmpty()) {
				comps.add(new TrackRow(t, onPlay, "+", () -> {
					if (names.contains(target)) {
						PlaylistStore.add(target, t);
						ConfigManager.get().noteChange("Added to " + target + ": " + t.title());
						NotificationManager.push("Added to playlist", target + ": " + t.title(), Icons.MUSIC);
					}
				}));
			} else {
				comps.add(new TrackRow(t, onPlay, null, null));
			}
		}
		rowCount = list.size();
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		panelY = top;
		int by = top + 58;
		int bw = (usable - 12) / 3;
		prev.bounds(x, by, bw, 22);
		play.bounds(x + bw + 6, by, bw, 22);
		next.bounds(x + 2 * (bw + 6), by, bw, 22);
		int hw = (usable - 6) / 2;
		shuffle.bounds(x, by + 28, hw, 22);
		repeat.bounds(x + hw + 6, by + 28, hw, 22);
		volume.bounds(x, by + 58, usable, 26);
		showPanel.bounds(x, by + 90, usable, 22);
		style.bounds(x, by + 118, usable, 22);
		viewDrop.bounds(x, by + 146, usable, 22);
		targetDrop.bounds(x, by + 174, usable, 22);
		nameField.bounds(x, by + 204, usable, 20);
		create.bounds(x, by + 230, hw, 22);
		delete.bounds(x + hw + 6, by + 230, hw, 22);
		search.bounds(x, by + 260, usable, 20);
		listY = by + 288;
		int cy = listY;
		for (var comp : comps) {
			if (comp instanceof TrackRow) {
				comp.bounds(x, cy, usable, ROW_H);
				cy += ROW_H + GAP;
			}
		}
		return Math.max(cy, listY + 60);
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		int usable = w - 10;
		int r = Math.min(Gfx.radius(), 12);
		Gfx.glassPanel(c, x, panelY, usable, 52, r, 0.4f);

		Track t = player.current();
		String err = player.lastError();
		if (!err.isEmpty()) {
			Gfx.text(c, Gfx.fit("Error: " + err, usable - 24), x + 12, panelY + 8, 0xFFFF6B6B);
			Gfx.text(c, Gfx.fit(t == null ? "" : t.title(), usable - 24), x + 12, panelY + 22, ThemeManager.dim());
		} else if (t == null) {
			Gfx.text(c, "Nothing playing", x + 12, panelY + 12, ThemeManager.text());
			Gfx.text(c, Gfx.fit("Put mp3 / ogg / wav files into config/nevavis/music/", usable - 24), x + 12, panelY + 26, ThemeManager.dim());
		} else {
			Gfx.text(c, Gfx.fit(t.title(), usable - 110), x + 12, panelY + 8, ThemeManager.text());
			Gfx.text(c, Gfx.fit(t.artist(), usable - 110), x + 12, panelY + 20, ThemeManager.dim());
			String backend = player.backend().isEmpty() ? "" : "  [" + player.backend() + "]";
			String time = TrackRow.time(player.positionMs()) + " / " + TrackRow.time(player.durationMs()) + backend;
			Gfx.text(c, Gfx.fit(time, usable - 110), x + 12, panelY + 32, ThemeManager.dim());
		}
		int px = x + 12, pw = usable - 24, py = panelY + 45;
		Gfx.roundedRect(c, px, py, pw, 3, 1, Colors.withAlpha(ThemeManager.dim(), 0.3f));
		long dur = player.durationMs();
		float prog = dur > 0 ? Math.min(1f, player.positionMs() / (float) dur) : 0f;
		if (prog > 0f) {
			Gfx.roundedGradient(c, px, py, Math.max(2, Math.round(pw * prog)), 3, 1, Gfx.accent(1f), Gfx.accent2(1f));
		}
		float[] b = player.bands();
		int ex = x + usable - 12 - 8 * 6;
		for (int i = 0; i < 8; i++) {
			shown[i] += (b[i] - shown[i]) * Math.min(1f, dt * 18f);
			int bh = 2 + Math.round(shown[i] * 24f);
			Gfx.roundedGradient(c, ex + i * 6, panelY + 40 - bh, 4, bh, 1, Gfx.accent(0.95f), Gfx.accent2(0.95f));
		}

		if (rowCount == 0) {
			String msg = search.getText().isEmpty()
					? (ALL.equals(view) ? "No tracks found. Folder: config/nevavis/music/" : "Playlist is empty. Pick \"All tracks\" and press +")
					: "Nothing found";
			Gfx.centered(c, Gfx.fit(msg, usable - 20), x + usable / 2, listY + 12, ThemeManager.dim());
		}
	}
}
