package nevavis.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import nevavis.hud.ClickTracker;
import nevavis.hud.HudElement;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

/** Клавиши WASD, пробел и кнопки мыши с CPS. */
public final class KeystrokesHud extends HudElement {
	private static final int K = 20, G = 2;

	public KeystrokesHud() {
		super("keystrokes", "Keystrokes", 0.01f, 0.45f, false);
	}

	private void key(DrawContext c, int x, int y, int kw, int kh, String label, boolean down) {
		int r = Math.min(4, Gfx.radius());
		var e = cfg();
		Gfx.roundedRect(c, x, y, kw, kh, r, down ? Gfx.accent(0.75f * e.alpha) : Colors.withAlpha(ThemeManager.bg(), 0.55f * e.alpha));
		Gfx.roundedOutline(c, x, y, kw, kh, r, Gfx.accent((down ? 1f : 0.5f) * e.alpha));
		Gfx.text(c, label, x + (kw - Gfx.width(label)) / 2, y + (kh - 8) / 2, textColor(), e.shadow);
	}

	@Override
	protected void draw(DrawContext c, MinecraftClient mc) {
		GameOptions o = mc.options;
		w = K * 3 + G * 2;
		h = K * 3 + G * 2 + 4;
		key(c, K + G, 0, K, K, "W", o.forwardKey.isPressed());
		key(c, 0, K + G, K, K, "A", o.leftKey.isPressed());
		key(c, K + G, K + G, K, K, "S", o.backKey.isPressed());
		key(c, 2 * (K + G), K + G, K, K, "D", o.rightKey.isPressed());
		int half = (w - G) / 2;
		key(c, 0, 2 * (K + G), half, K, "LMB " + ClickTracker.left(), o.attackKey.isPressed());
		key(c, half + G, 2 * (K + G), half, K, "RMB " + ClickTracker.right(), o.useKey.isPressed());
	}
}
