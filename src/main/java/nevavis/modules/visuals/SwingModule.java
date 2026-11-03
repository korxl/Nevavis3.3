package nevavis.modules.visuals;

import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;

/** Swing Animation: стиль и скорость взмаха (применяется миксинами HeldItemRenderer / LivingEntity). */
public final class SwingModule extends Module {
	public static final String ID = "swing_animation";

	public SwingModule() {
		super(ID, "Swing Animation", "Changes how your hand swings: style and speed", ModuleCategory.ANIMATIONS);
		addSetting("style", "Style (0 Vanilla, 1 Smooth, 2 Heavy, 3 Whip)", 0f, 3f, 1f, 1f);
		addSetting("speed", "Swing duration x", 0.5f, 2.5f, 0.1f, 1f);
	}

	private static SwingModule active() {
		Module m = ModuleManager.byId(ID);
		return m instanceof SwingModule s && s.isEnabled() ? s : null;
	}

	/** Преобразует прогресс взмаха 0..1 по выбранному стилю. */
	public static float remap(float p) {
		SwingModule m = active();
		if (m == null || p <= 0f || p >= 1f) {
			return p;
		}
		return switch ((int) m.get("style")) {
			case 1 -> p * p * (3f - 2f * p);
			case 2 -> p * p;
			case 3 -> (float) Math.sqrt(p);
			default -> p;
		};
	}

	public static int duration(int vanilla) {
		SwingModule m = active();
		return m == null ? vanilla : Math.max(2, Math.round(vanilla * m.get("speed")));
	}
}
