package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Slider extends UiComponent {
	private final String label;
	private final float min, max, step;
	private final Supplier<Float> getter;
	private final Consumer<Float> setter;
	private final String format;
	private final boolean quiet;

	private boolean dragging;
	private final SmoothValue fill = new SmoothValue(0f);
	private boolean initialized;
	private float lastShown = Float.NaN;
	private String shownText = "";

	public Slider(String label, float min, float max, float step, Supplier<Float> getter,
				  Consumer<Float> setter, String format) {
		this(label, min, max, step, getter, setter, format, false);
	}

	/** quiet = не писать изменения в «Recent changes» (для составных компонентов). */
	public Slider(String label, float min, float max, float step, Supplier<Float> getter,
				  Consumer<Float> setter, String format, boolean quiet) {
		this.label = label;
		this.min = min;
		this.max = max;
		this.step = step;
		this.getter = getter;
		this.setter = setter;
		this.format = format;
		this.quiet = quiet;
	}

	private float norm(float v) {
		return Math.max(0f, Math.min(1f, (v - min) / (max - min)));
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		float v = getter.get();
		if (v != lastShown) {
			lastShown = v;
			shownText = String.format(Locale.ROOT, format, v);
		}
		if (!initialized) {
			fill.snap(norm(v));
			initialized = true;
		}
		// при перетаскивании заполнение следует за курсором мгновенно
		fill.approach(norm(v), dt, dragging ? 1000f : 20f);
		float f = fill.value;

		Gfx.text(c, label, x + 2, y, ThemeManager.text());
		Gfx.rightAligned(c, shownText, x + w - 2, y, ThemeManager.dim());

		int ty = y + h - 7;
		int tw = w - 4;
		int tx = x + 2;
		Gfx.roundedRect(c, tx, ty, tw, 4, 2, Colors.withAlpha(ThemeManager.dim(), 0.28f));
		int fw = Math.max(0, Math.round(tw * f));
		if (fw > 0) {
			Gfx.roundedGradient(c, tx, ty, fw, 4, 2, Gfx.accent(0.95f), Gfx.accent2(0.95f));
		}
		float active = Math.max(hover.value, dragging ? 1f : 0f);
		int kx = tx + fw - 4;
		Gfx.glow(c, kx, ty - 2, 8, 8, 4, ThemeManager.accent(), active * 0.8f);
		Gfx.roundedRect(c, kx, ty - 2, 8, 8, 4, ThemeManager.text());
	}

	private void setFromMouse(double mx) {
		float t = (float) ((mx - (x + 2)) / (w - 4));
		t = Math.max(0f, Math.min(1f, t));
		float raw = min + t * (max - min);
		if (step > 0f) {
			raw = Math.round(raw / step) * step;
		}
		raw = Math.max(min, Math.min(max, raw));
		setter.accept(raw);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0 && enabled && contains(mx, my)) {
			dragging = true;
			setFromMouse(mx);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button) {
		if (dragging) {
			setFromMouse(mx);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		if (dragging) {
			dragging = false;
			if (!quiet) {
				ConfigManager.get().noteChange(label + ": " + shownText);
			}
			return true;
		}
		return false;
	}
}
