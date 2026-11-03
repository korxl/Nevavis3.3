package nevavis.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.notifications.NotificationManager;
import nevavis.render.Icons;

/** Durability Alert: уведомление, когда прочность предмета в руке или брони ниже порога. */
public final class DurabilityAlert extends Module {
	private long lastCheck;
	private String lastWarned = "";

	public DurabilityAlert() {
		super("durability_alert", "Durability Alert",
				"Warns when held item or armor is about to break", ModuleCategory.ITEMS);
		addSetting("percent", "Warn below %", 2f, 30f, 1f, 10f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		long now = System.currentTimeMillis();
		if (mc.player == null || now - lastCheck < 1000) {
			return;
		}
		lastCheck = now;
		float limit = get("percent");
		String warn = "";
		ItemStack[] stacks = {mc.player.getMainHandStack(), mc.player.getInventory().getArmorStack(0),
				mc.player.getInventory().getArmorStack(1), mc.player.getInventory().getArmorStack(2),
				mc.player.getInventory().getArmorStack(3)};
		for (ItemStack s : stacks) {
			if (!s.isEmpty() && s.getMaxDamage() > 0
					&& 100f * (s.getMaxDamage() - s.getDamage()) / s.getMaxDamage() < limit) {
				warn = s.getName().getString();
				break;
			}
		}
		if (!warn.isEmpty() && !warn.equals(lastWarned)) {
			NotificationManager.push("Low durability", warn, Icons.VISUALS);
		}
		lastWarned = warn;
	}
}
