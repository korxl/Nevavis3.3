package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.Theme;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

/** Карточка темы с образцами цветов. Клик применяет тему. */
public final class ThemeCard extends UiComponent {
	private final Theme theme;

	public ThemeCard(Theme theme) {
		this.theme = theme;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		boolean active = theme.name().equals(ThemeManager.currentName());
		int r = Math.min(Gfx.radius(), 10);
		float hv = hover.value;
		int accent = Colors.opaque(theme.accent());
		if (active || hv > 0.05f) {
			Gfx.glow(c, x, y, w, h, r, accent, active ? 0.8f : hv * 0.6f);
		}
		Gfx.roundedGradient(c, x, y, w, h, r,
				Colors.withAlpha(Colors.lerp(Colors.opaque(theme.surface()), accent, 0.12f), 0.95f),
				Colors.withAlpha(Colors.opaque(theme.bg()), 0.95f));
		Gfx.roundedOutline(c, x, y, w, h, r, Colors.withAlpha(accent, active ? 1f : 0.35f + 0.4f * hv));
		Gfx.text(c, theme.name(), x + 10, y + 8, Colors.opaque(theme.text()));
		if (active) {
			Gfx.rightAligned(c, "ACTIVE", x + w - 10, y + 8, accent);
		}
		int sx = x + 10;
		int sy = y + h - 16;
		int[] sw = {theme.bg(), theme.surface(), theme.accent(), theme.accent2(), theme.text()};
		for (int col : sw) {
			Gfx.roundedRect(c, sx, sy, 14, 8, 3, Colors.opaque(col));
			Gfx.roundedOutline(c, sx, sy, 14, 8, 3, Colors.withAlpha(0xFFFFFF, 0.25f));
			sx += 18;
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0 && enabled && contains(mx, my)) {
			ThemeManager.apply(theme);
			return true;
		}
		return false;
	}
}
