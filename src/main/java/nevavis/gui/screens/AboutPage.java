package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.core.NevaVis;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

/** О клиенте. */
public final class AboutPage extends Page {
	@Override
	public String title() {
		return "About";
	}

	@Override
	protected int reflow(int top) {
		return top + h;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		int bw = w - 10;
		int bh = 112;
		Gfx.glassPanel(c, x, y, bw, bh, Math.min(Gfx.radius(), 12), 0.4f);
		Gfx.text(c, "NEVAVIS", x + 14, y + 14, ThemeManager.accent(), true);
		String[] lines = {
				"Version " + NevaVis.VERSION(),
				"Minecraft " + NevaVis.MC_VERSION + " - Fabric",
				"Futuristic dark glass client UI",
				"Status: early development"
		};
		int ty = y + 34;
		for (String s : lines) {
			Gfx.text(c, s, x + 14, ty, ThemeManager.dim());
			ty += 14;
		}
		Gfx.text(c, "Config: config/nevavis/", x + 14, y + bh - 18, ThemeManager.dim());
	}
}
