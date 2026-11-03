package nevavis.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import nevavis.core.NevaVis;
import nevavis.hud.elements.ArmorHud;
import nevavis.hud.elements.KeystrokesHud;
import nevavis.hud.elements.TextHud;
import nevavis.modules.Module;
import nevavis.modules.ModuleManager;
import nevavis.render.Gfx;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Реестр HUD-элементов и их отрисовка. */
public final class HudManager {
	private static final List<HudElement> ELEMENTS = new ArrayList<>();
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

	private HudManager() {
	}

	public static List<HudElement> all() {
		return Collections.unmodifiableList(ELEMENTS);
	}

	private static void add(HudElement e) {
		ELEMENTS.add(e);
	}

	public static void init() {
		add(new TextHud("fps", "FPS", 0.01f, 0.01f, true,
				() -> List.of("FPS: " + MinecraftClient.getInstance().getCurrentFps())));
		add(new TextHud("xyz", "Coordinates", 0.01f, 0.07f, true, () -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null) {
				return List.of();
			}
			return List.of("XYZ: " + (int) Math.floor(mc.player.getX()) + " " + (int) Math.floor(mc.player.getY())
					+ " " + (int) Math.floor(mc.player.getZ()));
		}));
		add(new TextHud("cps", "CPS", 0.01f, 0.13f, false,
				() -> List.of("CPS: " + ClickTracker.left() + " | " + ClickTracker.right())));
		add(new TextHud("direction", "Direction", 0.01f, 0.19f, false, () -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null) {
				return List.of();
			}
			String f = mc.player.getHorizontalFacing().asString();
			return List.of(f.substring(0, 1).toUpperCase() + f.substring(1) + " (" + Math.round(mc.player.getYaw()) + ")");
		}));
		add(new TextHud("biome", "Biome", 0.01f, 0.25f, false, () -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null || mc.world == null) {
				return List.of();
			}
			String b = mc.world.getBiome(mc.player.getBlockPos()).getKey()
					.map(k -> k.getValue().getPath().replace('_', ' ')).orElse("unknown");
			return List.of("Biome: " + b);
		}));
		add(new TextHud("speed", "Speed", 0.01f, 0.31f, false,
				() -> List.of(String.format("Speed: %.1f b/s", SessionStats.speed()))));
		add(new TextHud("ping", "Ping", 0.01f, 0.37f, false, () -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null || mc.getNetworkHandler() == null) {
				return List.of();
			}
			var e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
			return List.of("Ping: " + (e == null ? "-" : e.getLatency() + " ms"));
		}));
		add(new TextHud("clock", "Clock", 0.90f, 0.01f, false,
				() -> List.of(LocalTime.now().format(CLOCK))));
		add(new TextHud("memory", "Memory", 0.90f, 0.07f, false, () -> {
			Runtime r = Runtime.getRuntime();
			long used = (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
			return List.of("RAM: " + used + " / " + r.maxMemory() / (1024 * 1024) + " MB");
		}));
		add(new TextHud("server", "Server Info", 0.90f, 0.13f, false, () -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.isInSingleplayer()) {
				return List.of("Singleplayer");
			}
			var s = mc.getCurrentServerEntry();
			return s == null ? List.of() : List.of(s.address, "Ping: " + s.ping + " ms");
		}));
		add(new TextHud("session", "Session", 0.90f, 0.20f, false, () -> {
			long t = SessionStats.playtimeSeconds();
			return List.of(String.format("Session: %d:%02d:%02d", t / 3600, (t / 60) % 60, t % 60),
					"FPS avg " + SessionStats.avgFps() + " min " + SessionStats.minFps() + " max " + SessionStats.maxFps());
		}));
		add(new TextHud("potions", "Potion Status", 0.90f, 0.30f, false, () -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null) {
				return List.of();
			}
			List<String> out = new ArrayList<>();
			for (StatusEffectInstance e : mc.player.getStatusEffects()) {
				String n = e.getEffectType().value().getName().getString();
				String lvl = e.getAmplifier() > 0 ? " " + (e.getAmplifier() + 1) : "";
				int sec = e.getDuration() / 20;
				out.add(n + lvl + (e.isInfinite() ? "" : String.format(" %d:%02d", sec / 60, sec % 60)));
			}
			return out;
		}));
		add(new TextHud("modules", "Module List", 0.90f, 0.45f, false, () -> {
			List<String> out = new ArrayList<>();
			for (Module m : ModuleManager.all()) {
				if (m.isEnabled()) {
					out.add(m.name());
				}
			}
			out.sort((a, b) -> Integer.compare(Gfx.width(b), Gfx.width(a)));
			return out;
		}));
		add(new TextHud("watermark", "Watermark", 0.40f, 0.95f, false,
				() -> List.of("NevaVis " + NevaVis.VERSION())));
		add(new KeystrokesHud());
		add(new ArmorHud());
		add(new nevavis.hud.elements.TargetHud());
		add(new nevavis.hud.elements.ComboHud());
		add(new nevavis.hud.elements.ItemCountHud());
		CombatTracker.init();
	}

	public static HudElement byId(String id) {
		for (HudElement e : ELEMENTS) {
			if (e.id.equals(id)) {
				return e;
			}
		}
		return null;
	}

	public static void tick(MinecraftClient mc) {
		CombatTracker.tick(mc);
		SessionStats.tick(mc);
	}

	public static void render(DrawContext c, MinecraftClient mc) {
		for (HudElement e : ELEMENTS) {
			e.render(c, mc, false);
		}
	}
}
