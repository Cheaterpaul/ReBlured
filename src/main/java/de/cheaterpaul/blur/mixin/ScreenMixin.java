package de.cheaterpaul.blur.mixin;

import de.cheaterpaul.blur.BlurClient;
import de.cheaterpaul.blur.BlurConfig;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Screen.class)
public class ScreenMixin {

    @ModifyConstant(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;)V", constant = @Constant(intValue = -804253680))
    private int secondValues(int constant){
        if (BlurConfig.guiExlusions.contains(this.getClass().getName())) return 0;
        return BlurClient.getBackgroundColor(true);
    }

    @ModifyConstant(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;)V", constant = @Constant(intValue = -1072689136))
    private int firstValues(int constant){
        if (BlurConfig.guiExlusions.contains(this.getClass().getName())) return 0;
        return BlurClient.getBackgroundColor(false);
    }
}
