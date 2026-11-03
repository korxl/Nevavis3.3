package nevavis.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import nevavis.hud.CombatTracker;
import nevavis.hud.HudElement;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

/** Target HUD: голова/скин, ник, HP-полоска, броня, комбо и reach последнего удара. */
public final class TargetHud extends HudElement {
	public TargetHud() {
		super("target", "Target HUD", 0.60f, 0.55f, true);
	}

	@Override
	protected void draw(DrawContext c, MinecraftClient mc) {
		w = 130;
		h = 44;
		LivingEntity t = CombatTracker.target();
		boolean ghost = t == null;
		background(c, w, h);
		if (ghost) {
			Gfx.text(c, "Target HUD", 8, 10, ThemeManager.dim(), cfg().shadow);
			Gfx.text(c, "Hit someone to see info", 8, 24, ThemeManager.dim(), cfg().shadow);
			return;
		}
		// голова
		boolean drawn = false;
		if (t instanceof PlayerEntity p && mc.getNetworkHandler() != null) {
			PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
			if (e != null) {
				PlayerSkinDrawer.draw(c, e.getSkinTextures(), 4, 4, 28);
				drawn = true;
			}
		}
		if (!drawn) {
			Gfx.roundedRect(c, 4, 4, 28, 28, 4, Gfx.accent(0.4f));
			String n = t.getName().getString();
			Gfx.centered(c, n.isEmpty() ? "?" : n.substring(0, 1).toUpperCase(), 18, 14, ThemeManager.text());
		}
		Gfx.text(c, Gfx.fit(t.getName().getString(), w - 42), 38, 5, textColor(), cfg().shadow);

		float hp = t.getHealth();
		float max = Math.max(1f, t.getMaxHealth());
		float frac = Math.max(0f, Math.min(1f, hp / max));
		int barX = 38, barY = 17, barW = w - 46;
		Gfx.roundedRect(c, barX, barY, barW, 6, 3, Colors.withAlpha(0x000000, 0.5f));
		int col = Colors.lerp(0xEF4444, 0x22C55E, frac);
		if (frac > 0f) {
			Gfx.roundedRect(c, barX, barY, Math.max(4, Math.round(barW * frac)), 6, 3, 0xFF000000 | col);
		}
		Gfx.text(c, String.format("%.1f / %.0f HP", hp + t.getAbsorptionAmount(), max), 38, 26, textColor(), cfg().shadow);
		Gfx.text(c, "Armor " + t.getArmor(), 38, 35, ThemeManager.dim(), cfg().shadow);

		int combo = CombatTracker.combo();
		if (combo > 1) {
			String s = "x" + combo;
			Gfx.text(c, s, w - 6 - Gfx.width(s), 35, ThemeManager.accent(), cfg().shadow);
		}
	}
}
