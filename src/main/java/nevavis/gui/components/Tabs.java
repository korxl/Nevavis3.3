package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.List;
import java.util.function.IntConsumer;

/** Горизонтальные вкладки с анимированным подчёркиванием. */
public final class Tabs extends UiComponent {
	private static final int PAD = 10;

	private final List<String> labels;
	private final IntConsumer onSelect;
	private int selected;
	private final SmoothValue ux = new SmoothValue(0f);
	private final SmoothValue uw = new SmoothValue(0f);
	private boolean initialized;

	public Tabs(List<String> labels, IntConsumer onSelect) {
		this.labels = labels;
		this.onSelect = onSelect;
	}

	public int selected() {
		return selected;
	}

	private int tabX(int index) {
		int cx = x;
		for (int i = 0; i < index; i++) {
			cx += Gfx.width(labels.get(i)) + PAD * 2;
		}
		return cx;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		int tx = tabX(selected);
		int tw = Gfx.width(labels.get(selected)) + PAD * 2;
		if (!initialized) {
			ux.snap(tx);
			uw.snap(tw);
			initialized = true;
		}
		ux.approach(tx, dt, 16f);
		uw.approach(tw, dt, 16f);

		int cx = x;
		for (int i = 0; i < labels.size(); i++) {
			String s = labels.get(i);
			int width = Gfx.width(s) + PAD * 2;
			boolean over = mx >= cx && mx < cx + width && my >= y && my < y + h && enabled;
			int col = i == selected ? ThemeManager.text()
					: Colors.lerp(ThemeManager.dim(), ThemeManager.text(), over ? 0.7f : 0f);
			Gfx.text(c, s, cx + PAD, y + (h - 8) / 2 - 1, col);
			cx += width;
		}
		Gfx.fill(c, x, y + h - 1, cx, y + h, Colors.withAlpha(ThemeManager.dim(), 0.2f));
		Gfx.roundedGradient(c, Math.round(ux.value), y + h - 2, Math.round(uw.value), 2, 1,
				Gfx.accent(1f), Gfx.accent2(1f));
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != 0 || !enabled || my < y || my >= y + h) {
			return false;
		}
		int cx = x;
		for (int i = 0; i < labels.size(); i++) {
			int width = Gfx.width(labels.get(i)) + PAD * 2;
			if (mx >= cx && mx < cx + width) {
				if (i != selected) {
					selected = i;
					onSelect.accept(i);
				}
				return true;
			}
			cx += width;
		}
		return false;
	}
}
