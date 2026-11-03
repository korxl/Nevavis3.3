package nevavis.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import nevavis.config.ConfigManager;
import nevavis.presence.PresenceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Плащ NevaVis: у тебя и у других игроков с модом (по списку онлайна). */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class CapeMixin {
	private static final Identifier CAPE = Identifier.of("nevavis", "textures/cape.png");

	@Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
	private void nevavis$cape(CallbackInfoReturnable<SkinTextures> cir) {
		if (!ConfigManager.get().data().showCape) {
			return;
		}
		AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
		String name = self.getNameForScoreboard();
		if (!name.equals(PresenceManager.selfName()) && !PresenceManager.isUser(name)) {
			return;
		}
		SkinTextures o = cir.getReturnValue();
		cir.setReturnValue(new SkinTextures(o.texture(), o.textureUrl(), CAPE,
				o.elytraTexture() != null ? o.elytraTexture() : CAPE, o.model(), o.secure()));
	}
}
