package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import nevavis.render.Gfx;
import nevavis.utils.SmoothValue;

/**
 * Область прокрутки с плавным скроллом и обрезкой (scissor).
 * Страницы пересчитывают позиции компонентов каждый кадр с учётом offset().
 */
public final class ScrollContainer {
	private int vx, vy, vw, vh;
	private float contentH;
	private float target;
	private float value;

	public void setViewport(int x, int y, int w, int h) {
		vx = x;
		vy = y;
		vw = w;
		vh = h;
	}

	public void setContentHeight(float h) {
		contentH = h;
		target = Math.max(0f, Math.min(target, maxScroll()));
	}

	public float maxScroll() {
		return Math.max(0f, contentH - vh);
	}

	public float offset() {
		return value;
	}

	public void reset() {
		target = 0f;
		value = 0f;
	}

	public boolean contains(double mx, double my) {
		return mx >= vx && mx < vx + vw && my >= vy && my < vy + vh;
	}

	public void scrollBy(double wheelAmount) {
		target = (float) Math.max(0, Math.min(maxScroll(), target - wheelAmount * 24.0));
	}

	public void update(float dt) {
		float k = 18f * SmoothValue.speedMul();
		if (k >= 500f) {
			value = target;
		} else {
			value += (target - value) * (1f - (float) Math.exp(-k * dt));
			if (Math.abs(target - value) < 0.05f) {
				value = target;
			}
		}
		value = Math.max(0f, Math.min(value, maxScroll()));
	}

	/** Включает обрезку по области. Координаты scissor переводятся в экранные вручную. */
	public void begin(DrawContext c) {
		int sx1 = (int) Math.floor(Gfx.sx(vx));
		int sy1 = (int) Math.floor(Gfx.sy(vy));
		int sx2 = (int) Math.ceil(Gfx.sx(vx + vw));
		int sy2 = (int) Math.ceil(Gfx.sy(vy + vh));
		MatrixStack m = c.getMatrices();
		m.push();
		m.loadIdentity(); // scissor задаём в экранных координатах, поэтому без текущей трансформации
		c.enableScissor(sx1, sy1, sx2, sy2);
		m.pop();
	}

	public void end(DrawContext c) {
		c.disableScissor();
	}

	public void renderBar(DrawContext c) {
		float max = maxScroll();
		if (max <= 0f) {
			return;
		}
		int barH = Math.max(16, Math.round(vh * (vh / contentH)));
		int barY = vy + Math.round((vh - barH) * (value / max));
		int bx = vx + vw - 3;
		Gfx.roundedRect(c, bx, vy, 3, vh, 1, Gfx.accent(0.10f));
		Gfx.roundedGradient(c, bx, barY, 3, barH, 1, Gfx.accent(0.9f), Gfx.accent2(0.9f));
	}
}
