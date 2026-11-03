package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Выпадающий список: подпись слева, поле справа, список открывается поверх контента. */
public final class Dropdown extends UiComponent {
	private static final int BOX_W = 130, ITEM_H = 16;

	private final String label;
	private final List<String> options;
	private final Supplier<String> current;
	private final Consumer<String> onSelect;
	private boolean open;
	private final SmoothValue anim = new SmoothValue(0f);

	public Dropdown(String label, List<String> options, Supplier<String> current, Consumer<String> onSelect) {
		this.label = label;
		this.options = options;
		this.current = current;
		this.onSelect = onSelect;
	}

	private int boxX() {
		return x + w - BOX_W;
	}

	private int listY() {
		return y + h + 2;
	}

	private int listH() {
		return options.size() * ITEM_H;
	}

	private boolean inBox(double mx, double my) {
		return mx >= boxX() && mx < boxX() + BOX_W && my >= y && my < y + h;
	}

	private boolean inList(double mx, double my) {
		return open && mx >= boxX() && mx < boxX() + BOX_W && my >= listY() && my < listY() + listH();
	}

	@Override
	public boolean capturesInput() {
		return open;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		anim.approach(open ? 1f : 0f, dt, 18f);
		int r = Math.min(Gfx.radius(), h / 2);
		Gfx.text(c, label, x + 2, y + (h - 8) / 2, ThemeManager.text());
		float hv = Math.max(hover.value, open ? 1f : 0f);
		Gfx.roundedRect(c, boxX(), y, BOX_W, h, r, Gfx.surface(0.8f));
		Gfx.roundedOutline(c, boxX(), y, BOX_W, h, r, Gfx.accent(0.3f + 0.6f * hv));
		Gfx.text(c, Gfx.fit(current.get(), BOX_W - 24), boxX() + 8, y + (h - 8) / 2, ThemeManager.text());
		Gfx.text(c, open ? "^" : "v", boxX() + BOX_W - 12, y + (h - 8) / 2, ThemeManager.accent());
	}

	@Override
	public void renderOverlay(DrawContext c, double mx, double my, float dt) {
		float a = anim.value;
		if (a < 0.02f) {
			return;
		}
		int ly = listY();
		int lh = Math.round(listH() * a);
		int r = Math.min(Gfx.radius(), 8);
		Gfx.roundedRect(c, boxX(), ly, BOX_W, lh, r, Gfx.surface(1f));
		Gfx.roundedRect(c, boxX(), ly, BOX_W, lh, r, Colors.withAlpha(ThemeManager.bg(), 0.85f));
		Gfx.roundedOutline(c, boxX(), ly, BOX_W, lh, r, Gfx.accent(0.7f));
		for (int i = 0; i < options.size(); i++) {
			int iy = ly + i * ITEM_H;
			if (iy + ITEM_H > ly + lh) {
				break;
			}
			boolean over = mx >= boxX() && mx < boxX() + BOX_W && my >= iy && my < iy + ITEM_H;
			if (over) {
				Gfx.fill(c, boxX() + 2, iy, boxX() + BOX_W - 2, iy + ITEM_H, Gfx.accent(0.25f));
			}
			String o = options.get(i);
			int col = o.equals(current.get()) ? ThemeManager.accent() : ThemeManager.text();
			Gfx.text(c, o, boxX() + 8, iy + 4, col);
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != 0 || !enabled) {
			return false;
		}
		if (open) {
			if (inList(mx, my)) {
				int idx = (int) ((my - listY()) / ITEM_H);
				if (idx >= 0 && idx < options.size()) {
					onSelect.accept(options.get(idx));
				}
				open = false;
				return true;
			}
			boolean inside = inBox(mx, my);
			open = false;
			return inside;
		}
		if (inBox(mx, my)) {
			open = true;
			return true;
		}
		return false;
	}
}
