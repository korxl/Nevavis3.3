package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

import java.util.function.Supplier;

public final class Button extends UiComponent {
	private final Supplier<String> label;
	private final Runnable action;
	private boolean primary;
	private boolean danger;

	public Button(String label, Runnable action) {
		this(() -> label, action);
	}

	public Button(Supplier<String> label, Runnable action) {
		this.label = label;
		this.action = action;
	}

	public Button primary() {
		this.primary = true;
		return this;
	}

	/** Красная подсветка (опасное действие). */
	public Button danger(boolean d) {
		this.danger = d;
		return this;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		int r = Math.min(Gfx.radius(), h / 2);
		float hv = hover.value;
		int fillColor;
		if (danger) {
			fillColor = Colors.lerp(Colors.withAlpha(0xDC2626, 0.55f), Colors.withAlpha(0xEF4444, 0.8f), hv);
		} else if (primary) {
			fillColor = Colors.lerp(Gfx.accent(0.55f), Gfx.accent2(0.75f), hv);
		} else {
			fillColor = Colors.lerp(Gfx.surface(0.9f), Gfx.accent(0.35f), hv);
		}
		Gfx.glow(c, x, y, w, h, r, ThemeManager.accent(), hv * 0.8f);
		Gfx.roundedRect(c, x, y, w, h, r, fillColor);
		Gfx.roundedOutline(c, x, y, w, h, r, danger ? Colors.withAlpha(0xEF4444, 0.9f) : Gfx.accent(0.45f + 0.5f * hv));
		Gfx.centered(c, Gfx.fit(label.get(), w - 6), x + w / 2, y + (h - 8) / 2, ThemeManager.text());
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0 && enabled && contains(mx, my)) {
			action.run();
			return true;
		}
		return false;
	}
}
