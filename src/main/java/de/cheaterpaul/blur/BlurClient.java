package de.cheaterpaul.blur;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.shaders.Uniform;
import de.cheaterpaul.blur.util.ShaderResourcePack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Blur.MODID)
public class BlurClient {

    private static float prevProgress = -1;
    private static Field _listShaders;
    private static long start;
    private static final ShaderResourcePack dummyPack = new ShaderResourcePack();
    private static final KeyMapping toggleKey = new KeyMapping("keys.blur.toggle", KeyConflictContext.GUI, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F10, "keys.blur.category");

    public static void register() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BlurClient::registerPackRepository);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BlurClient::clientSetup);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(toggleKey);
    }

    public static boolean clicked;
    @SubscribeEvent
    public static void handleInput(InputEvent.KeyInputEvent event) {
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
        if (_listShaders == null) return;
        PostChain sg = Minecraft.getInstance().gameRenderer.currentEffect();
        if(sg != null) {
            try {
                @SuppressWarnings("unchecked")
                List<PostPass> shaders = (List<PostPass>) _listShaders.get(sg);
                for (PostPass s : shaders) {
                    Uniform su = s.getEffect().getUniform(name);
                    if (su != null) {
                        su.set(value);
                    }
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SubscribeEvent
    public static void onGuiChange(ScreenOpenEvent event) throws SecurityException {
        updateShader(event.getScreen() == null || BlurConfig.guiExlusions.contains(event.getScreen().getClass().getName()));
    }

    public static void onConfigChange(boolean excluded) {
        updateShader(excluded);
    }

    public static void updateShader(boolean excluded) {
        if (_listShaders == null) {
            _listShaders = ObfuscationReflectionHelper.findField(PostChain.class, "f_110009_");
        }
        if (Minecraft.getInstance().level != null) {
            GameRenderer er = Minecraft.getInstance().gameRenderer;
            if (er.currentEffect() == null && !excluded) {
                er.loadEffect(new ResourceLocation("shaders/post/fade_in_blur.json"));
                updateUniform("Radius", BlurConfig.CLIENT.radius.get());
                start = System.currentTimeMillis();
            } else if (er.currentEffect() != null && excluded) {
                er.shutdownEffect();
            }
        }
    }

    @SubscribeEvent
    public static void onResourceListener(AddReloadListenerEvent event){
        event.addListener(dummyPack);
    }

    private static float getProgress() {
        return Math.min((System.currentTimeMillis() - start) / (float) BlurConfig.CLIENT.fadeTime.get(), 1);
    }

    public static void registerPackRepository(AddPackFindersEvent event){
        event.addRepositorySource((infoConsumer, infoFactory) -> infoConsumer.accept(new Pack("blur", true, () -> dummyPack, new TextComponent(dummyPack.getName()), new TextComponent("Default shaders for Blur"), PackCompatibility.COMPATIBLE, Pack.Position.BOTTOM, true, PackSource.DEFAULT, false)));
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
