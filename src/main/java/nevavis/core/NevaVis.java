package nevavis.core;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/** Глобальные константы клиента. */
public final class NevaVis {
	public static final String ID = "nevavis";
	public static final String NAME = "NevaVis";
	public static final String MC_VERSION = "1.21.4";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	private NevaVis() {
	}

	/** Версия берётся из fabric.mod.json (подставляется Gradle). */
	public static String VERSION() {
		return FabricLoader.getInstance().getModContainer(ID)
				.map(c -> c.getMetadata().getVersion().getFriendlyString())
				.orElse("unknown");
	}

	/** config/nevavis/ */
	public static Path configDir() {
		return FabricLoader.getInstance().getConfigDir().resolve(ID);
	}

	/** config/nevavis/music/ */
	public static Path musicDir() {
		return configDir().resolve("music");
	}
}
