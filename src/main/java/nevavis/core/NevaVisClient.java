package nevavis.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import nevavis.config.ConfigManager;
import nevavis.hud.HudRenderer;
import nevavis.keybinds.KeybindManager;
import nevavis.modules.ModuleManager;
import nevavis.modules.Modules;
import nevavis.music.MusicLibrary;
import nevavis.themes.ThemeManager;

/** Точка входа клиента NevaVis. */
public final class NevaVisClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		NevaVis.LOGGER.info("Initializing {} v{} (Minecraft {})",
				NevaVis.NAME, NevaVis.VERSION(), NevaVis.MC_VERSION);

		ConfigManager.get().load();
		ThemeManager.init();
		Modules.registerAll();
		ModuleManager.init();
		KeybindManager.register();
		MusicLibrary.init();
		nevavis.profiles.ProfileManager.init();
		nevavis.hud.HudManager.init();
		nevavis.presence.PresenceManager.init();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			KeybindManager.tick(client);
			nevavis.hud.HudManager.tick(client);
			nevavis.particles.ParticleEngine.tick(client);
		});
		HudRenderCallback.EVENT.register((context, tickCounter) ->
				HudRenderer.render(context, tickCounter.getTickDelta(false)));
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			ConfigManager.get().save();
			NevaVis.LOGGER.info("{} shut down, config saved.", NevaVis.NAME);
		});
	}
}
