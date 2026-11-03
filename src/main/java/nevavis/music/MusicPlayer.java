package nevavis.music;

import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.core.NevaVis;
import nevavis.notifications.NotificationManager;
import nevavis.render.Icons;

import java.util.List;
import java.util.Random;

/**
 * Музыкальный плеер. Звук играет в отдельном потоке (NevaVis-Audio) через javax.sound,
 * поэтому не зависит от открытого GUI и не блокирует игровой поток.
 */
public final class MusicPlayer {
	private static final MusicPlayer INSTANCE = new MusicPlayer();
	private static final float[] BAND_FREQ = {60f, 150f, 300f, 600f, 1200f, 2400f, 4800f, 9600f};
	private static final int FFT_N = 512;

	public static MusicPlayer get() {
		return INSTANCE;
	}

	private final Random random = new Random();
	private final float[] bands = new float[8];

	private volatile Track current;
	private volatile boolean playing;
	private volatile boolean paused;
	private volatile long durationMs;
	private volatile long positionMs;
	private volatile long changedAt;
	private volatile int token;
	private volatile List<Track> queue = List.of();
	private volatile int index = -1;
	private volatile String lastError = "";
	private volatile String backend = "";
	private boolean restored;

	private MusicPlayer() {
	}

	private static NevaVisConfig.MusicConfig cfg() {
		return ConfigManager.get().data().music;
	}

	// ---------- состояние для UI ----------

	public Track current() {
		return current;
	}

	/** Идёт воспроизведение (не пауза). */
	public boolean isPlaying() {
		return playing && !paused;
	}

	/** Есть активный трек (играет или на паузе). */
	public boolean isActive() {
		return playing;
	}

	public long positionMs() {
		return positionMs;
	}

	public long durationMs() {
		return durationMs;
	}

	/** Время последнего события (старт/пауза/смена) — для показа HUD. */
	public long changedAt() {
		return changedAt;
	}

	public float[] bands() {
		return bands;
	}

	/** Текст последней ошибки воспроизведения (пусто, если всё хорошо). */
	public String lastError() {
		return lastError;
	}

	/** Какой вывод звука используется: JavaSound или OpenAL. */
	public String backend() {
		return backend;
	}

	public float volume() {
		return cfg().volume;
	}

	public void setVolume(float v) {
		cfg().volume = Math.max(0f, Math.min(1f, v));
	}

	public boolean shuffle() {
		return cfg().shuffle;
	}

	public void toggleShuffle() {
		cfg().shuffle = !cfg().shuffle;
	}

	public String repeat() {
		return cfg().repeat;
	}

	public void cycleRepeat() {
		cfg().repeat = switch (cfg().repeat) {
			case "Off" -> "All";
			case "All" -> "One";
			default -> "Off";
		};
	}

	// ---------- управление ----------

	public synchronized void play(Track t) {
		play(t, MusicLibrary.tracks());
	}

	/** Воспроизвести трек; next/previous/shuffle работают внутри переданного списка (библиотека или плейлист). */
	public synchronized void play(Track t, List<Track> context) {
		List<Track> ctx = context.isEmpty() ? MusicLibrary.tracks() : List.copyOf(context);
		queue = ctx;
		index = indexOf(ctx, t);
		start(t);
	}

	/** Play / Pause. Если ничего не выбрано — запускает первый трек библиотеки. */
	public synchronized void togglePause() {
		if (current == null) {
			List<Track> lib = MusicLibrary.tracks();
			if (!lib.isEmpty()) {
				play(lib.get(0));
			}
			return;
		}
		if (!playing) {
			start(current);
			return;
		}
		paused = !paused;
		changedAt = System.currentTimeMillis();
		NotificationManager.push(paused ? "Music Paused" : "Music Started", current.title(), Icons.MUSIC);
	}

	public synchronized void next() {
		advance(false, true);
	}

	public synchronized void previous() {
		if (positionMs > 3000 && current != null) {
			start(current);
			return;
		}
		advance(false, false);
	}

	public synchronized void stop() {
		token++;
		playing = false;
		paused = false;
		positionMs = 0;
		changedAt = System.currentTimeMillis();
	}

	private void advance(boolean auto, boolean forward) {
		List<Track> q = queue;
		if (q.isEmpty()) {
			q = MusicLibrary.tracks();
			queue = q;
		}
		if (q.isEmpty()) {
			playing = false;
			return;
		}
		String rep = cfg().repeat;
		if (auto && "One".equals(rep) && current != null) {
			start(current);
			return;
		}
		int n = q.size();
		int idx;
		if (cfg().shuffle && n > 1) {
			do {
				idx = random.nextInt(n);
			} while (idx == index);
		} else {
			idx = index + (forward ? 1 : -1);
		}
		if (idx >= n) {
			if (auto && !"All".equals(rep)) {
				playing = false;
				positionMs = 0;
				changedAt = System.currentTimeMillis();
				return;
			}
			idx = 0;
		}
		if (idx < 0) {
			idx = n - 1;
		}
		index = idx;
		start(q.get(idx));
	}

