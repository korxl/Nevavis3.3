package nevavis.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.VignetteDraw;
import nevavis.utils.Colors;

/** Low Health Pulse: пульсирующая красная рамка, когда здоровья мало. */
public final class LowHealthPulse extends Module {
	public LowHealthPulse() {
		super("low_health_pulse", "Low Health Pulse",
				"Pulsing screen edges when your health is low", ModuleCategory.PLAYER);
		addSetting("threshold", "Hearts threshold", 1f, 10f, 1f, 3f);
		addSetting("hue", "Color (hue)", 0f, 1f, 0.01f, 0f);
		addSetting("intensity", "Intensity", 0.1f, 1f, 0.05f, 0.55f);
		addSetting("speed", "Pulse speed", 0.5f, 4f, 0.1f, 1.5f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		ClientPlayerEntity p = mc.player;
		if (p == null || p.isDead() || p.getHealth() > get("threshold") * 2f) {
			return;
		}
		double t = System.currentTimeMillis() / 1000.0 * get("speed") * Math.PI;
		float pulse = 0.55f + 0.45f * (float) Math.sin(t);
		int size = Math.max(6, (int) (Math.min(c.getScaledWindowWidth(), c.getScaledWindowHeight()) * 0.22f));
		VignetteDraw.edges(c, Colors.hsv(get("hue"), 1f, 1f), pulse * get("intensity"), size);
	}
}
