package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

import java.util.List;

/** Страница-заготовка для подсистем, которые появятся на следующих этапах. */
public final class PlaceholderPage extends Page {
	private final String title;
	private final int stage;
	private final List<String> lines;

	public PlaceholderPage(String title, int stage, List<String> lines) {
		this.title = title;
		this.stage = stage;
		this.lines = lines;
	}

	@Override
	public String title() {
		return title;
	}

	@Override
	protected int reflow(int top) {
		return top + h;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		int bw = w - 10;
		int bh = 26 + lines.size() * 12 + 14;
		int by = y + (h - bh) / 2;
		int r = Math.min(Gfx.radius(), 12);
		Gfx.glassPanel(c, x, by, bw, bh, r, 0.35f);
		Gfx.centered(c, "COMING IN STAGE " + stage, x + bw / 2, by + 10, ThemeManager.accent());
		int ty = by + 26;
		for (String s : lines) {
			Gfx.centered(c, s, x + bw / 2, ty, ThemeManager.dim());
			ty += 12;
		}
	}
}
