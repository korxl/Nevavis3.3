package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import nevavis.config.ConfigManager;
import nevavis.core.NevaVis;
import nevavis.gui.components.SearchBar;
import nevavis.gui.components.Sidebar;
import nevavis.gui.components.Tooltip;
import nevavis.gui.components.Window;
import nevavis.keybinds.KeybindManager;
import nevavis.modules.ModuleCategory;
import nevavis.render.Gfx;
import nevavis.render.Icons;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Главное окно NevaVis. Рисуется в виртуальном пространстве W x H, которое масштабируется
 * под экран и настройку GUI Scale; мышь пересчитывается обратно.
 */
public final class NevaVisScreen extends Screen {
	public static final int W = 580, H = 340;
	private static final int SIDE = 124, TOP = 36, PAD = 8, TITLE_H = 22;

	private final SmoothValue open = new SmoothValue(0f);
	private final SmoothValue pageFade = new SmoothValue(1f);
	private final Sidebar sidebar;
	private final SearchBar search = new SearchBar("Search modules...");
	private final List<Page> pages = new ArrayList<>();
	private final ModulesPage searchPage;

	private int current;
	private boolean closing;
	private long lastNs = System.nanoTime();
	private float curScale = 1f;
	private boolean wasSearching;

	public NevaVisScreen() {
		super(Text.literal("NevaVis"));

		sidebar = new Sidebar(List.of(
				new Sidebar.Item("Home", Icons.HOME),
				new Sidebar.Item("Visuals", Icons.VISUALS),
				new Sidebar.Item("HUD Editor", Icons.HUD),
				new Sidebar.Item("Music", Icons.MUSIC),
				new Sidebar.Item("Particles", Icons.PARTICLES),
				new Sidebar.Item("Animations", Icons.ANIMATIONS),
				new Sidebar.Item("Environment", Icons.ENVIRONMENT),
				new Sidebar.Item("Crosshair", Icons.CROSSHAIR),
				new Sidebar.Item("Themes", Icons.THEMES),
				new Sidebar.Item("Profiles", Icons.PROFILES),
				new Sidebar.Item("Settings", Icons.SETTINGS),
				new Sidebar.Item("About", Icons.ABOUT)));

		pages.add(new HomePage());
		pages.add(new ModulesPage("Visuals",
				EnumSet.of(ModuleCategory.PLAYER, ModuleCategory.ENTITIES, ModuleCategory.ITEMS),
				"More visual modules are planned.", () -> ""));
		pages.add(new HudPage());
		pages.add(new MusicPage());
		pages.add(new ParticlesPage());
		pages.add(new ModulesPage("Animations",
				EnumSet.of(ModuleCategory.ANIMATIONS),
				"Animation modules are planned.", () -> ""));
		pages.add(new ModulesPage("Environment",
				EnumSet.of(ModuleCategory.WORLD),
				"World modules are planned.", () -> ""));
		pages.add(new CrosshairPage());
		pages.add(new ThemesPage());
		pages.add(new ProfilesPage());
		pages.add(new SettingsPage());
		pages.add(new AboutPage());

		searchPage = new ModulesPage("Search results", null, "Type to search modules.", search::getText);
	}

	// ---------- раскладка ----------

	@Override
	protected void init() {
		int cx = SIDE + PAD;
		int cy = TOP + PAD + TITLE_H;
		int cw = W - SIDE - 2 * PAD;
		int ch = H - cy - PAD;
		for (Page p : pages) {
			p.setBounds(cx, cy, cw, ch);
		}
		searchPage.setBounds(cx, cy, cw, ch);
		search.bounds(SIDE + PAD, 8, 170, 20);
		sidebar.bounds(0, TOP + 6, SIDE);
		sidebar.select(current);
		pages.get(current).onShow();
	}

	private void setPage(int index) {
		if (index == current) {
			return;
		}
		current = index;
		sidebar.select(index);
		pages.get(index).onShow();
		pageFade.snap(0f);
	}

	private boolean searching() {
		return !search.getText().isEmpty();
	}

	private Page activePage() {
		return searching() ? searchPage : pages.get(current);
	}

	// ---------- координаты ----------

	private double vx(double mx) {
		return (mx - width / 2.0) / curScale + W / 2.0;
	}

	private double vy(double my) {
		return (my - height / 2.0) / curScale + H / 2.0;
	}

	// ---------- отрисовка ----------

	/** Фон рисуем сами (затемнение), без ванильного размытия меню. */
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void render(DrawContext c, int mouseX, int mouseY, float delta) {
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1_000_000_000f);
		lastNs = now;

		open.approach(closing ? 0f : 1f, dt, 11f);
		ThemeManager.update(dt);
		float o = open.value;

		// затемнение фона
		int dark = (int) (150 * o);
		if (dark > 0) {
			c.fill(0, 0, width, height, dark << 24);
		}

