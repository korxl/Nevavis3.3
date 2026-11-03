package nevavis.particles;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig.ParticleConfig;

import java.util.Random;

/**
 * Движок частиц: излучатели вокруг игрока, которые создают ванильные частицы в мире.
 * Параметры (количество, скорость, разброс, гравитация, свечение, качество) применяются ко всем эффектам.
 */
public final class ParticleEngine {
	public enum Fx {
		HEARTS("Hearts"), STARS("Stars"), SNOW("Snow"), FIRE("Fire"), SPARKS("Sparks"), GLOW("Glow"),
		CIRCLES("Circles"), RAINBOW("Rainbow notes"), MUSIC("Music notes"), ENCHANT("Enchantment"),
		WALKING("Walking"), TRAIL("Trail"), LANDING("Landing"), HIT("Hit"), CRIT("Critical");

		public final String label;

		Fx(String label) {
			this.label = label;
		}
	}

	private static final Random R = new Random();
	private static final double TAU = Math.PI * 2.0;

	private static int tick;
	private static boolean lastSwing, lastGround = true;
	private static float airFall;
	private static double lastX, lastZ;
	private static boolean hasLast;

	private ParticleEngine() {
	}

	private static ParticleConfig cfg() {
		return ConfigManager.get().data().particles;
	}

	public static boolean isOn(Fx fx) {
		return cfg().enabled.contains(fx.name());
	}

	public static void setOn(Fx fx, boolean on) {
		var list = cfg().enabled;
		if (on && !list.contains(fx.name())) {
			list.add(fx.name());
		} else if (!on) {
			list.remove(fx.name());
		}
	}

	private static float quality() {
		float q = switch (cfg().quality) {
			case "Low" -> 0.4f;
			case "High" -> 1.6f;
			default -> 1f;
		};
		return q * ConfigManager.get().perf();
	}

	/** Случайно округляет base * amount * quality до целого числа частиц. */
	private static int n(float base) {
		float v = base * cfg().amount * quality();
		int i = (int) v;
		return R.nextFloat() < v - i ? i + 1 : i;
	}

	private static void add(ClientWorld w, ParticleEffect type, double x, double y, double z, double vx, double vy, double vz) {
		ParticleConfig c = cfg();
		double sp = c.speed;
		w.addParticle(type, x, y, z, vx * sp, vy * sp - 0.02 * c.gravity, vz * sp);
		if (c.glow > 0f && R.nextFloat() < c.glow * 0.3f) {
			w.addParticle(ParticleTypes.GLOW, x, y, z, 0, 0.01, 0);
		}
	}

	private static double rnd() {
		return (R.nextDouble() - 0.5) * 2.0;
	}

	public static void tick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		ClientWorld w = mc.world;
		if (p == null || w == null || mc.isPaused()) {
			hasLast = false;
			return;
		}
		ParticleConfig c = cfg();
		tick++;

		// события
		boolean swing = p.handSwinging;
		boolean newSwing = swing && !lastSwing;
		lastSwing = swing;
		boolean ground = p.isOnGround();
		if (!ground) {
			airFall = Math.max(airFall, p.fallDistance);
		}
		boolean landed = ground && !lastGround && airFall > 1.5f;
		if (ground) {
			if (landed) {
				// airFall сбрасывается после обработки ниже
			} else {
				airFall = 0f;
			}
		}
		lastGround = ground;

		double px = p.getX(), py = p.getY(), pz = p.getZ();
		double moved = hasLast ? Math.hypot(px - lastX, pz - lastZ) : 0.0;
		lastX = px;
		lastZ = pz;
		hasLast = true;
		boolean moving = moved > 0.05;
		float spread = c.spread;

		if (c.enabled.isEmpty()) {
			if (landed) {
				airFall = 0f;
			}
			return;
		}

