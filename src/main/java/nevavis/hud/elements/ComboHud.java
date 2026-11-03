package nevavis.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.hud.CombatTracker;
import nevavis.hud.HudElement;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

/** Combo & Reach: серия ударов подряд и дистанция последнего удара. */
public final class ComboHud extends HudElement {
	public ComboHud() {
		super("combo", "Combo & Reach", 0.60f, 0.46f, false);
	}

	@Override
	protected void draw(DrawContext c, MinecraftClient mc) {
		w = 96;
		h = 28;
		background(c, w, h);
		Gfx.text(c, "Combo: " + CombatTracker.combo(), 6, 4, textColor(), cfg().shadow);
		double r = CombatTracker.reach();
		Gfx.text(c, r > 0 ? String.format("Reach: %.2f", r) : "Reach: -", 6, 15, ThemeManager.dim(), cfg().shadow);
	}
}
