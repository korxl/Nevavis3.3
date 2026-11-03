package nevavis.utils;

import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;

/** Плавно приближающееся значение (экспоненциальное сглаживание, не зависит от FPS). */
public final class SmoothValue {
	public float value;
	public float target;

	public SmoothValue(float initial) {
		this.value = initial;
		this.target = initial;
	}

	/** Множитель скорости анимаций из настроек; 1000 = мгновенно (анимации выключены). */
	public static float speedMul() {
		NevaVisConfig c = ConfigManager.get().data();
		return c.guiAnimations ? Math.max(0.1f, c.animationSpeed) : 1000f;
	}

	public void approach(float newTarget, float dt, float speed) {
		target = newTarget;
		float k = speed * speedMul();
		if (k >= 500f) {
			value = newTarget;
			return;
		}
		value += (newTarget - value) * (1f - (float) Math.exp(-k * dt));
		if (Math.abs(newTarget - value) < 0.001f) {
			value = newTarget;
		}
	}

	public void snap(float v) {
		value = v;
		target = v;
	}
}