	private static int indexOf(List<Track> list, Track t) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).file().equals(t.file())) {
				return i;
			}
		}
		return -1;
	}

	/** Вызывается сканером при изменении библиотеки: один раз восстанавливает последний трек. */
	public synchronized void onLibraryChanged() {
		List<Track> lib = MusicLibrary.tracks();
		if (!playing) {
			queue = lib;
		}
		if (!restored && !lib.isEmpty()) {
			restored = true;
			String last = cfg().lastTrack;
			if (current == null && last != null && !last.isEmpty()) {
				for (Track t : lib) {
					if (t.file().equals(last)) {
						current = t;
						durationMs = t.durationMs();
						index = indexOf(lib, t);
						break;
					}
				}
			}
		}
	}

	private void start(Track t) {
		final int my = ++token;
		current = t;
		playing = true;
		paused = false;
		positionMs = 0;
		durationMs = t.durationMs();
		changedAt = System.currentTimeMillis();
		cfg().lastTrack = t.file();
		lastError = "";
		Thread th = new Thread(() -> run(t, my), "NevaVis-Audio");
		th.setDaemon(true);
		th.setContextClassLoader(MusicPlayer.class.getClassLoader());
		th.start();
		NotificationManager.push("Music Started", t.title() + " - " + t.artist(), Icons.MUSIC);
	}

	// ---------- поток воспроизведения ----------

	private AudioSink openSink(int rate, int ch) throws Exception {
		try {
			AudioSink s = new JavaSoundSink(rate, ch);
			backend = "JavaSound";
			return s;
		} catch (Throwable first) {
			NevaVis.LOGGER.warn("JavaSound unavailable ({}), falling back to OpenAL", first.toString());
			try {
				AudioSink s = new AlSink(rate, ch);
				backend = "OpenAL";
				return s;
			} catch (Throwable second) {
				throw new Exception("JavaSound: " + describe(first) + " | OpenAL: " + describe(second));
			}
		}
	}

	private static String describe(Throwable e) {
		String m = e.getMessage();
		return e.getClass().getSimpleName() + (m == null ? "" : ": " + m);
	}

	private void run(Track t, int my) {
		boolean finished = false;
		PcmStream stream = null;
		AudioSink sink = null;
		try {
			stream = Decoders.open(t.path());
			if (stream.durationMs() > 0) {
				durationMs = stream.durationMs();
			}
			final int rate = stream.sampleRate();
			final int ch = stream.channels();
			sink = openSink(rate, ch);

			boolean sinkPaused = false;
			short[] chunk;
			while (token == my && (chunk = stream.next()) != null) {
				while (paused && token == my) {
					if (!sinkPaused) {
						sink.pause();
						sinkPaused = true;
					}
					Thread.sleep(25);
				}
				if (token != my) {
					break;
				}
				if (sinkPaused) {
					sink.resume();
					sinkPaused = false;
				}
				analyze(chunk, ch, rate);
				float v = volume();
				byte[] bytes = toBytes(chunk, v * v);
				sink.write(bytes, bytes.length);
				positionMs = sink.positionMs();
			}
			if (token == my) {
				sink.drain();
				finished = true;
			} else {
				sink.flush();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Throwable e) {
			String msg = describe(e);
			NevaVis.LOGGER.error("Playback failed: {}", t.file(), e);
			if (token == my) {
				playing = false;
				lastError = msg;
				NotificationManager.push("Playback error", msg, Icons.MUSIC);
			}
		} finally {
			if (sink != null) {
				try {
					sink.close();
				} catch (Throwable ignored) {
					// ничего
				}
			}
			if (stream != null) {
				stream.close();
			}
			for (int i = 0; i < bands.length; i++) {
				bands[i] = 0f;
			}
		}
		if (finished && token == my) {
			synchronized (this) {
				if (token == my) {
					advance(true, true);
				}
			}
		}
	}

	private static byte[] toBytes(short[] chunk, float gain) {
		byte[] out = new byte[chunk.length * 2];
		for (int i = 0; i < chunk.length; i++) {
			int v = Math.round(chunk[i] * gain);
			if (v > 32767) {
				v = 32767;
			} else if (v < -32768) {
				v = -32768;
			}
			out[2 * i] = (byte) v;
			out[2 * i + 1] = (byte) (v >> 8);
		}
		return out;
	}

	/** Реальный спектр: алгоритм Герцеля по 8 частотам на окне из 512 кадров. */
	private void analyze(short[] chunk, int ch, int rate) {
		int frames = Math.min(FFT_N, chunk.length / ch);
		if (frames < 64) {
			return;
		}
		float[] mono = new float[frames];
		for (int i = 0; i < frames; i++) {
			float s = 0f;
			for (int c = 0; c < ch; c++) {
				s += chunk[i * ch + c];
			}
			mono[i] = s / ch;
		}
		for (int b = 0; b < BAND_FREQ.length; b++) {
			if (BAND_FREQ[b] >= rate / 2f) {
				bands[b] *= 0.8f;
				continue;
			}
			double k = BAND_FREQ[b] * frames / rate;
			double coeff = 2.0 * Math.cos(2.0 * Math.PI * k / frames);
			double s1 = 0, s2 = 0;
			for (int i = 0; i < frames; i++) {
				double s0 = mono[i] + coeff * s1 - s2;
				s2 = s1;
				s1 = s0;
			}
			double mag = Math.sqrt(Math.max(0.0, s1 * s1 + s2 * s2 - coeff * s1 * s2));
			float level = (float) Math.min(1.0, mag / (frames * 0.5) / 5000.0);
			bands[b] = Math.max(level, bands[b] * 0.8f);
		}
	}
}
