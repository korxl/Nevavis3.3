package nevavis.hud;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/** Считает клики мыши (CPS) опросом состояния кнопок каждый кадр. */
public final class ClickTracker {
	private static final Deque<Long> LEFT = new ArrayDeque<>();
	private static final Deque<Long> RIGHT = new ArrayDeque<>();
	private static boolean lastLeft, lastRight;

	private ClickTracker() {
	}

	public static void poll(MinecraftClient mc) {
		long handle = mc.getWindow().getHandle();
		boolean l = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean r = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		long now = System.currentTimeMillis();
		if (l && !lastLeft) {
			LEFT.addLast(now);
		}
		if (r && !lastRight) {
			RIGHT.addLast(now);
		}
		lastLeft = l;
		lastRight = r;
		trim(LEFT, now);
		trim(RIGHT, now);
	}

	private static void trim(Deque<Long> d, long now) {
		while (!d.isEmpty() && now - d.peekFirst() > 1000L) {
			d.pollFirst();
		}
	}

	public static int left() {
		return LEFT.size();
	}

	public static int right() {
		return RIGHT.size();
	}
}
