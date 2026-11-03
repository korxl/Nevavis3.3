package nevavis.mixin;

import net.minecraft.client.world.ClientWorld;
import nevavis.modules.visuals.SkyHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Custom Sky / Custom Clouds. Тип результата (int или Vec3d) обрабатывается в SkyHooks. */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
	@Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
	private void nevavis$sky(CallbackInfoReturnable<Object> cir) {
		Object r = SkyHooks.tint("custom_sky", cir.getReturnValue());
		if (r != null) {
			cir.setReturnValue(r);
		}
	}

	@Inject(method = "getCloudsColor", at = @At("RETURN"), cancellable = true)
	private void nevavis$clouds(CallbackInfoReturnable<Object> cir) {
		Object r = SkyHooks.tint("custom_clouds", cir.getReturnValue());
		if (r != null) {
			cir.setReturnValue(r);
		}
	}
}
