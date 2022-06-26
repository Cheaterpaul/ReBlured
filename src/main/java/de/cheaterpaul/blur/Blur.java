package de.cheaterpaul.blur;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

import static de.cheaterpaul.blur.Blur.MODID;

@Mod(MODID)
public class Blur {
    
    public static final String MODID = "blur";
    public static final String MOD_NAME = "Blur";
    public static final String VERSION = "@VERSION@";
    
    @SuppressWarnings("deprecation")
	public Blur() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (incoming, isNetwork) -> true));
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            BlurConfig.registerConfig();
            BlurClient.register();
        });
    }




    

    

    

    



    

    

}
