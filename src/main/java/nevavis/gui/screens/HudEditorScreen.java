package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import nevavis.config.ConfigManager;
import nevavis.gui.components.Button;
import nevavis.gui.components.ColorPicker;
import nevavis.gui.components.Dropdown;
import nevavis.gui.components.Slider;
import nevavis.gui.components.Toggle;
import nevavis.gui.components.UiComponent;
import nevavis.hud.HudElement;
import nevavis.hud.HudManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

import java.util.ArrayList;
import java.util.List;

/** Полноэкранный редактор HUD: элементы перетаскиваются мышью, справа — настройки выбранного. */
public final class HudEditorScreen extends Screen {
	private static final int PW = 150;

	private final Screen parent;
	private final List<UiComponent> panel = new ArrayList<>();
	private final Dropdown picker;
	private HudElement selected;
	private boolean dragging;
	private int dragDx, dragDy;
	private long lastNs = System.nanoTime();

	public HudEditorScreen(Screen parent) {
		super(Text.literal("NevaVis HUD Editor"));
		this.parent = parent;
		selected = HudManager.all().get(0);

		List<String> names = new ArrayList<>();
		for (HudElement e : HudManager.all()) {
			names.add(e.name);
		}
		picker = new Dropdown("", names, () -> selected.name, n -> {
			for (HudElement e : HudManager.all()) {
				if (e.name.equals(n)) {
					selected = e;
				}
			}
		});
		panel.add(picker);
		panel.add(new Toggle("Visible", () -> selected.cfg().visible, v -> selected.cfg().visible = v));
		panel.add(new Toggle("Background", () -> selected.cfg().background, v -> selected.cfg().background = v));
		panel.add(new Toggle("Text shadow", () -> selected.cfg().shadow, v -> selected.cfg().shadow = v));
		panel.add(new Slider("Scale", 0.5f, 3f, 0.05f, () -> selected.cfg().scale, v -> selected.cfg().scale = v, "%.2f", true));
		panel.add(new Slider("Opacity", 0.2f, 1f, 0.05f, () -> selected.cfg().alpha, v -> selected.cfg().alpha = v, "%.2f", true));
		panel.add(new ColorPicker("Text color", () -> selected.cfg().color, v -> selected.cfg().color = v));
		panel.add(new Button("Reset position", () -> selected.resetPosition()));
		panel.add(new Button("Done", this::close).primary());
	}

	private int panelX() {
		return width - PW - 8;
	}

	private void layoutPanel() {
		int x = panelX() + 6, w = PW - 12, y = 12;
		picker.bounds(x, y, w, 20);
		y += 26;
		for (int i = 1; i <= 3; i++) {
			panel.get(i).bounds(x, y, w, 20);
			y += 23;
		}
		panel.get(4).bounds(x, y, w, 24);
		y += 27;
		panel.get(5).bounds(x, y, w, 24);
		y += 27;
		panel.get(6).bounds(x, y, w, ColorPicker.HEIGHT);
		y += ColorPicker.HEIGHT + 6;
		panel.get(7).bounds(x, y, w, 20);
		y += 24;
		panel.get(8).bounds(x, y, w, 20);
	}

	private int panelHeight() {
		return panel.get(8).y + 20 + 8;
	}

	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void render(DrawContext c, int mouseX, int mouseY, float delta) {
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1_000_000_000f);
		lastNs = now;
		ThemeManager.update(dt);
		Gfx.setFade(1f);
		Gfx.setTransform(width / 2f, height / 2f, 1f, width, height);

		c.fill(0, 0, width, height, 0x88000000);
		for (HudElement e : HudManager.all()) {
			e.render(c, client, true);
		}
		int sw = width, sh = height;
		int ox = selected.ax(sw), oy = selected.ay(sh);
		Gfx.roundedOutline(c, ox - 2, oy - 2, selected.aw() + 4, selected.ah() + 4, 3, ThemeManager.accent());

		layoutPanel();
		int px = panelX();
		Gfx.glow(c, px, 8, PW, panelHeight(), 10, ThemeManager.accent(), 0.5f);
		Gfx.glassPanel(c, px, 8, PW, panelHeight(), 10, 0.7f);
		for (UiComponent comp : panel) {
			comp.draw(c, mouseX, mouseY, dt);
		}
		for (UiComponent comp : panel) {
			comp.renderOverlay(c, mouseX, mouseY, dt);
		}
		Gfx.centered(c, "Drag elements with the mouse", (width - PW) / 2, height - 14, ThemeManager.dim());
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		layoutPanel();
		for (UiComponent comp : panel) {
			if (comp.capturesInput() && comp.mouseClicked(mx, my, button)) {
				return true;
			}
		}
		for (UiComponent comp : panel) {
			if (comp.mouseClicked(mx, my, button)) {
				return true;
			}
		}
		if (mx >= panelX() && my >= 8 && my <= 8 + panelHeight()) {
			return true;
		}
		if (button == 0) {
			List<HudElement> list = HudManager.all();
			for (int i = list.size() - 1; i >= 0; i--) {
				HudElement e = list.get(i);
				if (e.hit(mx, my, width, height)) {
					selected = e;
					dragging = true;
					dragDx = (int) mx - e.ax(width);
					dragDy = (int) my - e.ay(height);
					return true;
				}
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		boolean used = false;
		for (UiComponent comp : panel) {
			used |= comp.mouseDragged(mx, my, button);
		}
		if (dragging) {
			float maxX = Math.max(0f, 1f - selected.aw() / (float) width);
			float maxY = Math.max(0f, 1f - selected.ah() / (float) height);
			selected.cfg().fx = Math.max(0f, Math.min(maxX, (float) (mx - dragDx) / width));
			selected.cfg().fy = Math.max(0f, Math.min(maxY, (float) (my - dragDy) / height));
			return true;
		}
		return used || super.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		dragging = false;
		boolean used = false;
		for (UiComponent comp : panel) {
			used |= comp.mouseReleased(mx, my, button);
		}
		return used || super.mouseReleased(mx, my, button);
	}

	@Override
	public void close() {
		ConfigManager.get().save();
		client.setScreen(parent);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
