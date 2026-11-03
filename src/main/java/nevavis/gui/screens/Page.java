package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.gui.components.ScrollContainer;
import nevavis.gui.components.SearchBar;
import nevavis.gui.components.UiComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Страница главного окна. Страница пересчитывает позиции компонентов каждый кадр (reflow),
 * учитывая прокрутку, поэтому компоненты не знают о скролле.
 */
public abstract class Page {
	protected final List<UiComponent> comps = new ArrayList<>();
	protected final ScrollContainer scroll = new ScrollContainer();
	protected int x, y, w, h;

	public abstract String title();

	public void setBounds(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		scroll.setViewport(x, y, w, h);
	}

	/** Вызывается при каждом открытии страницы. */
	public void onShow() {
		scroll.reset();
	}

	/** Раз в кадр до reflow (обновление кэшированных данных). */
	protected void update() {
	}

	/**
	 * Расставляет компоненты, начиная с верхней координаты top (уже со сдвигом прокрутки).
	 * Возвращает нижнюю границу содержимого (абсолютную координату).
	 */
	protected abstract int reflow(int top);

	/** Рисует не-компонентные элементы (заголовки, подписи, пустые состояния). */
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
	}

	public final void render(DrawContext c, double mx, double my, float dt) {
		update();
		scroll.update(dt);
		int top = y - Math.round(scroll.offset());
		int bottom = reflow(top);
		scroll.setContentHeight(bottom - top);

		scroll.begin(c);
		renderBody(c, mx, my, dt);
		boolean inside = scroll.contains(mx, my);
		for (UiComponent comp : comps) {
			// hover только внутри видимой области (или если компонент открыт)
			comp.draw(c, inside || comp.capturesInput() ? mx : -10000, inside || comp.capturesInput() ? my : -10000, dt);
		}
		scroll.end(c);
		scroll.renderBar(c);
		for (UiComponent comp : comps) {
			comp.renderOverlay(c, mx, my, dt);
		}
	}

	public boolean wantsKeyboard() {
		for (UiComponent comp : comps) {
			if (comp.wantsKeyboard()) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseClicked(double mx, double my, int button) {
		for (UiComponent comp : comps) {
			if (comp.capturesInput() && comp.mouseClicked(mx, my, button)) {
				return true;
			}
		}
		boolean inside = scroll.contains(mx, my);
		boolean consumed = false;
		// поля ввода всегда получают клик (чтобы снять фокус при клике в другое место)
		for (UiComponent comp : new ArrayList<>(comps)) {
			if (comp instanceof SearchBar sb && sb.mouseClicked(inside ? mx : -10000, inside ? my : -10000, button)) {
				consumed = true;
			}
		}
		if (consumed) {
			return true;
		}
		if (!inside) {
			return false;
		}
		for (UiComponent comp : new ArrayList<>(comps)) {
			if (!(comp instanceof SearchBar) && comp.mouseClicked(mx, my, button)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseReleased(double mx, double my, int button) {
		boolean used = false;
		for (UiComponent comp : new ArrayList<>(comps)) {
			used |= comp.mouseReleased(mx, my, button);
		}
		return used;
	}

	public boolean mouseDragged(double mx, double my, int button) {
		boolean used = false;
		for (UiComponent comp : new ArrayList<>(comps)) {
			used |= comp.mouseDragged(mx, my, button);
		}
		return used;
	}

	public boolean mouseScrolled(double mx, double my, double amount) {
		if (scroll.contains(mx, my)) {
			scroll.scrollBy(amount);
			return true;
		}
		return false;
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		for (UiComponent comp : comps) {
			if (comp.keyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
		}
		return false;
	}

	public boolean charTyped(char chr) {
		for (UiComponent comp : comps) {
			if (comp.charTyped(chr)) {
				return true;
			}
		}
		return false;
	}
}
