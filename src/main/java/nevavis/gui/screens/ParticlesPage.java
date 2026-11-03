package nevavis.gui.screens;

import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig.ParticleConfig;
import nevavis.gui.components.Button;
import nevavis.gui.components.Dropdown;
import nevavis.gui.components.Slider;
import nevavis.gui.components.Toggle;
import nevavis.particles.ParticleEngine;
import nevavis.particles.ParticleEngine.Fx;

import java.util.ArrayList;
import java.util.List;

/** Настройки частиц: пресеты, качество, параметры и список эффектов. */
public final class ParticlesPage extends Page {
	private static final List<String> PRESETS = List.of("Soft", "Intense", "Minimal", "Cinematic");

	private final Dropdown preset, quality;
	private final Slider amount, speed, spread, gravity, glow;
	private final Button disableAll;
	private final List<Toggle> toggles = new ArrayList<>();
	private String lastPreset = "Choose preset...";

	private static ParticleConfig p() {
		return ConfigManager.get().data().particles;
	}

	public ParticlesPage() {
		preset = new Dropdown("Preset", PRESETS, () -> lastPreset, this::applyPreset);
		quality = new Dropdown("Quality", List.of("Low", "Medium", "High"), () -> p().quality, q -> p().quality = q);
		amount = new Slider("Amount", 0.2f, 4f, 0.1f, () -> p().amount, v -> p().amount = v, "%.1fx");
		speed = new Slider("Speed", 0.2f, 3f, 0.1f, () -> p().speed, v -> p().speed = v, "%.1fx");
		spread = new Slider("Spread", 0.3f, 3f, 0.1f, () -> p().spread, v -> p().spread = v, "%.1fx");
		gravity = new Slider("Gravity", -1f, 2f, 0.1f, () -> p().gravity, v -> p().gravity = v, "%.1f");
		glow = new Slider("Glow", 0f, 1f, 0.05f, () -> p().glow, v -> p().glow = v, "%.2f");
		disableAll = new Button("Disable all effects", () -> p().enabled.clear());
		for (Fx fx : Fx.values()) {
			toggles.add(new Toggle(fx.label, () -> ParticleEngine.isOn(fx), v -> ParticleEngine.setOn(fx, v)));
		}
		comps.add(amount);
		comps.add(speed);
		comps.add(spread);
		comps.add(gravity);
		comps.add(glow);
		comps.add(disableAll);
		comps.addAll(toggles);
		comps.add(preset);
		comps.add(quality);
	}

	private void applyPreset(String name) {
		ParticleConfig c = p();
		switch (name) {
			case "Soft" -> set(c, 0.6f, 0.7f, 1.2f, -0.3f, 0.4f);
			case "Intense" -> set(c, 2.5f, 1.5f, 1.5f, 0.2f, 0.6f);
			case "Minimal" -> set(c, 0.4f, 0.8f, 0.7f, 0f, 0f);
			default -> set(c, 1.2f, 0.5f, 2f, -0.5f, 0.9f);
		}
		lastPreset = name;
		ConfigManager.get().noteChange("Particle preset: " + name);
	}

	private static void set(ParticleConfig c, float amount, float speed, float spread, float gravity, float glow) {
		c.amount = amount;
		c.speed = speed;
		c.spread = spread;
		c.gravity = gravity;
		c.glow = glow;
	}

	@Override
	public String title() {
		return "Particles";
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		int cy = top;
		// Dropdown'ы идут последними в списке comps, чтобы их выпадающие списки рисовались поверх,
		// но по координатам они стоят вверху страницы.
		preset.bounds(x, cy, usable, 22);
		cy += 26;
		quality.bounds(x, cy, usable, 22);
		cy += 30;
		for (Slider s : new Slider[]{amount, speed, spread, gravity, glow}) {
			s.bounds(x, cy, usable, 26);
			cy += 30;
		}
		disableAll.bounds(x, cy, usable, 22);
		cy += 32;
		for (Toggle t : toggles) {
			t.bounds(x, cy, usable, 22);
			cy += 25;
		}
		return cy + 6;
	}
}
