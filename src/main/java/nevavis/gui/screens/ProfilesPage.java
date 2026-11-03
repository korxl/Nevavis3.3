package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.gui.components.Button;
import nevavis.gui.components.ProfileRow;
import nevavis.gui.components.SearchBar;
import nevavis.profiles.ProfileManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

import java.util.List;

/** Профили: создать, переключить, дублировать, переименовать, экспорт/импорт (буфер обмена), удалить. */
public final class ProfilesPage extends Page {
	private static final int ROW_H = 30, GAP = 4;

	private final SearchBar nameField = new SearchBar("Profile name (for Create / Rename)...", 24);
	private final Button create, save, importBtn;
	private String lastSig = null;
	private long lastCheck;

	public ProfilesPage() {
		create = new Button("Create", () -> {
			if (ProfileManager.create(nameField.getText())) {
				nameField.clear();
			}
			lastSig = null;
		});
		create.primary();
		save = new Button("Save current", () -> ProfileManager.saveCurrent(true));
		importBtn = new Button("Import (clipboard)", () -> {
			ProfileManager.importFromClipboard();
			lastSig = null;
		});
		rebuild(ProfileManager.names());
	}

	private void rebuild(List<String> names) {
		comps.clear();
		comps.add(nameField);
		comps.add(create);
		comps.add(save);
		comps.add(importBtn);
		for (String n : names) {
			comps.add(new ProfileRow(n, nameField::getText));
		}
	}

	@Override
	public String title() {
		return "Profiles";
	}

	@Override
	public void onShow() {
		super.onShow();
		lastSig = null;
	}

	@Override
	protected void update() {
		long now = System.currentTimeMillis();
		if (lastSig != null && now - lastCheck < 400) {
			return;
		}
		lastCheck = now;
		List<String> names = ProfileManager.names();
		String sig = String.join(",", names) + "|" + ProfileManager.active();
		if (sig.equals(lastSig)) {
			return;
		}
		lastSig = sig;
		rebuild(names);
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		nameField.bounds(x, top, usable, 20);
		int third = (usable - 12) / 3;
		create.bounds(x, top + 26, third, 22);
		save.bounds(x + third + 6, top + 26, third, 22);
		importBtn.bounds(x + 2 * (third + 6), top + 26, third, 22);
		int cy = top + 62;
		for (var comp : comps) {
			if (comp instanceof ProfileRow) {
				comp.bounds(x, cy, usable, ROW_H);
				cy += ROW_H + GAP;
			}
		}
		return cy + 20;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		Gfx.text(c, "PROFILES", x + 2, nameField.y + 26 + 28, ThemeManager.accent());
		Gfx.text(c, "Active: " + ProfileManager.active(), x + w - 14 - Gfx.width("Active: " + ProfileManager.active()),
				nameField.y + 26 + 28, ThemeManager.dim());
	}
}
