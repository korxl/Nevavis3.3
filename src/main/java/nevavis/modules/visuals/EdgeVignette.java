package nevavis.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.VignetteDraw;
import nevavis.utils.Colors;

/** Neon Vignette: постоянная цветная рамка по краям экрана. */
public final class EdgeVignette extends Module {
	public EdgeVignette() {
		super("neon_vignette", "Neon Vignette",
				"Permanent colored glow at the screen edges", ModuleCategory.PLAYER);
		addSetting("hue", "Color (hue)", 0f, 1f, 0.01f, 0.78f);
		addSetting("strength", "Strength", 0.05f, 1f, 0.05f, 0.35f);
		addSetting("size", "Edge size", 0.05f, 0.4f, 0.01f, 0.14f);
		addSetting("cycle", "Rainbow cycle", 0f, 1f, 1f, 0f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		float hue = get("hue");
		if (get("cycle") > 0.5f) {
			hue = (System.currentTimeMillis() % 8000L) / 8000f;
		}
		int size = Math.max(4, (int) (Math.min(c.getScaledWindowWidth(), c.getScaledWindowHeight()) * get("size")));
		VignetteDraw.edges(c, Colors.hsv(hue, 0.9f, 1f), get("strength"), size);
	}
}
