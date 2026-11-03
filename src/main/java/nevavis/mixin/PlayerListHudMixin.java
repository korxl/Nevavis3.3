package nevavis.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import nevavis.presence.Badge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Значок N в списке игроков (Tab). */
@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
	@Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
	private void nevavis$badge(CallbackInfoReturnable<Text> cir) {
		if (!Badge.enabled()) {
			return;
		}
		Text original = cir.getReturnValue();
		if (original != null && Badge.textContainsUser(original.getString())) {
			cir.setReturnValue(Badge.prefix(original));
		}
	}
}
