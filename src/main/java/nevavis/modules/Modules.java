package nevavis.modules;

import nevavis.modules.visuals.EdgeVignette;
import nevavis.modules.visuals.HitMarker;
import nevavis.modules.visuals.HurtEffect;
import nevavis.modules.visuals.LowHealthPulse;
import nevavis.modules.visuals.ScreenTint;
import nevavis.modules.visuals.SkyTintModule;
import nevavis.modules.visuals.SwingModule;
import nevavis.modules.visuals.DurabilityAlert;

/** Список всех реализованных модулей. */
public final class Modules {
	private Modules() {
	}

	public static void registerAll() {
		ModuleManager.register(new HurtEffect());
		ModuleManager.register(new LowHealthPulse());
		ModuleManager.register(new ScreenTint());
		ModuleManager.register(new EdgeVignette());
		ModuleManager.register(new HitMarker());
		ModuleManager.register(new SwingModule());
		ModuleManager.register(new DurabilityAlert());
		ModuleManager.register(new SkyTintModule("custom_sky", "Custom Sky",
				"Tints the sky color (keeps day/night brightness)", 0.75f));
		ModuleManager.register(new SkyTintModule("custom_clouds", "Custom Clouds",
				"Tints the cloud color", 0.9f));
	}
}
