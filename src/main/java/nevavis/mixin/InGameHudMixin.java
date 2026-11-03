package nevavis.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import nevavis.config.ConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Скрывает ванильный прицел, когда включён прицел NevaVis. */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void nevavis$hideCrosshair(CallbackInfo ci) {
		if (ConfigManager.get().data().crosshair.enabled) {
			ci.cancel();
		}
	}
}
