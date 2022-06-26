package de.cheaterpaul.blur;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(value = Dist.CLIENT, modid = Blur.MODID, bus = Bus.MOD)
public class BlurConfig {

	private static final ForgeConfigSpec clientSpec;
	public static final Client CLIENT;
    public static int colorFirst, colorSecond;
    public static Set<String> guiExlusions = new HashSet<>();

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static void registerConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BlurConfig.clientSpec);
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent configEvent) {
        updateColors();
    }

    private static void updateColors() {
        int colorFirst = Integer.parseUnsignedInt(CLIENT.colorFirstRaw.get(), 16);
        BlurConfig.colorFirst = convertColor(colorFirst);
        int colorSecond = Integer.parseUnsignedInt(CLIENT.colorSecondRaw.get(), 16);
        BlurConfig.colorSecond = convertColor(colorSecond);
        guiExlusions.clear();;
        guiExlusions.addAll(CLIENT.guiExclusions.get());
        Minecraft.getInstance().execute(() -> BlurClient.updateUniform("Radius", CLIENT.radius.get()));
    }

    private static int convertColor(int rgba) {
        return  (((rgba) & 0xFF) << 24) |
                (((rgba >> 24) & 0xFF) << 16) |
                (((rgba >> 16) & 0xFF) << 8)  |
                (((rgba >> 8) & 0xFF) << 0);
    }

    public static class Client {

        public final ForgeConfigSpec.ConfigValue<List<? extends String>> guiExclusions;
        public final ForgeConfigSpec.IntValue fadeTime;
        public final ForgeConfigSpec.IntValue radius;
        public final ForgeConfigSpec.ConfigValue<String> colorFirstRaw;
        public final ForgeConfigSpec.ConfigValue<String> colorSecondRaw;

        private Client(ForgeConfigSpec.Builder builder) {
            this.guiExclusions = builder.comment("A list of classes to be excluded from the blur shader.")
                    .defineList("guiExclusions", Lists.newArrayList(ChatScreen.class.getName(), InBedChatScreen.class.getName()), o -> {
                        try {
                            //noinspection ConstantConditions
                            return o instanceof String && Class.forName((String) o) != null;
                        } catch (ClassNotFoundException e) {
                            return false;
                        }
                    });

            this.fadeTime = builder.comment("The time it takes for the blur to fade in, in ms.").defineInRange("fadeTime", 200, 0, 10 * 1000);
            this.radius = builder.comment("The radius of the blur effect. This controls how \"strong\" the blur is.").defineInRange("radius", 12, 1, 100);
            this.colorFirstRaw = builder.comment("The top color of the background gradient. Given in RGBA hex.").define("gradientStartColor", "00000075", this::isHexValue);
            this.colorSecondRaw = builder.comment("The bottom color of the background gradient. Given in RGBA hex.").define("gradientEndColor", "00000075", this::isHexValue);
        }

        private boolean isHexValue(Object o) {
            if (o instanceof String) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Integer.parseUnsignedInt((String) o, 16);
                    return true;
                } catch (NumberFormatException ignored) {}
            }
            return false;
        }
    }
}
