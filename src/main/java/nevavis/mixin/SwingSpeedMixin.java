package nevavis.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import nevavis.modules.visuals.SwingModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Длительность взмаха (только для себя). */
@Mixin(LivingEntity.class)
public abstract class SwingSpeedMixin {
	@Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
	private void nevavis$duration(CallbackInfoReturnable<Integer> cir) {
		if ((Object) this == MinecraftClient.getInstance().player) {
			cir.setReturnValue(SwingModule.duration(cir.getReturnValue()));
		}
	}
}
