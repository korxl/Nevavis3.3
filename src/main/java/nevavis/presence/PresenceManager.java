package nevavis.presence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.core.NevaVis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Онлайн-присутствие: раз в 10 минут отправляет на ваш сервер (Cloudflare Worker из папки server/)
 * свой UUID и ник, раз в 2 минуты запрашивает список игроков с модом. Без адреса сервера ничего не отправляется.
 */
public final class PresenceManager {
	private static final long PING_EVERY_MS = 10 * 60 * 1000L;

	private static volatile Set<String> names = Set.of();
	private static volatile int count = -1;
	private static volatile String status = "Backend URL not set";
	private static long lastPing;
	private static ScheduledExecutorService executor;

	private PresenceManager() {
	}

	public static void init() {
		executor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "NevaVis-Presence");
			t.setDaemon(true);
			return t;
		});
		executor.scheduleWithFixedDelay(PresenceManager::tickSafe, 5, 120, TimeUnit.SECONDS);
	}

	/** Сбросить таймер (после смены адреса) — следующий цикл отправит ping сразу. */
	public static void resetTimer() {
		lastPing = 0L;
	}

	/** Строка для карточки на Home. */
	public static String statusText() {
		if (count >= 0) {
			return count + " online";
		}
		return status;
	}

	public static String status() {
		return status;
	}

	/** Есть ли у игрока с таким ником значок (он онлайн с модом). */
	public static boolean isUser(String name) {
		if (name == null) {
			return false;
		}
		if (selfName().equalsIgnoreCase(name)) {
			return true;
		}
		return names.contains(name.toLowerCase());
	}

	public static Set<String> users() {
		return names;
	}

	public static String selfName() {
		try {
			return MinecraftClient.getInstance().getSession().getUsername();
		} catch (Throwable t) {
			return "";
		}
	}

	private static void tickSafe() {
		try {
			tick();
		} catch (Throwable t) {
			status = "Error: " + t.getClass().getSimpleName();
			NevaVis.LOGGER.warn("Presence update failed: {}", t.toString());
		}
	}

	private static void tick() throws IOException {
		NevaVisConfig cfg = ConfigManager.get().data();
		String base = cfg.presenceUrl == null ? "" : cfg.presenceUrl.trim();
		if (base.isEmpty()) {
			status = "Backend URL not set";
			count = -1;
			names = Set.of();
			return;
		}
		while (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		if (!base.startsWith("https://")) {
			status = "URL must start with https://";
			count = -1;
			return;
		}
		long now = System.currentTimeMillis();
		if (cfg.presenceEnabled && now - lastPing > PING_EVERY_MS) {
			MinecraftClient mc = MinecraftClient.getInstance();
			var uuid = mc.getSession().getUuidOrNull();
			String name = selfName();
			if (uuid != null && !name.isEmpty()) {
				JsonObject body = new JsonObject();
				body.addProperty("uuid", uuid.toString());
				body.addProperty("name", name);
				http("POST", base + "/ping", body.toString());
				lastPing = now;
			}
		}
		String json = http("GET", base + "/online", null);
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
		Set<String> set = new HashSet<>();
		JsonArray players = root.has("players") ? root.getAsJsonArray("players") : new JsonArray();
		for (JsonElement e : players) {
			JsonObject o = e.getAsJsonObject();
			if (o.has("name")) {
				set.add(o.get("name").getAsString().toLowerCase());
			}
		}
		names = Set.copyOf(set);
		count = root.has("count") ? root.get("count").getAsInt() : set.size();
		status = count + " online";
	}

	private static String http(String method, String url, String body) throws IOException {
		HttpURLConnection c = (HttpURLConnection) URI.create(url).toURL().openConnection();
		c.setRequestMethod(method);
		c.setConnectTimeout(5000);
		c.setReadTimeout(5000);
		c.setRequestProperty("User-Agent", "NevaVis-Client");
		if (body != null) {
			c.setDoOutput(true);
			c.setRequestProperty("Content-Type", "application/json");
			try (OutputStream o = c.getOutputStream()) {
				o.write(body.getBytes(StandardCharsets.UTF_8));
			}
		}
		int code = c.getResponseCode();
		if (code >= 400) {
			throw new IOException("HTTP " + code);
		}
		try (InputStream in = c.getInputStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
