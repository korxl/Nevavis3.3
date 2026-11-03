package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig.CrosshairConfig;
import nevavis.gui.components.ColorPicker;
import nevavis.gui.components.Dropdown;
import nevavis.gui.components.Slider;
import nevavis.gui.components.Toggle;
import nevavis.render.CrosshairRenderer;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

/** Редактор прицела с предпросмотром. */
public final class CrosshairPage extends Page {
	private static final int PREVIEW_H = 80;

	private final Toggle enabled, outline, rainbow;
	private final Dropdown type;
	private final Slider size, thickness, gap, alpha, glow, rotation, anim;
	private final ColorPicker color;
	private int previewY;

	private static CrosshairConfig k() {
		return ConfigManager.get().data().crosshair;
	}

	public CrosshairPage() {
		enabled = new Toggle("Use custom crosshair", () -> k().enabled, v -> k().enabled = v);
		type = new Dropdown("Type", CrosshairRenderer.TYPES, () -> k().type, t -> k().type = t);
		size = new Slider("Size", 1f, 20f, 1f, () -> k().size, v -> k().size = v, "%.0f");
		thickness = new Slider("Thickness", 1f, 6f, 1f, () -> k().thickness, v -> k().thickness = v, "%.0f");
		gap = new Slider("Gap", 0f, 15f, 1f, () -> k().gap, v -> k().gap = v, "%.0f");
		alpha = new Slider("Transparency", 0.1f, 1f, 0.05f, () -> k().alpha, v -> k().alpha = v, "%.2f");
		glow = new Slider("Glow", 0f, 1f, 0.05f, () -> k().glow, v -> k().glow = v, "%.2f");
		rotation = new Slider("Rotation", 0f, 90f, 1f, () -> k().rotation, v -> k().rotation = v, "%.0f deg");
		anim = new Slider("Animation speed", 0.2f, 4f, 0.1f, () -> k().animationSpeed, v -> k().animationSpeed = v, "%.1fx");
		outline = new Toggle("Outline", () -> k().outline, v -> k().outline = v);
		rainbow = new Toggle("Rainbow color", () -> k().rainbow, v -> k().rainbow = v);
		color = new ColorPicker("Color", () -> k().color, v -> k().color = v);
		comps.add(enabled);
		comps.add(size);
		comps.add(thickness);
		comps.add(gap);
		comps.add(alpha);
		comps.add(glow);
		comps.add(rotation);
		comps.add(anim);
		comps.add(outline);
		comps.add(rainbow);
		comps.add(color);
		comps.add(type);
	}

	@Override
	public String title() {
		return "Crosshair";
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		previewY = top;
		int cy = top + PREVIEW_H + 8;
		enabled.bounds(x, cy, usable, 22);
		cy += 26;
		type.bounds(x, cy, usable, 22);
		cy += 30;
		for (Slider s : new Slider[]{size, thickness, gap, alpha, glow, rotation, anim}) {
			s.bounds(x, cy, usable, 26);
			cy += 30;
		}
		outline.bounds(x, cy, usable, 22);
		cy += 26;
		rainbow.bounds(x, cy, usable, 22);
		cy += 30;
		color.bounds(x, cy, usable, ColorPicker.HEIGHT);
		cy += ColorPicker.HEIGHT + 10;
		return cy;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		int usable = w - 10;
		Gfx.glassPanel(c, x, previewY, usable, PREVIEW_H, Math.min(Gfx.radius(), 12), 0.4f);
		float t = (System.currentTimeMillis() % 100000L) / 1000f;
		CrosshairRenderer.draw(c, x + usable / 2, previewY + PREVIEW_H / 2, k(), t, 0.5f);
		Gfx.text(c, "PREVIEW", x + 8, previewY + 6, ThemeManager.dim());
	}
}
