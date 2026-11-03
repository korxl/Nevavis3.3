package nevavis.hud;

import net.minecraft.client.MinecraftClient;

/** Статистика сессии и скорость игрока (блоков/с). */
public final class SessionStats {
	private static final long START = System.currentTimeMillis();
	private static double lastX, lastZ;
	private static boolean hasLast;
	private static float speed;
	private static long fpsSum, fpsTicks;
	private static int fpsMin = Integer.MAX_VALUE, fpsMax;

	private SessionStats() {
	}

	public static void tick(MinecraftClient mc) {
		int fps = mc.getCurrentFps();
		if (fps > 0) {
			fpsSum += fps;
			fpsTicks++;
			fpsMin = Math.min(fpsMin, fps);
			fpsMax = Math.max(fpsMax, fps);
		}
		if (mc.player == null) {
			hasLast = false;
			speed = 0f;
			return;
		}
		double x = mc.player.getX(), z = mc.player.getZ();
		if (hasLast) {
			double d = Math.hypot(x - lastX, z - lastZ) * 20.0;
			speed += ((float) d - speed) * 0.3f;
		}
		lastX = x;
		lastZ = z;
		hasLast = true;
	}

	public static float speed() {
		return speed;
	}

	public static long playtimeSeconds() {
		return (System.currentTimeMillis() - START) / 1000L;
	}

	public static int avgFps() {
		return fpsTicks == 0 ? 0 : (int) (fpsSum / fpsTicks);
	}

	public static int minFps() {
		return fpsMin == Integer.MAX_VALUE ? 0 : fpsMin;
	}

	public static int maxFps() {
		return fpsMax;
	}
}
