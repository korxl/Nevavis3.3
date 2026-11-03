package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.render.Icons;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.List;

/** Левая навигационная панель с анимированным индикатором выбранной страницы. */
public final class Sidebar {
	public record Item(String label, int icon) {
	}

	private static final int ITEM_H = 22;

	private final List<Item> items;
	private final SmoothValue indicator = new SmoothValue(0f);
	private final SmoothValue[] sel;
	private final SmoothValue[] hov;
	private int selected;
	private int x, y, w;

	public Sidebar(List<Item> items) {
		this.items = items;
		this.sel = new SmoothValue[items.size()];
		this.hov = new SmoothValue[items.size()];
		for (int i = 0; i < items.size(); i++) {
			sel[i] = new SmoothValue(0f);
			hov[i] = new SmoothValue(0f);
		}
		sel[0].snap(1f);
	}

	public void bounds(int x, int y, int w) {
		this.x = x;
		this.y = y;
		this.w = w;
		indicator.snap(selected * ITEM_H);
	}

	public void select(int index) {
		selected = index;
	}

	public int selected() {
		return selected;
	}

	public void render(DrawContext c, double mx, double my, float dt) {
		indicator.approach(selected * ITEM_H, dt, 16f);
		int r = Math.min(Gfx.radius(), 8);
		int iy = y + Math.round(indicator.value);
		Gfx.glow(c, x + 6, iy, w - 12, ITEM_H - 2, r, ThemeManager.accent(), 0.5f);
		Gfx.roundedGradient(c, x + 6, iy, w - 12, ITEM_H - 2, r, Gfx.accent(0.40f), Gfx.accent2(0.22f));
		Gfx.roundedGradient(c, x + 6, iy + 3, 2, ITEM_H - 8, 1, Gfx.accent(1f), Gfx.accent2(1f));

		for (int i = 0; i < items.size(); i++) {
			int top = y + i * ITEM_H;
			boolean over = mx >= x && mx < x + w && my >= top && my < top + ITEM_H;
			hov[i].approach(over ? 1f : 0f, dt, 14f);
			sel[i].approach(i == selected ? 1f : 0f, dt, 14f);
			float a = Math.max(hov[i].value * 0.75f, sel[i].value);
			int col = Colors.lerp(ThemeManager.dim(), ThemeManager.text(), a);
			int shift = Math.round(2f * hov[i].value);
			Icons.draw(c, items.get(i).icon(), x + 16 + shift, top + (ITEM_H - 2 - 8) / 2,
					Colors.lerp(col, ThemeManager.accent(), sel[i].value * 0.6f));
			Gfx.text(c, items.get(i).label(), x + 32 + shift, top + (ITEM_H - 2 - 8) / 2, col);
		}
	}

	/** Возвращает индекс элемента под курсором или -1. */
	public int hit(double mx, double my) {
		if (mx < x || mx >= x + w || my < y) {
			return -1;
		}
		int idx = (int) ((my - y) / ITEM_H);
		return idx >= 0 && idx < items.size() ? idx : -1;
	}
}
