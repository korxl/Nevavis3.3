package nevavis.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import nevavis.hud.HudElement;
import nevavis.render.Gfx;

/** Броня: предмет и процент прочности. */
public final class ArmorHud extends HudElement {
	public ArmorHud() {
		super("armor", "Armor Status", 0.01f, 0.6f, false);
	}

	@Override
	protected void draw(DrawContext c, MinecraftClient mc) {
		w = 52;
		h = 4 * 18 + 4;
		background(c, w, h);
		if (mc.player == null) {
			return;
		}
		int y = 3;
		for (int slot = 3; slot >= 0; slot--) {
			ItemStack s = mc.player.getInventory().getArmorStack(slot);
			if (!s.isEmpty()) {
				c.drawItem(s, 3, y);
				if (s.getMaxDamage() > 0) {
					int pct = Math.round(100f * (s.getMaxDamage() - s.getDamage()) / s.getMaxDamage());
					Gfx.text(c, pct + "%", 22, y + 4, textColor(), cfg().shadow);
				}
			}
			y += 18;
		}
	}
}
