package nevavis.mixin;

import net.minecraft.client.render.item.HeldItemRenderer;
import nevavis.modules.visuals.SwingModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Меняет прогресс взмаха руки (единственный float-аргумент applySwingOffset). */
@Mixin(HeldItemRenderer.class)
public abstract class SwingMixin {
	@ModifyVariable(method = "applySwingOffset", at = @At("HEAD"), argsOnly = true)
	private float nevavis$swing(float progress) {
		return SwingModule.remap(progress);
	}
}
