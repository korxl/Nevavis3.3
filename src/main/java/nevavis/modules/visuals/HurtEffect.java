package nevavis.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.VignetteDraw;
import nevavis.utils.Colors;

/** Custom Hurt Effect: цветная виньетка по краям экрана при получении урона. */
public final class HurtEffect extends Module {
	public HurtEffect() {
		super("hurt_effect", "Custom Hurt Effect",
				"Colored screen-edge flash when you take damage", ModuleCategory.PLAYER);
		addSetting("hue", "Color (hue)", 0f, 1f, 0.01f, 0f);
		addSetting("intensity", "Intensity", 0.1f, 1f, 0.05f, 0.6f);
		addSetting("size", "Edge size", 0.05f, 0.4f, 0.01f, 0.18f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		ClientPlayerEntity p = mc.player;
		if (p == null || p.hurtTime <= 0) {
			return;
		}
		float t = Math.max(0f, (p.hurtTime - td) / Math.max(1, p.maxHurtTime));
		int size = Math.max(4, (int) (Math.min(c.getScaledWindowWidth(), c.getScaledWindowHeight()) * get("size")));
		VignetteDraw.edges(c, Colors.hsv(get("hue"), 1f, 1f), Math.min(1f, t) * get("intensity"), size);
	}
}
