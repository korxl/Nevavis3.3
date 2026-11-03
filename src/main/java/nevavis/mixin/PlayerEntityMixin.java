package nevavis.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import nevavis.presence.Badge;
import nevavis.presence.PresenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Значок N над ником (в нейм-теге и чате). */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
	private void nevavis$badge(CallbackInfoReturnable<Text> cir) {
		if (!Badge.enabled()) {
			return;
		}
		PlayerEntity self = (PlayerEntity) (Object) this;
		if (PresenceManager.isUser(self.getNameForScoreboard())) {
			cir.setReturnValue(Badge.prefix(cir.getReturnValue()));
		}
	}
}
