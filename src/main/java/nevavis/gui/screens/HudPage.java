package nevavis.gui.screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.gui.components.Button;
import nevavis.gui.components.Slider;
import nevavis.gui.components.Toggle;
import nevavis.hud.HudElement;
import nevavis.hud.HudManager;

import java.util.ArrayList;
import java.util.List;

/** HUD: переключатели элементов и вход в редактор перетаскивания. */
public final class HudPage extends Page {
	private final Button editor;
	private final Slider scale;
	private final List<Toggle> toggles = new ArrayList<>();

	public HudPage() {
		NevaVisConfig cfg = ConfigManager.get().data();
		editor = new Button("Open HUD editor (drag elements)", () -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			mc.setScreen(new HudEditorScreen(mc.currentScreen));
		});
		editor.primary();
		scale = new Slider("HUD Scale", 0.5f, 2f, 0.05f, () -> cfg.hudScale, v -> cfg.hudScale = v, "%.2fx");
		comps.add(editor);
		comps.add(scale);
		for (HudElement e : HudManager.all()) {
			Toggle t = new Toggle(e.name, () -> e.cfg().visible, v -> e.cfg().visible = v);
			toggles.add(t);
			comps.add(t);
		}
	}

	@Override
	public String title() {
		return "HUD";
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		editor.bounds(x, top, usable, 24);
		scale.bounds(x, top + 30, usable, 26);
		int cy = top + 64;
		for (Toggle t : toggles) {
			t.bounds(x, cy, usable, 24);
			cy += 28;
		}
		return cy + 20;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
	}
}
