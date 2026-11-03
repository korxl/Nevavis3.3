package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.utils.SmoothValue;

/** Базовый компонент UI. Координаты — виртуальные (пространство окна NevaVis). */
public abstract class UiComponent {
	public int x, y, w, h;
	public boolean enabled = true;
	protected String tooltip;
	protected final SmoothValue hover = new SmoothValue(0f);

	public UiComponent bounds(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		return this;
	}

	public UiComponent tooltip(String text) {
		this.tooltip = text;
		return this;
	}

	public boolean contains(double mx, double my) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	/** Кадр: обновляет hover-анимацию и рисует компонент. */
	public final void draw(DrawContext c, double mx, double my, float dt) {
		boolean over = enabled && contains(mx, my);
		hover.approach(over ? 1f : 0f, dt, 14f);
		render(c, mx, my, dt);
		if (over && tooltip != null) {
			Tooltip.show(tooltip);
		}
	}

	protected abstract void render(DrawContext c, double mx, double my, float dt);

	/** Рисуется поверх всего контента, без обрезки (выпадающие списки). */
	public void renderOverlay(DrawContext c, double mx, double my, float dt) {
	}

	/** true — компонент сейчас перехватывает мышь (открытый dropdown). */
	public boolean capturesInput() {
		return false;
	}

	/** true — компонент ждёт ввод с клавиатуры (поиск, выбор клавиши). */
	public boolean wantsKeyboard() {
		return false;
	}

	public boolean mouseClicked(double mx, double my, int button) {
		return false;
	}

	public boolean mouseReleased(double mx, double my, int button) {
		return false;
	}

	public boolean mouseDragged(double mx, double my, int button) {
		return false;
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return false;
	}

	public boolean charTyped(char chr) {
		return false;
	}
}
