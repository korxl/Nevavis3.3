package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.SmoothValue;
import org.lwjgl.glfw.GLFW;

public final class SearchBar extends UiComponent {

	private final String placeholder;
	private final int maxLen;
	private String text = "";
	private boolean focused;
	private final SmoothValue focus = new SmoothValue(0f);

	public SearchBar(String placeholder) {
		this(placeholder, 32);
	}

	public SearchBar(String placeholder, int maxLen) {
		this.placeholder = placeholder;
		this.maxLen = maxLen;
	}

	public void setText(String t) {
		text = t == null ? "" : (t.length() > maxLen ? t.substring(0, maxLen) : t);
	}

	public String getText() {
		return text;
	}

	public boolean isFocused() {
		return focused;
	}

	public void clear() {
		text = "";
	}

	@Override
	public boolean wantsKeyboard() {
		return focused;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		focus.approach(focused ? 1f : 0f, dt, 14f);
		int r = Math.min(Gfx.radius(), h / 2);
		float f = focus.value;
		Gfx.glow(c, x, y, w, h, r, nevavis.themes.ThemeManager.accent(), f * 0.7f);
		Gfx.roundedRect(c, x, y, w, h, r, Gfx.surface(0.7f + 0.2f * f));
		Gfx.roundedOutline(c, x, y, w, h, r, Gfx.accent(0.30f + 0.6f * f));

		int ty = y + (h - 8) / 2;
		int maxW = w - 16;
		if (text.isEmpty() && !focused) {
			Gfx.text(c, placeholder, x + 8, ty, ThemeManager.dim());
		} else {
			String shown = text;
			while (shown.length() > 1 && Gfx.width(shown) > maxW) {
				shown = shown.substring(1);
			}
			Gfx.text(c, shown, x + 8, ty, ThemeManager.text());
			if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
				Gfx.fill(c, x + 8 + Gfx.width(shown), ty - 1, x + 9 + Gfx.width(shown), ty + 9, ThemeManager.accent());
			}
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		focused = enabled && button == 0 && contains(mx, my);
		return focused;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!focused) {
			return false;
		}
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			focused = false;
			return true;
		}
		boolean ctrl = (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
		if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
			String clip = net.minecraft.client.MinecraftClient.getInstance().keyboard.getClipboard();
			if (clip != null) {
				StringBuilder sb = new StringBuilder(text);
				for (char ch : clip.toCharArray()) {
					if (ch >= 32 && ch != 127 && sb.length() < maxLen) {
						sb.append(ch);
					}
				}
				text = sb.toString();
			}
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			if (!text.isEmpty()) {
				text = text.substring(0, text.length() - 1);
			}
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			focused = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(char chr) {
		if (!focused || chr < 32 || chr == 127 || text.length() >= maxLen) {
			return false;
		}
		text += chr;
		return true;
	}
}
