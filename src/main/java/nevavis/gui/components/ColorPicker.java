package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Выбор цвета тремя RGB-слайдерами с образцом. */
public final class ColorPicker extends UiComponent {
	public static final int HEIGHT = 16 + 3 * 22;

	private final String label;
	private final IntSupplier getter;
	private final Slider rs, gs, bs;

	public ColorPicker(String label, IntSupplier getter, IntConsumer setter) {
		this.label = label;
		this.getter = getter;
		this.rs = new Slider("R", 0, 255, 1, () -> (float) Colors.red(getter.getAsInt()),
				v -> setter.accept(Colors.rgb(v.intValue(), Colors.green(getter.getAsInt()), Colors.blue(getter.getAsInt())) & 0xFFFFFF),
				"%.0f", true);
		this.gs = new Slider("G", 0, 255, 1, () -> (float) Colors.green(getter.getAsInt()),
				v -> setter.accept(Colors.rgb(Colors.red(getter.getAsInt()), v.intValue(), Colors.blue(getter.getAsInt())) & 0xFFFFFF),
				"%.0f", true);
		this.bs = new Slider("B", 0, 255, 1, () -> (float) Colors.blue(getter.getAsInt()),
				v -> setter.accept(Colors.rgb(Colors.red(getter.getAsInt()), Colors.green(getter.getAsInt()), v.intValue()) & 0xFFFFFF),
				"%.0f", true);
	}

	private void layoutChildren() {
		rs.bounds(x, y + 16, w, 22);
		gs.bounds(x, y + 16 + 22, w, 22);
		bs.bounds(x, y + 16 + 44, w, 22);
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		layoutChildren();
		Gfx.text(c, label, x + 2, y + 2, ThemeManager.text());
		int col = Colors.opaque(getter.getAsInt());
		Gfx.roundedRect(c, x + w - 44, y, 42, 12, 4, col);
		Gfx.roundedOutline(c, x + w - 44, y, 42, 12, 4, Colors.withAlpha(0xFFFFFF, 0.5f));
		rs.draw(c, mx, my, dt);
		gs.draw(c, mx, my, dt);
		bs.draw(c, mx, my, dt);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		layoutChildren();
		return rs.mouseClicked(mx, my, button) || gs.mouseClicked(mx, my, button) || bs.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button) {
		boolean a = rs.mouseDragged(mx, my, button);
		boolean b = gs.mouseDragged(mx, my, button);
		boolean d = bs.mouseDragged(mx, my, button);
		return a || b || d;
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		boolean a = rs.mouseReleased(mx, my, button);
		boolean b = gs.mouseReleased(mx, my, button);
		boolean d = bs.mouseReleased(mx, my, button);
		return a || b || d;
	}
}
