package nevavis.hud;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/** Запоминает последнюю цель удара, комбо и дистанцию (reach) последнего удара. */
public final class CombatTracker {
	private static LivingEntity target;
	private static long lastHit;
	private static int combo;
	private static double reach;

	private CombatTracker() {
	}

	public static void init() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (world.isClient && mc.player != null && player == mc.player && entity instanceof LivingEntity le) {
				long now = System.currentTimeMillis();
				combo = (le == target && now - lastHit < 3000) ? combo + 1 : 1;
				target = le;
				lastHit = now;
				reach = player.getEyePos().distanceTo(le.getBoundingBox().getCenter());
			}
			return ActionResult.PASS;
		});
	}

	public static void tick(MinecraftClient mc) {
		if (mc.player != null && mc.player.hurtTime == mc.player.maxHurtTime - 1 && mc.player.hurtTime > 0) {
			combo = 0;
		}
		if (target != null && (target.isRemoved() || System.currentTimeMillis() - lastHit > 6000)) {
			target = null;
		}
	}

	public static LivingEntity target() {
		return target;
	}

	public static int combo() {
		return System.currentTimeMillis() - lastHit < 3000 ? combo : 0;
	}

	public static double reach() {
		return reach;
	}
}
