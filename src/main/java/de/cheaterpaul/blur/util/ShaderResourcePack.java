package de.cheaterpaul.blur.util;

import com.google.common.collect.ImmutableSet;
import de.cheaterpaul.blur.Blur;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;

public class ShaderResourcePack implements PackResources, ResourceManagerReloadListener {

	private final ModFile blurModFile = FMLLoader.getLoadingModList().getModFileById(Blur.MODID).getFile();
	
	protected boolean validPath(ResourceLocation location) {
		return location.getNamespace().equals("minecraft") && location.getPath().startsWith("shaders/");
	}
	
	private final Map<ResourceLocation, String> loadedData = new HashMap<>();

	@Override
	public @NotNull InputStream getResource(@NotNull PackType type, @NotNull ResourceLocation location) throws IOException {
        if (type == PackType.CLIENT_RESOURCES && validPath(location)) {
            try {
                return Files.newInputStream(blurModFile.findResource(location.getPath()));
            } catch (IOException e) {
                throw new RuntimeException("Could not read " + location.getPath());
            }
        }
        throw new FileNotFoundException(location.toString());
	}

	@Override
	public boolean hasResource(@NotNull PackType type, @NotNull ResourceLocation location) {
		return type == PackType.CLIENT_RESOURCES && validPath(location) && Files.exists(blurModFile.findResource(location.getPath()));
	}

	@Override
	public @NotNull Set<String> getNamespaces(@NotNull PackType type) {
		return type == PackType.CLIENT_RESOURCES ? ImmutableSet.of("minecraft") : Collections.emptySet();
	}

	@SuppressWarnings({ "unchecked", "null" })
    @Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> arg0) throws IOException {
	    if ("pack".equals(arg0.getMetadataSectionName())) {
	        return (T) new PackMetadataSection(Component.literal("Blur's default shaders"), 3);
	    }
	    return null;
    }

	@Override
	public void onResourceManagerReload(@NotNull ResourceManager pResourceManager) {
		loadedData.clear();
	}

	@Override
	public @NotNull String getName() {
		return "Blur dummy resource pack";
	}

	@Override
	public Collection<ResourceLocation> getResources(PackType p_215339_, String p_215340_, String p_215341_, Predicate<ResourceLocation> p_215342_) {
		return Collections.emptyList();
	}

	@Override
	public InputStream getRootResource(@NotNull String arg0) throws IOException {
        return Files.newInputStream(blurModFile.findResource("assets/reblured/" + arg0));
	}
	
	@Override
	public void close() {}
}
