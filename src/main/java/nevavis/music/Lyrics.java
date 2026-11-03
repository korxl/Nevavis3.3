package nevavis.music;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Текст песни из файла рядом с треком: song.lrc (с таймкодами) или song.txt (строки равномерно по длительности).
 * Тексты не поставляются с модом — кладёшь свои файлы в папку музыки.
 */
public final class Lyrics {
	private static final Pattern STAMP = Pattern.compile("\\[(\\d+):(\\d+(?:[.:]\\d+)?)]");
	private static Track cachedFor;
	private static Lyrics cached;

	private final List<Long> times = new ArrayList<>();
	private final List<String> lines = new ArrayList<>();

	public int size() {
		return lines.size();
	}

	public String line(int i) {
		return i < 0 || i >= lines.size() ? "" : lines.get(i);
	}

	/** Индекс текущей строки или -1 до первой. */
	public int indexAt(long posMs) {
		int idx = -1;
		for (int i = 0; i < times.size(); i++) {
			if (times.get(i) <= posMs) {
				idx = i;
			} else {
				break;
			}
		}
		return idx;
	}

	public static synchronized Lyrics of(Track t) {
		if (t == null) {
			return null;
		}
		if (t != cachedFor) {
			cachedFor = t;
			cached = load(t);
		}
		return cached;
	}

	private static Lyrics load(Track t) {
		String base = t.path().getFileName().toString();
		int dot = base.lastIndexOf('.');
		if (dot > 0) {
			base = base.substring(0, dot);
		}
		Path dir = t.path().getParent();
		if (dir == null) {
			return null;
		}
		try {
			Path lrc = dir.resolve(base + ".lrc");
			if (Files.exists(lrc)) {
				return parseLrc(Files.readAllLines(lrc, StandardCharsets.UTF_8));
			}
			Path txt = dir.resolve(base + ".txt");
			if (Files.exists(txt)) {
				return parseTxt(Files.readAllLines(txt, StandardCharsets.UTF_8), t.durationMs());
			}
		} catch (IOException | RuntimeException ignored) {
			// битый файл — просто без текста
		}
		return null;
	}

	private static Lyrics parseLrc(List<String> src) {
		List<long[]> idx = new ArrayList<>();
		List<String> texts = new ArrayList<>();
		for (String raw : src) {
			Matcher m = STAMP.matcher(raw);
			List<Long> stamps = new ArrayList<>();
			int end = 0;
			while (m.find() && m.start() == end) {
				double sec = Double.parseDouble(m.group(2).replace(':', '.'));
				stamps.add((long) (Long.parseLong(m.group(1)) * 60000L + sec * 1000));
				end = m.end();
			}
			String text = raw.substring(end).trim();
			for (long s : stamps) {
				idx.add(new long[]{s, texts.size()});
				texts.add(text);
			}
		}
		idx.sort((a, b) -> Long.compare(a[0], b[0]));
		Lyrics l = new Lyrics();
		for (long[] e : idx) {
			l.times.add(e[0]);
			l.lines.add(texts.get((int) e[1]));
		}
		return l.size() == 0 ? null : l;
	}

	private static Lyrics parseTxt(List<String> src, long durationMs) {
		List<String> clean = new ArrayList<>();
		for (String s : src) {
			if (!s.isBlank()) {
				clean.add(s.trim());
			}
		}
		if (clean.isEmpty()) {
			return null;
		}
		long per = durationMs > 0 ? durationMs / clean.size() : 4000L;
		Lyrics l = new Lyrics();
		for (int i = 0; i < clean.size(); i++) {
			l.times.add(i * per);
			l.lines.add(clean.get(i));
		}
		return l;
	}
}
