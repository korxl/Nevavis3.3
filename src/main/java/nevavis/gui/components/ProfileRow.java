package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.profiles.ProfileManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

import java.util.function.Supplier;

/** Строка профиля: имя и кнопки Switch / Copy / Rename / Export / Delete. */
public final class ProfileRow extends UiComponent {
	private static final String[] LABELS = {"Use", "Copy", "Ren", "Exp", "Del"};
	private static final int BTN_W = 34, BTN_H = 18;

	private final String name;
	private final Supplier<String> renameSource;
	private long deleteArmedAt;

	public ProfileRow(String name, Supplier<String> renameSource) {
		this.name = name;
		this.renameSource = renameSource;
	}

	private int btnX(int i) {
		return x + w - 6 - (LABELS.length - i) * (BTN_W + 3) + 3;
	}

	private int btnY() {
		return y + (h - BTN_H) / 2;
	}

	private int hitButton(double mx, double my) {
		if (my < btnY() || my >= btnY() + BTN_H) {
			return -1;
		}
		for (int i = 0; i < LABELS.length; i++) {
			if (mx >= btnX(i) && mx < btnX(i) + BTN_W) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		boolean active = name.equals(ProfileManager.active());
		int r = Math.min(Gfx.radius(), 8);
		Gfx.roundedRect(c, x, y, w, h, r, Colors.lerp(Gfx.surface(0.6f), Gfx.accent(0.3f), active ? 1f : hover.value * 0.5f));
		if (active) {
			Gfx.roundedOutline(c, x, y, w, h, r, Gfx.accent(0.9f));
		}
		int nameMax = btnX(0) - x - 14;
		Gfx.text(c, Gfx.fit(name, nameMax), x + 10, y + (active ? 5 : (h - 8) / 2), ThemeManager.text());
		if (active) {
			Gfx.text(c, "ACTIVE", x + 10, y + 16, ThemeManager.accent());
		}
		boolean armed = System.currentTimeMillis() - deleteArmedAt < 3000;
		int over = hitButton(mx, my);
		for (int i = 0; i < LABELS.length; i++) {
			boolean danger = i == 4 && armed;
			int bg = danger ? Colors.withAlpha(0xEF4444, 0.85f) : (over == i ? Gfx.accent(0.8f) : Gfx.accent(0.3f));
			Gfx.roundedRect(c, btnX(i), btnY(), BTN_W, BTN_H, 5, bg);
			Gfx.centered(c, danger ? "Sure?" : LABELS[i], btnX(i) + BTN_W / 2, btnY() + (BTN_H - 8) / 2, ThemeManager.text());
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != 0 || !enabled || !contains(mx, my)) {
			return false;
		}
		switch (hitButton(mx, my)) {
			case 0 -> ProfileManager.switchTo(name);
			case 1 -> ProfileManager.duplicate(name);
			case 2 -> ProfileManager.rename(name, renameSource.get());
			case 3 -> ProfileManager.export(name);
			case 4 -> {
				if (System.currentTimeMillis() - deleteArmedAt < 3000) {
					ProfileManager.delete(name);
					deleteArmedAt = 0L;
				} else {
					deleteArmedAt = System.currentTimeMillis();
				}
			}
			default -> {
			}
		}
		return true;
	}
}
