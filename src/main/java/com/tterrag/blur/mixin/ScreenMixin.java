package com.tterrag.blur.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tterrag.blur.Blur;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

//    @Inject(method = "Lnet/minecraft/client/gui/screens/Screen;renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;fillGradient(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V"))
//    private void modifyBackground(PoseStack pPoseStack, int pVOffset, CallbackInfo ci) {
//        Blur.getBackgroundColor(true);
//    }

    @ModifyConstant(method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", constant = @Constant(intValue = -804253680))
    private int secondValues(int constant){
        return Blur.getBackgroundColor(true);
    }

    @ModifyConstant(method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V", constant = @Constant(intValue = -1072689136))
    private int firstValues(int constant){
        return Blur.getBackgroundColor(false);
    }
}