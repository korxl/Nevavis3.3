package nevavis.profiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.core.NevaVis;
import nevavis.modules.ModuleManager;
import nevavis.notifications.NotificationManager;
import nevavis.render.Icons;
import nevavis.themes.ThemeManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/** Профили настроек в JSON: config/nevavis/profiles/&lt;имя&gt;.json (включают модули, HUD, частицы, прицел, тему, музыку). */
public final class ProfileManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ProfileManager() {
	}

	private static NevaVisConfig cfg() {
		return ConfigManager.get().data();
	}

	private static Path dir() {
		return NevaVis.configDir().resolve("profiles");
	}

	private static Path file(String name) {
		return dir().resolve(name + ".json");
	}

	/** Допустимое имя профиля или null. */
	public static String sanitize(String raw) {
		if (raw == null) {
			return null;
		}
		String n = raw.replaceAll("[^A-Za-z0-9 _-]", "").trim();
		if (n.isEmpty() || n.length() > 24) {
			return null;
		}
		return n;
	}

	public static void init() {
		try {
			Files.createDirectories(dir());
			if (!Files.exists(file(cfg().activeProfile))) {
				write(cfg().activeProfile, cfg());
			}
		} catch (IOException e) {
			NevaVis.LOGGER.error("Profiles init failed", e);
		}
	}

	public static List<String> names() {
		List<String> out = new ArrayList<>();
		try (Stream<Path> s = Files.list(dir())) {
			s.filter(p -> p.getFileName().toString().endsWith(".json")).forEach(p -> {
				String n = p.getFileName().toString();
				out.add(n.substring(0, n.length() - 5));
			});
		} catch (IOException ignored) {
			// папки ещё нет
		}
		Collections.sort(out);
		return out;
	}

	public static String active() {
		return cfg().activeProfile;
	}

	private static void write(String name, NevaVisConfig data) throws IOException {
		Files.createDirectories(dir());
		Files.writeString(file(name), GSON.toJson(data), StandardCharsets.UTF_8);
	}

	private static NevaVisConfig read(String name) throws IOException {
		NevaVisConfig c = GSON.fromJson(Files.readString(file(name), StandardCharsets.UTF_8), NevaVisConfig.class);
		if (c == null) {
			throw new IOException("empty profile");
		}
		return c;
	}

	private static void toast(String title, String desc) {
		NotificationManager.push(title, desc, Icons.PROFILES);
	}

	private static String unique(String base) {
		String n = base;
		int i = 2;
		while (Files.exists(file(n))) {
			n = base + " " + i++;
		}
		return n;
	}

	/** Сохраняет текущие настройки в активный профиль. */
	public static void saveCurrent(boolean notify) {
		try {
			ConfigManager.get().save();
			write(cfg().activeProfile, cfg());
			if (notify) {
				toast("Profile Saved", cfg().activeProfile);
			}
		} catch (IOException e) {
			NevaVis.LOGGER.error("Profile save failed", e);
		}
	}

	public static boolean create(String raw) {
		String name = sanitize(raw);
		if (name == null || Files.exists(file(name))) {
			toast("Profile", "Invalid or duplicate name");
			return false;
		}
		try {
			NevaVisConfig fresh = new NevaVisConfig();
			fresh.activeProfile = name;
			write(name, fresh);
			toast("Profile created", name);
			return true;
		} catch (IOException e) {
			NevaVis.LOGGER.error("Profile create failed", e);
			return false;
		}
	}

	public static void switchTo(String name) {
		if (name.equals(cfg().activeProfile)) {
			return;
		}
		try {
			NevaVisConfig loaded = read(name);
			saveCurrent(false);
			cfg().copyFrom(loaded);
			cfg().activeProfile = name;
			if (cfg().enabledModules == null) {
				cfg().enabledModules = new ArrayList<>();
			}
			ThemeManager.applyByName(cfg().theme);
			ModuleManager.init();
			ConfigManager.get().save();
			toast("Profile Loaded", name);
		} catch (Exception e) {
			NevaVis.LOGGER.error("Profile switch failed", e);
			toast("Profile", "Cannot load " + name);
		}
	}

	public static void delete(String name) {
		if (name.equals(cfg().activeProfile) || names().size() <= 1) {
			toast("Profile", "Cannot delete the active profile");
			return;
		}
		try {
			Files.deleteIfExists(file(name));
			toast("Profile deleted", name);
		} catch (IOException e) {
			NevaVis.LOGGER.error("Profile delete failed", e);
		}
	}

	public static void rename(String name, String raw) {
		String n = sanitize(raw);
		if (n == null || Files.exists(file(n))) {
			toast("Profile", "Invalid or duplicate name");
			return;
		}
		try {
			boolean isActive = name.equals(cfg().activeProfile);
			if (isActive) {
				saveCurrent(false);
			}
			Files.move(file(name), file(n));
			if (isActive) {
				cfg().activeProfile = n;
				write(n, cfg());
				ConfigManager.get().save();
			}
			toast("Profile renamed", name + " -> " + n);
		} catch (IOException e) {
			NevaVis.LOGGER.error("Profile rename failed", e);
		}
	}

	public static void duplicate(String name) {
		try {
			if (name.equals(cfg().activeProfile)) {
				saveCurrent(false);
			}
			String copy = unique(name + " Copy");
			Files.copy(file(name), file(copy));
			toast("Profile duplicated", copy);
		} catch (IOException e) {
			NevaVis.LOGGER.error("Profile duplicate failed", e);
		}
	}

	/** Копирует JSON профиля в буфер обмена и в config/nevavis/profiles/exports/. */
	public static void export(String name) {
		try {
			if (name.equals(cfg().activeProfile)) {
				saveCurrent(false);
			}
			String json = Files.readString(file(name), StandardCharsets.UTF_8);
			Path out = dir().resolve("exports");
			Files.createDirectories(out);
			Files.writeString(out.resolve(name + ".json"), json, StandardCharsets.UTF_8);
			MinecraftClient.getInstance().keyboard.setClipboard(json);
			toast("Profile exported", "Copied to clipboard + exports/");
		} catch (IOException e) {
			NevaVis.LOGGER.error("Profile export failed", e);
		}
	}

	/** Создаёт профиль из JSON в буфере обмена. */
	public static void importFromClipboard() {
		try {
			String json = MinecraftClient.getInstance().keyboard.getClipboard();
			NevaVisConfig c = json == null ? null : GSON.fromJson(json, NevaVisConfig.class);
			if (c == null) {
				toast("Import failed", "Clipboard has no profile JSON");
				return;
			}
			String base = sanitize(c.activeProfile);
			String name = unique(base == null ? "Imported" : base);
			c.activeProfile = name;
			write(name, c);
			toast("Profile imported", name);
		} catch (Exception e) {
			toast("Import failed", "Clipboard has no valid profile JSON");
		}
	}
}
