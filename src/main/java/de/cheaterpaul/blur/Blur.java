package de.cheaterpaul.blur;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import static de.cheaterpaul.blur.Blur.MODID;

@Mod(MODID)
public class Blur {
    
    public static final String MODID = "reblured";

	public Blur(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            BlurConfig.registerConfig();
            BlurClient.register(modBus);
        }
    }




    

    

    

    



    

    

}
