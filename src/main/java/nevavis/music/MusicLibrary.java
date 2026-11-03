package nevavis.music;

import nevavis.core.NevaVis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/** Библиотека треков из config/nevavis/music/. Новые файлы подхватываются автоматически (раз в 3 с). */
public final class MusicLibrary {
	private static volatile List<Track> tracks = List.of();
	private static volatile int version;
	private static String lastSignature = "";
	private static ScheduledExecutorService executor;

	private MusicLibrary() {
	}

	public static List<Track> tracks() {
		return tracks;
	}

	/** Увеличивается при каждом изменении списка (для обновления UI). */
	public static int version() {
		return version;
	}

	public static void init() {
		try {
			Files.createDirectories(NevaVis.musicDir());
		} catch (IOException e) {
			NevaVis.LOGGER.error("Cannot create music folder", e);
		}
		executor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "NevaVis-MusicScan");
			t.setDaemon(true);
			return t;
		});
		executor.scheduleWithFixedDelay(MusicLibrary::scanSafe, 0, 3, TimeUnit.SECONDS);
	}

	private static void scanSafe() {
		try {
			scan();
		} catch (Throwable t) {
			NevaVis.LOGGER.error("Music scan failed", t);
		}
	}

	private static void scan() throws IOException {
		Path root = NevaVis.musicDir();
		List<Path> files;
		try (Stream<Path> s = Files.walk(root, 3)) {
			files = s.filter(Files::isRegularFile).filter(Decoders::isSupported).sorted().toList();
		}
		StringBuilder sig = new StringBuilder();
		for (Path f : files) {
			sig.append(f).append('|').append(Files.getLastModifiedTime(f).toMillis()).append('|').append(Files.size(f)).append(';');
		}
		String signature = sig.toString();
		if (signature.equals(lastSignature)) {
			return;
		}
		lastSignature = signature;

		List<Track> list = new ArrayList<>();
		for (Path f : files) {
			list.add(makeTrack(root, f));
		}
		tracks = List.copyOf(list);
		version++;
		MusicPlayer.get().onLibraryChanged();
		NevaVis.LOGGER.info("Music library: {} tracks", list.size());
	}

	private static Track makeTrack(Path root, Path f) {
		String rel = root.relativize(f).toString().replace('\\', '/');
		String name = f.getFileName().toString();
		int dot = name.lastIndexOf('.');
		String base = dot > 0 ? name.substring(0, dot) : name;
		String artist = "Unknown artist";
		String title = base;
		int sep = base.indexOf(" - ");
		if (sep > 0 && sep < base.length() - 3) {
			artist = base.substring(0, sep).trim();
			title = base.substring(sep + 3).trim();
		}
		return new Track(f, rel, title, artist, Decoders.quickDurationMs(f));
	}

	public static List<Track> search(String query) {
		String q = query.toLowerCase(Locale.ROOT).trim();
		if (q.isEmpty()) {
			return tracks;
		}
		List<Track> out = new ArrayList<>();
		for (Track t : tracks) {
			if (t.title().toLowerCase(Locale.ROOT).contains(q) || t.artist().toLowerCase(Locale.ROOT).contains(q)) {
				out.add(t);
			}
		}
		return out;
	}
}
