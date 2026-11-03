package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

import java.util.function.Supplier;

/** Карточка со статистикой: заголовок + значение. */
public final class Card extends UiComponent {
	private final String title;
	private final Supplier<String> value;

	public Card(String title, Supplier<String> value) {
		this.title = title;
		this.value = value;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		int r = Math.min(Gfx.radius(), 10);
		float hv = hover.value;
		Gfx.glow(c, x, y, w, h, r, ThemeManager.accent(), hv * 0.6f);
		Gfx.roundedGradient(c, x, y, w, h, r,
				Colors.lerp(Gfx.surface(1f), Gfx.accent(0.25f), 0.15f + 0.2f * hv), Gfx.surface(0.7f));
		Gfx.fill(c, x + r, y + 1, x + w - r, y + 2, Colors.withAlpha(0xFFFFFF, 0.08f));
		Gfx.roundedOutline(c, x, y, w, h, r, Gfx.accent(0.30f + 0.5f * hv));
		Gfx.text(c, title.toUpperCase(java.util.Locale.ROOT), x + 8, y + 7, ThemeManager.dim());
		Gfx.text(c, Gfx.fit(value.get(), w - 16), x + 8, y + h - 17, ThemeManager.text());
	}
}
