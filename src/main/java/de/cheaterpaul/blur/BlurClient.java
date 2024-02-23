package de.cheaterpaul.blur;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.shaders.Uniform;
import de.cheaterpaul.blur.mixin.PostChainMixin;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Blur.MODID)
public class BlurClient {

    private static final KeyMapping toggleKey = new KeyMapping("keys.reblured.toggle", KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F10, "keys.reblured.category");
    /** uses `minecraft` as namespace because optifine breaks the shader loading */
    private static final ResourceLocation fade_in_blur = new ResourceLocation("shaders/post/fade_in_blur.json");
    private static float prevProgress = -1;
    private static long start;
    private static String lastShader;

    public static void register(IEventBus modBus) {
        modBus.addListener(BlurClient::registerKeyBinding);
    }

    private static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(toggleKey);
    }

    public static boolean clicked;
    @SubscribeEvent
    public static void handleInput(InputEvent.Key event) {
        if (!clicked && toggleKey.matches(event.getKey(), event.getScanCode())) {
            clicked = true;
            if (Minecraft.getInstance().level != null) {
                Screen screen = Minecraft.getInstance().screen;
                if (screen != null) {
                    String clazz = screen.getClass().getName();
                    List<? extends String> s = BlurConfig.CLIENT.guiExclusions.get();
                    boolean added;
                    if (s.contains(clazz)) {
                        s.remove(clazz);
                        added = false;
                    } else {
                        ((List<String>) s).add(clazz);
                        added = true;
                    }
                    BlurConfig.CLIENT.guiExclusions.set(s);
                    onConfigChange(added);
                }
            }
        } else {
            clicked = false;
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().screen != null && Minecraft.getInstance().gameRenderer.currentEffect() != null) {
            float progress = getProgress();
            if (progress != prevProgress) {
                prevProgress = progress;
                updateUniform("Progress", progress);
            }
        }
    }

    public static void updateUniform(String name, float value) {
        PostChain sg = Minecraft.getInstance().gameRenderer.currentEffect();
        if(sg != null) {
            try {
                List<PostPass> shaders = ((PostChainMixin)sg).getPasses();
                for (PostPass s : shaders) {
                    Uniform su = s.getEffect().getUniform(name);
                    if (su != null) {
                        su.set(value);
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SubscribeEvent
    public static void onGuiChange(ScreenEvent.Init.Post event) throws SecurityException {
        updateShader(BlurConfig.guiExlusions.contains(event.getScreen().getClass().getName()));
    }

    @SubscribeEvent
    public static void onGuiChange(ScreenEvent.Closing event) throws SecurityException {
//        updateShader(true);
    }

    public static void onConfigChange(boolean excluded) {
        updateShader(excluded);
    }

    public static void updateShader(boolean excluded) {
        if (Minecraft.getInstance().level != null) {
            GameRenderer er = Minecraft.getInstance().gameRenderer;
            PostChain postChain = er.currentEffect();
            if (!excluded) {
                boolean setShader = false;
                if (postChain == null) {
                    setShader = true;
                } else if (!fade_in_blur.toString().equals(postChain.getName())) {
                    setShader = true;
                    lastShader = postChain.getName();
                }
                if (setShader) {
                    er.loadEffect(fade_in_blur);
                    updateUniform("Radius", BlurConfig.CLIENT.radius.get());
                    if (start == -1) {
                        start = System.currentTimeMillis();
                    } else {
                        updateUniform("Progress", getProgress());
                    }
                }
            } else if (postChain != null && fade_in_blur.toString().equals(postChain.getName())) {
                if (lastShader != null) {
                    er.loadEffect(new ResourceLocation(lastShader));
                    lastShader = null;
                } else {
                    er.shutdownEffect();
                }
                start = -1;
                prevProgress = -1;
            }
        } else {
            start = -1;
            prevProgress = -1;
        }
    }

    private static float getProgress() {
        return Math.min((System.currentTimeMillis() - start) / (float) BlurConfig.CLIENT.fadeTime.get(), 1);
    }

    public static int getBackgroundColor(boolean second) {
        int color = second ? BlurConfig.colorSecond : BlurConfig.colorFirst;
        int a = color >> 24;
        int r = (color >> 16) & 0xFF;
        int b = (color >> 8) & 0xFF;
        int g = color & 0xFF;
        float prog = getProgress();
        a *= prog;
        r *= prog;
        g *= prog;
        b *= prog;
        return a << 24 | r << 16 | b << 8 | g;
    }
}
