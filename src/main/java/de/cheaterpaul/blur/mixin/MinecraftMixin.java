package de.cheaterpaul.blur.mixin;

import de.cheaterpaul.blur.BlurClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;removed()V"))
    private void onScreenClose(@Nullable Screen p_91153_, CallbackInfo ci ) {
        if (p_91153_ == null) {
            BlurClient.updateShader(true);
        }
    }
}
