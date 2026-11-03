package nevavis.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.notifications.NotificationManager;
import nevavis.render.Icons;

import java.util.ArrayList;
import java.util.List;

/** Базовый класс модуля. Состояние и настройки хранятся в config.json. */
public abstract class Module {
	/** Числовая настройка модуля (рисуется слайдером). */
	public record Setting(String key, String label, float min, float max, float step, float def) {
	}

	private final String id;
	private final String name;
	private final String description;
	private final ModuleCategory category;
	private final List<Setting> settings = new ArrayList<>();
	private boolean enabled;

	protected Module(String id, String name, String description, ModuleCategory category) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	public String description() {
		return description;
	}

	public ModuleCategory category() {
		return category;
	}

	public List<Setting> settings() {
		return settings;
	}

	protected final void addSetting(String key, String label, float min, float max, float step, float def) {
		settings.add(new Setting(key, label, min, max, step, def));
	}

	public final float get(String key) {
		Double v = ConfigManager.get().data().moduleSettings.get(id + "." + key);
		if (v != null) {
			return v.floatValue();
		}
		for (Setting s : settings) {
			if (s.key().equals(key)) {
				return s.def();
			}
		}
		return 0f;
	}

	public final void set(String key, float value) {
		ConfigManager.get().data().moduleSettings.put(id + "." + key, (double) value);
	}

	public boolean isEnabled() {
		return enabled;
	}

	/** Установка состояния без записи в конфиг (загрузка конфига). */
	final void applyState(boolean value) {
		enabled = value;
	}

	public void setEnabled(boolean value) {
		if (value == enabled) {
			return;
		}
		var list = ConfigManager.get().data().enabledModules;
		if (value) {
			if (!list.contains(id)) {
				list.add(id);
			}
		} else {
			list.remove(id);
		}
		enabled = value;
		ConfigManager.get().noteChange(name + (value ? " enabled" : " disabled"));
		NotificationManager.push(value ? "Module Enabled" : "Module Disabled", name, Icons.VISUALS);
	}

	/** Рисование поверх HUD, пока модуль включён. */
	public void onHud(DrawContext c, MinecraftClient mc, float tickDelta) {
	}
}
