package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class Toggle extends UiComponent {
	private static final int SW = 34, SH = 16;

	private final String label;
	private final BooleanSupplier getter;
	private final Consumer<Boolean> setter;
	private final SmoothValue state = new SmoothValue(0f);
	private boolean initialized;

	public Toggle(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		this.label = label;
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		boolean v = getter.getAsBoolean();
		if (!initialized) {
			state.snap(v ? 1f : 0f);
			initialized = true;
		}
		state.approach(v ? 1f : 0f, dt, 16f);
		float st = state.value;

		int r = Math.min(Gfx.radius(), h / 2);
		Gfx.roundedRect(c, x, y, w, h, r, Colors.lerp(Gfx.surface(0.55f), Gfx.accent(0.18f), hover.value));
		Gfx.text(c, label, x + 8, y + (h - 8) / 2, ThemeManager.text());

		int sx = x + w - SW - 8;
		int sy = y + (h - SH) / 2;
		Gfx.glow(c, sx, sy, SW, SH, SH / 2, ThemeManager.accent(), st * 0.7f);
		int track = Colors.lerp(Colors.withAlpha(ThemeManager.dim(), 0.35f),
				Colors.lerp(Gfx.accent(0.95f), Gfx.accent2(0.95f), 0.5f), st);
		Gfx.roundedRect(c, sx, sy, SW, SH, SH / 2, track);
		int kx = sx + 2 + Math.round((SW - SH) * st);
		Gfx.roundedRect(c, kx, sy + 2, SH - 4, SH - 4, (SH - 4) / 2, ThemeManager.text());
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0 && enabled && contains(mx, my)) {
			boolean nv = !getter.getAsBoolean();
			setter.accept(nv);
			ConfigManager.get().noteChange(label + ": " + (nv ? "ON" : "OFF"));
			return true;
		}
		return false;
	}
}
