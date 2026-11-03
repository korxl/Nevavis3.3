package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.gui.components.Button;
import nevavis.gui.components.ColorPicker;
import nevavis.gui.components.Dropdown;
import nevavis.gui.components.Tabs;
import nevavis.gui.components.ThemeCard;
import nevavis.render.Gfx;
import nevavis.themes.Theme;
import nevavis.themes.ThemeManager;

import java.util.ArrayList;
import java.util.List;

/** Встроенные темы и редактор пользовательской темы. */
public final class ThemesPage extends Page {
	private final Tabs tabs;
	private int tab;
	private final List<ThemeCard> cards = new ArrayList<>();
	private final Dropdown copyFrom;
	private final ColorPicker accentPicker, accent2Picker, bgPicker;
	private final Button useCustom;

	public ThemesPage() {
		NevaVisConfig cfg = ConfigManager.get().data();
		tabs = new Tabs(List.of("Built-in", "Custom"), i -> {
			tab = i;
			rebuild();
			scroll.reset();
		});
		for (Theme t : ThemeManager.builtin()) {
			cards.add(new ThemeCard(t));
		}
		List<String> names = new ArrayList<>();
		for (Theme t : ThemeManager.builtin()) {
			names.add(t.name());
		}
		copyFrom = new Dropdown("Copy colors from", names, () -> "Choose theme...", name -> {
			for (Theme t : ThemeManager.builtin()) {
				if (t.name().equals(name)) {
					cfg.customAccent = t.accent();
					cfg.customAccent2 = t.accent2();
					cfg.customBg = t.bg();
					ThemeManager.applyCustom();
					ConfigManager.get().noteChange("Custom theme copied from " + name);
				}
			}
		});
		accentPicker = new ColorPicker("Accent", () -> cfg.customAccent, v -> {
			cfg.customAccent = v;
			ThemeManager.applyCustom();
		});
		accent2Picker = new ColorPicker("Accent 2", () -> cfg.customAccent2, v -> {
			cfg.customAccent2 = v;
			ThemeManager.applyCustom();
		});
		bgPicker = new ColorPicker("Background", () -> cfg.customBg, v -> {
			cfg.customBg = v;
			ThemeManager.applyCustom();
		});
		useCustom = new Button("Apply custom theme", () -> {
			ThemeManager.applyCustom();
			ConfigManager.get().noteChange("Theme: Custom");
		}).primary();
		rebuild();
	}

	private void rebuild() {
		comps.clear();
		comps.add(tabs);
		if (tab == 0) {
			comps.addAll(cards);
		} else {
			comps.add(copyFrom);
			comps.add(accentPicker);
			comps.add(accent2Picker);
			comps.add(bgPicker);
			comps.add(useCustom);
		}
	}

	@Override
	public String title() {
		return "Themes";
	}

	@Override
	public void onShow() {
		super.onShow();
		rebuild();
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		tabs.bounds(x, top, 170, 20);
		int cy = top + 28;
		if (tab == 0) {
			int gap = 8;
			int cw = (usable - gap) / 2;
			int ch = 44;
			for (int i = 0; i < cards.size(); i++) {
				cards.get(i).bounds(x + (i % 2) * (cw + gap), cy, cw, ch);
				if (i % 2 == 1) {
					cy += ch + gap;
				}
			}
			if (cards.size() % 2 == 1) {
				cy += ch + gap;
			}
			return cy;
		}
		copyFrom.bounds(x, cy, usable, 22);
		cy += 32;
		accentPicker.bounds(x, cy, usable, ColorPicker.HEIGHT);
		cy += ColorPicker.HEIGHT + 8;
		accent2Picker.bounds(x, cy, usable, ColorPicker.HEIGHT);
		cy += ColorPicker.HEIGHT + 8;
		bgPicker.bounds(x, cy, usable, ColorPicker.HEIGHT);
		cy += ColorPicker.HEIGHT + 10;
		useCustom.bounds(x, cy, usable, 22);
		return cy + 30;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		Gfx.rightAligned(c, "Current: " + ThemeManager.currentName(), x + w - 14, tabs.y + 6, ThemeManager.dim());
	}
}
