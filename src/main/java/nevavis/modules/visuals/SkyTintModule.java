package nevavis.modules.visuals;

import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;

/** Custom Sky / Custom Clouds: подмешивает цвет к небу или облакам (применяется миксином ClientWorld). */
public final class SkyTintModule extends Module {
	public SkyTintModule(String id, String name, String description, float defHue) {
		super(id, name, description, ModuleCategory.WORLD);
		addSetting("hue", "Color (hue)", 0f, 1f, 0.01f, defHue);
		addSetting("saturation", "Saturation", 0f, 1f, 0.05f, 0.7f);
		addSetting("strength", "Strength", 0.05f, 1f, 0.05f, 0.6f);
	}
}