		float fit = Math.min((width - 16f) / W, (height - 16f) / H);
		float auto = Math.min(fit * 0.92f, 1.5f);
		float base = Math.max(0.3f, Math.min(auto * ConfigManager.get().data().guiScale, fit));
		curScale = base * (0.9f + 0.1f * o);
		Gfx.setTransform(width / 2f, height / 2f, curScale, W, H);

		MatrixStack m = c.getMatrices();
		m.push();
		m.translate(width / 2f, height / 2f, 0f);
		m.scale(curScale, curScale, 1f);
		m.translate(-W / 2f, -H / 2f, 0f);

		double mx = vx(mouseX), my = vy(mouseY);
		Gfx.setFade(o);
		Tooltip.clear();

		Window.draw(c, 0, 0, W, H);
		renderTopBar(c, mx, my, dt);
		Gfx.fill(c, SIDE, TOP, SIDE + 1, H - 10, Gfx.accent(0.18f));
		sidebar.render(c, mx, my, dt);

		// заголовок страницы и сама страница
		boolean s = searching();
		if (s != wasSearching) {
			wasSearching = s;
			pageFade.snap(0f);
			if (s) {
				searchPage.onShow();
			}
		}
		pageFade.approach(1f, dt, 12f);
		Page page = activePage();
		Gfx.setFade(o * pageFade.value);
		Gfx.text(c, page.title().toUpperCase(java.util.Locale.ROOT), SIDE + PAD + 2, TOP + PAD, ThemeManager.text(), true);
		Gfx.fill(c, SIDE + PAD + 2, TOP + PAD + 12, SIDE + PAD + 26, TOP + PAD + 13, ThemeManager.accent());
		page.render(c, mx, my, dt);

		Gfx.setFade(o);
		Tooltip.renderAndClear(c, mx, my, W, H);
		m.pop();
		Gfx.setFade(1f);
	}

	private void renderTopBar(DrawContext c, double mx, double my, float dt) {
		// логотип с бегущим градиентом
		String logo = "NEVAVIS";
		int lx = 14;
		float phase = (System.currentTimeMillis() % 3000L) / 3000f;
		for (int i = 0; i < logo.length(); i++) {
			float t = 0.5f + 0.5f * (float) Math.sin((phase + i / 7f) * Math.PI * 2);
			String ch = String.valueOf(logo.charAt(i));
			Gfx.text(c, ch, lx, 14, Colors.lerp(ThemeManager.accent(), ThemeManager.accent2(), t), true);
			lx += Gfx.width(ch) + 1;
		}
		Gfx.text(c, "v" + NevaVis.VERSION(), lx + 6, 14, ThemeManager.dim());
		Gfx.fill(c, 10, TOP - 1, W - 10, TOP, Gfx.accent(0.18f));

		search.draw(c, mx, my, dt);

		// справа налево: тема, статус, FPS
		int rx = W - 14;
		String theme = ThemeManager.currentName();
		Gfx.rightAligned(c, theme, rx, 14, ThemeManager.text());
		rx -= Gfx.width(theme) + 14;
		String status = "Active";
		Gfx.rightAligned(c, status, rx, 14, ThemeManager.dim());
		rx -= Gfx.width(status) + 4;
		Gfx.roundedRect(c, rx - 5, 16, 5, 5, 2, 0xFF4ADE80);
		rx -= 18;
		String fps = net.minecraft.client.MinecraftClient.getInstance().getCurrentFps() + " FPS";
		Gfx.rightAligned(c, fps, rx, 14, ThemeManager.accent());
	}

	// ---------- закрытие ----------

	/** Запускает анимацию закрытия; реальное закрытие — в tick(). */
	@Override
	public void close() {
		closing = true;
	}

	@Override
	public void tick() {
		if (closing && open.value < 0.03f) {
			super.close();
		}
	}

	@Override
	public void removed() {
		ConfigManager.get().save();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	// ---------- ввод ----------

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (closing) {
			return true;
		}
		double mx = vx(mouseX), my = vy(mouseY);
		return handleClick(activePage(), mx, my, button);
	}

	private boolean handleClick(Page page, double mx, double my, int button) {
		if (search.mouseClicked(mx, my, button)) {
			return true;
		}
		if (button == 0) {
			int idx = sidebar.hit(mx, my);
			if (idx >= 0) {
				search.clear();
				setPage(idx);
				return true;
			}
		}
		return page.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return activePage().mouseReleased(vx(mouseX), vy(mouseY), button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return activePage().mouseDragged(vx(mouseX), vy(mouseY), button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return activePage().mouseScrolled(vx(mouseX), vy(mouseY), verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		Page page = activePage();
		// выбор клавиши и ввод текста имеют приоритет над закрытием
		if (page.wantsKeyboard() && page.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
		if (search.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
		boolean typing = search.isFocused() || page.wantsKeyboard();
		if (!typing && KeybindManager.openGui().matchesKey(keyCode, scanCode)) {
			close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (search.charTyped(chr)) {
			return true;
		}
		return activePage().charTyped(chr);
	}
}
