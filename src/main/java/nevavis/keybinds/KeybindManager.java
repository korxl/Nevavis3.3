package nevavis.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import nevavis.gui.screens.NevaVisScreen;
import nevavis.music.MusicPlayer;
import org.lwjgl.glfw.GLFW;

/**
 * Клавиши NevaVis. Open GUI — Right Shift по умолчанию (меняется в GUI: Settings → Keybinds,
 * или в Управление → NevaVis). Клавиши музыки по умолчанию не назначены — назначьте в Управление → NevaVis.
 */
public final class KeybindManager {
	public static final String CATEGORY = "category.nevavis";

	private static KeyBinding openGui, musicToggle, musicNext, musicPrev;

	private KeybindManager() {
	}

	public static void register() {
		openGui = reg("key.nevavis.open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT);
		musicToggle = reg("key.nevavis.music_toggle", GLFW.GLFW_KEY_UNKNOWN);
		musicNext = reg("key.nevavis.music_next", GLFW.GLFW_KEY_UNKNOWN);
		musicPrev = reg("key.nevavis.music_prev", GLFW.GLFW_KEY_UNKNOWN);
	}

	private static KeyBinding reg(String id, int key) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(id, InputUtil.Type.KEYSYM, key, CATEGORY));
	}

	public static KeyBinding openGui() {
		return openGui;
	}

	public static void tick(MinecraftClient client) {
		while (openGui.wasPressed()) {
			if (client.currentScreen == null && client.player != null) {
				client.setScreen(new NevaVisScreen());
			}
		}
		while (musicToggle.wasPressed()) {
			MusicPlayer.get().togglePause();
		}
		while (musicNext.wasPressed()) {
			MusicPlayer.get().next();
		}
		while (musicPrev.wasPressed()) {
			MusicPlayer.get().previous();
		}
	}
}
