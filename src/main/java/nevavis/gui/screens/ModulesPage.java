package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.gui.components.Slider;
import nevavis.gui.components.Toggle;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

/** Список модулей выбранных категорий (или всех — для поиска). У включённого модуля показываются его настройки. */
public final class ModulesPage extends Page {
	private static final int ROW_H = 24, GAP = 4, SLIDER_H = 26;

	private final String title;
	private final Set<ModuleCategory> categories; // null = все
	private final String emptyMessage;
	private final Supplier<String> query;
	private String lastSig = null;
	private String lastQuery = null;

	public ModulesPage(String title, Set<ModuleCategory> categories, String emptyMessage, Supplier<String> query) {
		this.title = title;
		this.categories = categories;
		this.emptyMessage = emptyMessage;
		this.query = query;
	}

	@Override
	public String title() {
		return title;
	}

	@Override
	public void onShow() {
		super.onShow();
		lastSig = null;
	}

	private boolean matches(Module m, String q) {
		if (categories != null && !categories.contains(m.category())) {
			return false;
		}
		return q.isEmpty() || m.name().toLowerCase(Locale.ROOT).contains(q)
				|| m.description().toLowerCase(Locale.ROOT).contains(q);
	}

	@Override
	protected void update() {
		String q = query.get().toLowerCase(Locale.ROOT).trim();
		StringBuilder sig = new StringBuilder(q).append('#');
		for (Module m : ModuleManager.all()) {
			if (matches(m, q)) {
				sig.append(m.id()).append(m.isEnabled() ? '1' : '0');
			}
		}
		String s = sig.toString();
		if (s.equals(lastSig)) {
			return;
		}
		lastSig = s;
		if (!q.equals(lastQuery)) {
			lastQuery = q;
			scroll.reset();
		}
		comps.clear();
		for (Module m : ModuleManager.all()) {
			if (!matches(m, q)) {
				continue;
			}
			comps.add(new Toggle(m.name(), m::isEnabled, m::setEnabled).tooltip(m.description()));
			if (m.isEnabled()) {
				for (Module.Setting st : m.settings()) {
					comps.add(new Slider(st.label(), st.min(), st.max(), st.step(),
							() -> m.get(st.key()), v -> m.set(st.key(), v), "%.2f", true));
				}
			}
		}
	}

	@Override
	protected int reflow(int top) {
		int cy = top;
		int usable = w - 10;
		for (var comp : comps) {
			if (comp instanceof Slider) {
				comp.bounds(x + 14, cy, usable - 24, SLIDER_H);
				cy += SLIDER_H + GAP;
			} else {
				comp.bounds(x, cy, usable, ROW_H);
				cy += ROW_H + GAP;
			}
		}
		return comps.isEmpty() ? top + h : cy;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		if (!comps.isEmpty()) {
			return;
		}
		String q = lastQuery == null ? "" : lastQuery;
		String msg = q.isEmpty() ? emptyMessage : "Nothing found for \"" + q + "\"";
		int bw = w - 10;
		int bh = 54;
		int by = y + (h - bh) / 2;
		Gfx.glassPanel(c, x, by, bw, bh, Math.min(Gfx.radius(), 12), 0.35f);
		Gfx.centered(c, q.isEmpty() ? "NO MODULES YET" : "NO RESULTS", x + bw / 2, by + 14, ThemeManager.accent());
		Gfx.centered(c, Gfx.fit(msg, bw - 20), x + bw / 2, by + 30, ThemeManager.dim());
	}
}
