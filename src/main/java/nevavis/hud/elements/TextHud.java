package nevavis.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.hud.HudElement;

import java.util.List;
import java.util.function.Supplier;

/** Текстовый элемент HUD: строки приходят из лямбды. */
public final class TextHud extends HudElement {
	private final Supplier<List<String>> lines;

	public TextHud(String id, String name, float x, float y, boolean visible, Supplier<List<String>> lines) {
		super(id, name, x, y, visible);
		this.lines = lines;
	}

	@Override
	protected void draw(DrawContext c, MinecraftClient mc) {
		List<String> ls = lines.get();
		if (ls.isEmpty()) {
			ls = List.of(name);
		}
		drawLines(c, ls);
	}
}
