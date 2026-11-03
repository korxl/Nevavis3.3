package nevavis.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import nevavis.hud.HudElement;
import nevavis.render.Gfx;

/** Item Counter: количество тотемов, золотых яблок, жемчуга, кристаллов и пузырьков опыта в инвентаре. */
public final class ItemCountHud extends HudElement {
	private static final ItemStack[] ITEMS = {
			new ItemStack(Items.TOTEM_OF_UNDYING), new ItemStack(Items.GOLDEN_APPLE),
			new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.END_CRYSTAL), new ItemStack(Items.EXPERIENCE_BOTTLE)};

	public ItemCountHud() {
		super("itemcount", "Item Counter", 0.01f, 0.45f, false);
	}

	@Override
	protected void draw(DrawContext c, MinecraftClient mc) {
		w = 50;
		h = ITEMS.length * 18 + 4;
		background(c, w, h);
		if (mc.player == null) {
			return;
		}
		int y = 3;
		for (ItemStack icon : ITEMS) {
			int n = 0;
			var inv = mc.player.getInventory();
			for (int i = 0; i < inv.size(); i++) {
				ItemStack s = inv.getStack(i);
				if (s.isOf(icon.getItem())) {
					n += s.getCount();
				}
			}
			c.drawItem(icon, 3, y);
			Gfx.text(c, "x" + n, 24, y + 4, n == 0 ? 0xFF888888 : textColor(), cfg().shadow);
			y += 18;
		}
	}
}
