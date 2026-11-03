package nevavis.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Реестр модулей. */
public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();

	private ModuleManager() {
	}

	public static void register(Module m) {
		MODULES.add(m);
	}

	/** Применяет сохранённое состояние (enabledModules) к зарегистрированным модулям. */
	public static void init() {
		var enabled = ConfigManager.get().data().enabledModules;
		for (Module m : MODULES) {
			m.applyState(enabled.contains(m.id()));
		}
	}

	public static List<Module> all() {
		return Collections.unmodifiableList(MODULES);
	}

	public static Module byId(String id) {
		for (Module m : MODULES) {
			if (m.id().equals(id)) {
				return m;
			}
		}
		return null;
	}

	public static int activeCount() {
		int n = 0;
		for (Module m : MODULES) {
			if (m.isEnabled()) {
				n++;
			}
		}
		return n;
	}

	public static void hud(DrawContext c, MinecraftClient mc, float td) {
		for (Module m : MODULES) {
			if (m.isEnabled()) {
				m.onHud(c, mc, td);
			}
		}
	}
}
