package nevavis.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nevavis.core.NevaVis;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Загрузка и сохранение config/nevavis/config.json + журнал последних изменений. */
public final class ConfigManager {
	private static final ConfigManager INSTANCE = new ConfigManager();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final int RECENT_MAX = 6;

	private NevaVisConfig config = new NevaVisConfig();
	private final Deque<String> recent = new ArrayDeque<>();

	private ConfigManager() {
	}

	public static ConfigManager get() {
		return INSTANCE;
	}

	public NevaVisConfig data() {
		return config;
	}

	private Path file() {
		return NevaVis.configDir().resolve("config.json");
	}

	public void load() {
		try {
			Files.createDirectories(NevaVis.configDir());
			Files.createDirectories(NevaVis.musicDir());
			Path f = file();
			if (Files.exists(f)) {
				try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
					NevaVisConfig loaded = GSON.fromJson(r, NevaVisConfig.class);
					if (loaded != null) {
						config = loaded;
						if (config.enabledModules == null) {
							config.enabledModules = new ArrayList<>();
						}
					}
				}
			} else {
				save();
			}
		} catch (Exception e) {
			NevaVis.LOGGER.error("Failed to load config, using defaults", e);
			config = new NevaVisConfig();
		}
	}

	public void save() {
		try {
			Files.createDirectories(NevaVis.configDir());
			try (Writer w = Files.newBufferedWriter(file(), StandardCharsets.UTF_8)) {
				GSON.toJson(config, w);
			}
		} catch (IOException e) {
			NevaVis.LOGGER.error("Failed to save config", e);
		}
	}

	/** Коэффициент нагрузки эффектов: 0.5 при Low Performance / FPS Optimization, иначе 1. */
	public float perf() {
		return config.lowPerformance || config.fpsOptimization ? 0.5f : 1f;
	}

	/** Записывает событие в список «Recent changes» на главной странице. */
	public void noteChange(String text) {
		recent.addFirst(LocalTime.now().format(TIME) + "  " + text);
		while (recent.size() > RECENT_MAX) {
			recent.removeLast();
		}
	}

	public List<String> recent() {
		return new ArrayList<>(recent);
	}
}
