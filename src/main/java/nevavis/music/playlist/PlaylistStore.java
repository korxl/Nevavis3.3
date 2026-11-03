package nevavis.music.playlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import nevavis.core.NevaVis;
import nevavis.music.MusicLibrary;
import nevavis.music.Track;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Плейлисты: config/nevavis/playlists.json (имя → список файлов относительно папки музыки). */
public final class PlaylistStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type TYPE = new TypeToken<LinkedHashMap<String, List<String>>>() {
	}.getType();

	private static Map<String, List<String>> data;

	private PlaylistStore() {
	}

	private static Path file() {
		return NevaVis.configDir().resolve("playlists.json");
	}

	private static Map<String, List<String>> data() {
		if (data == null) {
			data = new LinkedHashMap<>();
			try {
				Path f = file();
				if (Files.exists(f)) {
					try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
						Map<String, List<String>> loaded = GSON.fromJson(r, TYPE);
						if (loaded != null) {
							data = loaded;
						}
					}
				}
			} catch (Exception e) {
				NevaVis.LOGGER.error("Cannot read playlists", e);
			}
		}
		return data;
	}

	private static void save() {
		try (Writer w = Files.newBufferedWriter(file(), StandardCharsets.UTF_8)) {
			GSON.toJson(data(), TYPE, w);
		} catch (Exception e) {
			NevaVis.LOGGER.error("Cannot save playlists", e);
		}
	}

	public static List<String> names() {
		return new ArrayList<>(data().keySet());
	}

	public static boolean create(String name) {
		String n = name == null ? "" : name.replaceAll("[^\\p{L}\\p{N} _-]", "").trim();
		if (n.isEmpty() || n.length() > 24 || data().containsKey(n)) {
			return false;
		}
		data().put(n, new ArrayList<>());
		save();
		return true;
	}

	public static void delete(String name) {
		if (data().remove(name) != null) {
			save();
		}
	}

	public static void add(String name, Track t) {
		List<String> l = data().get(name);
		if (l != null && !l.contains(t.file())) {
			l.add(t.file());
			save();
		}
	}

	public static void remove(String name, Track t) {
		List<String> l = data().get(name);
		if (l != null && l.remove(t.file())) {
			save();
		}
	}

	/** Треки плейлиста, найденные в библиотеке (отсутствующие файлы пропускаются). */
	public static List<Track> tracks(String name) {
		List<String> files = data().get(name);
		List<Track> out = new ArrayList<>();
		if (files == null) {
			return out;
		}
		for (String f : files) {
			for (Track t : MusicLibrary.tracks()) {
				if (t.file().equals(f)) {
					out.add(t);
					break;
				}
			}
		}
		return out;
	}
}
