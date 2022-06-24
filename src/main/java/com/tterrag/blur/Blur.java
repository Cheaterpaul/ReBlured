package com.tterrag.blur;

import com.mojang.blaze3d.shaders.Uniform;
import com.tterrag.blur.util.ShaderResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.List;

import static com.tterrag.blur.Blur.MODID;

@Mod(MODID)
public class Blur {
    
    public static final String MODID = "blur";
    public static final String MOD_NAME = "Blur";
    public static final String VERSION = "@VERSION@";
    
    public static Blur instance;
        
    private Field _listShaders;
    private long start;
    
    @Nonnull
    private final ShaderResourcePack dummyPack = new ShaderResourcePack();
    
    @SuppressWarnings("deprecation")
	public Blur() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (incoming, isNetwork) -> true));
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
        	FMLJavaModLoadingContext.get().getModEventBus().register(this);
        	MinecraftForge.EVENT_BUS.addListener(this::onGuiChange);
            MinecraftForge.EVENT_BUS.addListener(this::onRenderTick);
            MinecraftForge.EVENT_BUS.addListener(this::onResourceListener);
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BlurConfig.clientSpec);
        });
        instance = this;
    }

    @SubscribeEvent
    public void registerPackRepository(AddPackFindersEvent event){
        event.addRepositorySource((infoConsumer, infoFactory) -> infoConsumer.accept(new Pack("blur", true, () -> dummyPack, new TextComponent(dummyPack.getName()), new TextComponent("Default shaders for Blur"), PackCompatibility.COMPATIBLE, Pack.Position.BOTTOM, true, PackSource.DEFAULT, false)));
    }

    public void onResourceListener(AddReloadListenerEvent event){
        event.addListener(dummyPack);
    }
    
    public void onGuiChange(ScreenOpenEvent event) throws SecurityException {
        if (_listShaders == null) {
            _listShaders = ObfuscationReflectionHelper.findField(PostChain.class, "f_110009_");
        }
        if (Minecraft.getInstance().level != null) {
            GameRenderer er = Minecraft.getInstance().gameRenderer;
            boolean excluded = event.getScreen() == null || BlurConfig.CLIENT.guiExclusions.get().contains(event.getScreen().getClass().getName());
            if (er.currentEffect() == null && !excluded) {
                er.loadEffect(new ResourceLocation("shaders/post/fade_in_blur.json"));
                updateUniform("Radius", BlurConfig.CLIENT.radius.get());
                start = System.currentTimeMillis();
            } else if (er.currentEffect() != null && excluded) {
                er.shutdownEffect();
            }
        }
    }
    
    private float getProgress() {
        return Math.min((System.currentTimeMillis() - start) / (float) BlurConfig.CLIENT.fadeTime.get(), 1);
    }
    
    private float prevProgress = -1;
    
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().screen != null && Minecraft.getInstance().gameRenderer.currentEffect() != null) {
            float progress = getProgress();
            if (progress != prevProgress) {
                prevProgress = progress;
                updateUniform("Progress", progress);
            }
        }
    }
    
    public void updateUniform(String name, float value) {
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
    
    public static int getBackgroundColor(boolean second) {
        int color = second ? BlurConfig.colorSecond : BlurConfig.colorFirst;
        int a = color >>> 24;
        int r = (color >> 16) & 0xFF;
        int b = (color >> 8) & 0xFF;
        int g = color & 0xFF;
        float prog = instance.getProgress();
        a *= prog;
        r *= prog;
        g *= prog;
        b *= prog;
        return a << 24 | r << 16 | b << 8 | g;
    }
}
