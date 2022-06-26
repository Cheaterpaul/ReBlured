package de.cheaterpaul.blur.mixin;

import de.cheaterpaul.blur.BlurClient;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Screen.class)
public class ScreenMixin {

    @ModifyConstant(method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", constant = @Constant(intValue = -804253680))
    private int secondValues(int constant){
        return BlurClient.getBackgroundColor(true);
    }

    @ModifyConstant(method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", constant = @Constant(intValue = -1072689136))
    private int firstValues(int constant){
        return BlurClient.getBackgroundColor(false);
    }
}
