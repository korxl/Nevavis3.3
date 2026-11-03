package nevavis.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig.HudElementConfig;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

import java.util.List;

/** Элемент HUD. Рисуется в локальных координатах (0,0 — левый верхний угол), размер w/h задаёт draw(). */
public abstract class HudElement {
	public final String id;
	public final String name;
	private final float defX, defY;
	private final boolean defVisible;
	public int w = 60, h = 16;

	protected HudElement(String id, String name, float defX, float defY, boolean defVisible) {
		this.id = id;
		this.name = name;
		this.defX = defX;
		this.defY = defY;
		this.defVisible = defVisible;
	}

	public HudElementConfig cfg() {
		return ConfigManager.get().data().hud.computeIfAbsent(id, k -> {
			HudElementConfig c = new HudElementConfig();
			c.visible = defVisible;
			c.fx = defX;
			c.fy = defY;
			return c;
		});
	}

	public void resetPosition() {
		HudElementConfig c = cfg();
		c.fx = defX;
		c.fy = defY;
		c.scale = 1f;
	}

	public float scale() {
		return cfg().scale * ConfigManager.get().data().hudScale;
	}

	/** Нарисовать содержимое в локальных координатах и обновить w/h. */
	protected abstract void draw(DrawContext c, MinecraftClient mc);

	/** ghost = рисовать даже скрытый элемент (приглушённо) — для редактора. */
	public final void render(DrawContext c, MinecraftClient mc, boolean ghost) {
		HudElementConfig e = cfg();
		if (!e.visible && !ghost) {
			return;
		}
		MatrixStack m = c.getMatrices();
		m.push();
		m.translate(e.fx * c.getScaledWindowWidth(), e.fy * c.getScaledWindowHeight(), 0f);
		float s = scale();
		m.scale(s, s, 1f);
		Gfx.setFade(e.visible ? 1f : 0.35f);
		draw(c, mc);
		Gfx.setFade(1f);
		m.pop();
	}

	// ---- экранные границы (для редактора) ----

	public int ax(int sw) {
		return Math.round(cfg().fx * sw);
	}

	public int ay(int sh) {
		return Math.round(cfg().fy * sh);
	}

	public int aw() {
		return Math.max(8, Math.round(w * scale()));
	}

	public int ah() {
		return Math.max(8, Math.round(h * scale()));
	}

	public boolean hit(double mx, double my, int sw, int sh) {
		int x = ax(sw), y = ay(sh);
		return mx >= x && mx < x + aw() && my >= y && my < y + ah();
	}

	// ---- помощники рисования ----

	protected int textColor() {
		HudElementConfig e = cfg();
		return Colors.withAlpha(0xFF000000 | e.color, e.alpha);
	}

	protected void background(DrawContext c, int bw, int bh) {
		HudElementConfig e = cfg();
		if (!e.background) {
			return;
		}
		int r = Math.min(4, Gfx.radius());
		Gfx.roundedRect(c, 0, 0, bw, bh, r, Colors.withAlpha(ThemeManager.bg(), 0.55f * e.alpha));
		Gfx.roundedOutline(c, 0, 0, bw, bh, r, Gfx.accent(0.5f * e.alpha));
	}

	protected void drawLines(DrawContext c, List<String> lines) {
		int tw = 0;
		for (String s : lines) {
			tw = Math.max(tw, Gfx.width(s));
		}
		w = tw + 10;
		h = lines.size() * 10 + 6;
		background(c, w, h);
		int y = 4;
		boolean shadow = cfg().shadow;
		int col = textColor();
		for (String s : lines) {
			Gfx.text(c, s, 5, y, col, shadow);
			y += 10;
		}
	}
}