		if (isOn(Fx.HEARTS) && tick % 8 == 0) {
			for (int i = n(1f); i > 0; i--) {
				add(w, ParticleTypes.HEART, px + rnd() * 0.4 * spread, py + 2.0, pz + rnd() * 0.4 * spread, 0, 0.05, 0);
			}
		}
		if (isOn(Fx.STARS)) {
			for (int i = n(0.5f); i > 0; i--) {
				add(w, ParticleTypes.END_ROD, px + rnd() * 1.2 * spread, py + R.nextDouble() * 2.0, pz + rnd() * 1.2 * spread, 0, 0.02, 0);
			}
		}
		if (isOn(Fx.SNOW)) {
			for (int i = n(2f); i > 0; i--) {
				add(w, ParticleTypes.SNOWFLAKE, px + rnd() * 3.0 * spread, py + 2.5 + R.nextDouble(), pz + rnd() * 3.0 * spread, 0, -0.04, 0);
			}
		}
		if (isOn(Fx.FIRE)) {
			for (int i = n(0.6f); i > 0; i--) {
				add(w, ParticleTypes.FLAME, px + rnd() * 0.6 * spread, py + 0.1, pz + rnd() * 0.6 * spread, 0, 0.02, 0);
			}
		}
		if (isOn(Fx.SPARKS)) {
			for (int i = n(0.8f); i > 0; i--) {
				add(w, ParticleTypes.ELECTRIC_SPARK, px + rnd() * 0.8 * spread, py + R.nextDouble() * 1.8, pz + rnd() * 0.8 * spread,
						rnd() * 0.1, rnd() * 0.1, rnd() * 0.1);
			}
		}
		if (isOn(Fx.GLOW)) {
			for (int i = n(0.6f); i > 0; i--) {
				add(w, ParticleTypes.GLOW, px + rnd() * 0.9 * spread, py + R.nextDouble() * 1.9, pz + rnd() * 0.9 * spread, 0, 0.01, 0);
			}
		}
		if (isOn(Fx.CIRCLES)) {
			int pts = Math.max(1, n(3f));
			double base = tick * 0.15 * c.speed;
			for (int i = 0; i < pts; i++) {
				double a = base + i * TAU / pts;
				add(w, ParticleTypes.END_ROD, px + Math.cos(a) * 0.9 * spread, py + 0.1, pz + Math.sin(a) * 0.9 * spread, 0, 0, 0);
			}
		}
		if (isOn(Fx.RAINBOW)) {
			for (int i = n(1f); i > 0; i--) {
				double a = tick * 0.25;
				double yy = (tick % 40) / 40.0 * 1.9;
				double hue = (tick * 0.02 + i * 0.1) % 1.0;
				w.addParticle(ParticleTypes.NOTE, px + Math.cos(a) * 0.7 * spread, py + yy, pz + Math.sin(a) * 0.7 * spread, hue, 0, 0);
			}
		}
		if (isOn(Fx.MUSIC) && tick % 10 == 0) {
			for (int i = n(1f); i > 0; i--) {
				w.addParticle(ParticleTypes.NOTE, px + rnd() * 0.4 * spread, py + 2.1, pz + rnd() * 0.4 * spread, R.nextDouble(), 0, 0);
			}
		}
		if (isOn(Fx.ENCHANT)) {
			for (int i = n(2f); i > 0; i--) {
				double rx = rnd() * 1.5 * spread, ry = R.nextDouble() * 1.0, rz = rnd() * 1.5 * spread;
				w.addParticle(ParticleTypes.ENCHANT, px + rx, py + 1.8 + ry, pz + rz, -rx, -ry - 0.8, -rz);
			}
		}
		if (isOn(Fx.WALKING) && moving && ground) {
			for (int i = n(1f); i > 0; i--) {
				add(w, ParticleTypes.CLOUD, px + rnd() * 0.3 * spread, py + 0.05, pz + rnd() * 0.3 * spread, 0, 0.02, 0);
			}
		}
		if (isOn(Fx.TRAIL) && moving) {
			for (int i = n(2f); i > 0; i--) {
				add(w, ParticleTypes.FIREWORK, px + rnd() * 0.2 * spread, py + 0.2 + R.nextDouble() * 0.4, pz + rnd() * 0.2 * spread, 0, 0, 0);
			}
		}
		if (isOn(Fx.LANDING) && landed) {
			int count = Math.max(4, n(12f));
			for (int i = 0; i < count; i++) {
				double a = i * TAU / count;
				add(w, ParticleTypes.CLOUD, px, py + 0.1, pz, Math.cos(a) * 0.12 * spread, 0.02, Math.sin(a) * 0.12 * spread);
			}
		}
		if (landed) {
			airFall = 0f;
		}
		if (newSwing && mc.crosshairTarget instanceof EntityHitResult hit) {
			var pos = hit.getPos();
			if (isOn(Fx.HIT)) {
				for (int i = n(8f); i > 0; i--) {
					add(w, ParticleTypes.ENCHANTED_HIT, pos.x, pos.y, pos.z, rnd() * 0.2, rnd() * 0.2, rnd() * 0.2);
				}
			}
			boolean crit = p.fallDistance > 0f && !ground && !p.isClimbing() && !p.isTouchingWater();
			if (isOn(Fx.CRIT) && crit) {
				for (int i = n(10f); i > 0; i--) {
					add(w, ParticleTypes.CRIT, pos.x, pos.y, pos.z, rnd() * 0.3, rnd() * 0.3, rnd() * 0.3);
				}
			}
		}
	}
}
