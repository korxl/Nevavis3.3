package nevavis.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Данные config.json (она же — содержимое профиля). */
public final class NevaVisConfig {
	public int configVersion = 4;

	// Тема
	public String theme = "NevaVis Purple";
	public int customAccent = 0xA855F7;
	public int customAccent2 = 0x3B82F6;
	public int customBg = 0x0A0710;

	// Интерфейс
	public float guiScale = 1.0f;
	public float animationSpeed = 1.0f;
	public float glowIntensity = 1.0f;
	public float transparency = 0.85f;
	public int cornerRadius = 8;
	public boolean guiAnimations = true;
	public boolean glowEnabled = true;
	public boolean lowPerformance = false;
	public boolean fpsOptimization = false;
	public float hudScale = 1.0f;

	// Уведомления
	public boolean notifications = true;
	public float notificationSeconds = 3.0f;

	// Профили
	public String activeProfile = "Default";

	// Онлайн / значок N
	public boolean presenceEnabled = true;
	public String presenceUrl = "";
	public boolean showBadge = true;
	public boolean showCape = true;

	// Текст песни в небе
	public boolean skyLyrics = true;

	// Модули
	public List<String> enabledModules = new ArrayList<>();
	public Map<String, Double> moduleSettings = new HashMap<>();

	// Подсистемы
	public Map<String, HudElementConfig> hud = new HashMap<>();
	public ParticleConfig particles = new ParticleConfig();
	public MusicConfig music = new MusicConfig();
	public CrosshairConfig crosshair = new CrosshairConfig();

	/** Настройки одного HUD-элемента. fx/fy — положение как доля экрана (не зависит от разрешения). */
	public static final class HudElementConfig {
		public boolean visible;
		public float fx, fy;
		public float scale = 1f;
		public int color = 0xFFFFFF;
		public float alpha = 1f;
		public boolean shadow = true;
		public boolean background = true;
	}

	public static final class ParticleConfig {
		public List<String> enabled = new ArrayList<>();
		public float amount = 1f, speed = 1f, spread = 1f, gravity = 0f, glow = 0f;
		public String quality = "Medium";
	}

	public static final class MusicConfig {
		public float volume = 0.5f;
		public String lastTrack = "";
		public boolean shuffle = false;
		public String repeat = "Off"; // Off / All / One
		public String hudVariant = "Glass";
		public boolean hudEnabled = true;
	}

	public static final class CrosshairConfig {
		public boolean enabled = false;
		public String type = "Cross";
		public float size = 6, thickness = 1, gap = 3;
		public int color = 0xFFFFFF;
		public float alpha = 1f;
		public boolean outline = true;
		public float glow = 0f;
		public float rotation = 0f;
		public float animationSpeed = 1f;
		public boolean rainbow = false;
	}

	/** Копирует все поля из other в этот объект (идентичность объекта сохраняется для UI-лямбд). */
	public void copyFrom(NevaVisConfig other) {
		for (Field f : NevaVisConfig.class.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers())) {
				continue;
			}
			try {
				f.set(this, f.get(other));
			} catch (IllegalAccessException ignored) {
				// поля публичные — недостижимо
			}
		}
	}

	/** Сброс настроек интерфейса (Reset to Defaults). */
	public void resetInterface() {
		NevaVisConfig d = new NevaVisConfig();
		theme = d.theme;
		customAccent = d.customAccent;
		customAccent2 = d.customAccent2;
		customBg = d.customBg;
		guiScale = d.guiScale;
		animationSpeed = d.animationSpeed;
		glowIntensity = d.glowIntensity;
		transparency = d.transparency;
		cornerRadius = d.cornerRadius;
		guiAnimations = d.guiAnimations;
		glowEnabled = d.glowEnabled;
		lowPerformance = d.lowPerformance;
		fpsOptimization = d.fpsOptimization;
		hudScale = d.hudScale;
		notifications = d.notifications;
		notificationSeconds = d.notificationSeconds;
	}
}
