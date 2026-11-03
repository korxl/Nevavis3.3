package nevavis.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import nevavis.config.ConfigManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import org.lwjgl.glfw.GLFW;

/** Выбор клавиши для KeyBinding: клик → нажмите клавишу (Esc — отмена). */
public final class KeybindSelector extends UiComponent {
	private static final int BOX_W = 110;

	private final String label;
	private final KeyBinding binding;
	private boolean capturing;

	public KeybindSelector(String label, KeyBinding binding) {
		this.label = label;
		this.binding = binding;
	}

	@Override
	public boolean wantsKeyboard() {
		return capturing;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		int r = Math.min(Gfx.radius(), h / 2);
		Gfx.roundedRect(c, x, y, w, h, r, Gfx.surface(0.55f));
		Gfx.text(c, label, x + 8, y + (h - 8) / 2, ThemeManager.text());
		int bx = x + w - BOX_W - 6;
		int by = y + 3;
		int bh = h - 6;
		float hv = Math.max(hover.value, capturing ? 1f : 0f);
		Gfx.glow(c, bx, by, BOX_W, bh, r, ThemeManager.accent(), capturing ? 0.8f : 0f);
		Gfx.roundedRect(c, bx, by, BOX_W, bh, r, Gfx.surface(0.9f));
		Gfx.roundedOutline(c, bx, by, BOX_W, bh, r, Gfx.accent(0.35f + 0.6f * hv));
		String s = capturing ? "Press a key..." : binding.getBoundKeyLocalizedText().getString();
		Gfx.centered(c, Gfx.fit(s, BOX_W - 8), bx + BOX_W / 2, by + (bh - 8) / 2,
				capturing ? ThemeManager.accent() : ThemeManager.text());
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (!enabled) {
			return false;
		}
		if (capturing) {
			capturing = false;
			return true;
		}
		if (button == 0 && contains(mx, my)) {
			capturing = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!capturing) {
			return false;
		}
		capturing = false;
		if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
			binding.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
			KeyBinding.updateKeysByCode();
			MinecraftClient.getInstance().options.write();
			ConfigManager.get().noteChange(label + ": " + binding.getBoundKeyLocalizedText().getString());
		}
		return true;
	}
}
