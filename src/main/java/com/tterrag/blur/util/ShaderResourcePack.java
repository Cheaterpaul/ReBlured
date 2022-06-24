package com.tterrag.blur.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

public class ShaderResourcePack implements PackResources, ResourceManagerReloadListener {

	private final ModFile blurModFile = FMLLoader.getLoadingModList().getModFileById("blur").getFile();
	
	protected boolean validPath(ResourceLocation location) {
		return location.getNamespace().equals("minecraft") && location.getPath().startsWith("shaders/");
	}
	
	private final Map<ResourceLocation, String> loadedData = new HashMap<>();

	@Override
	public InputStream getResource(PackType type, ResourceLocation location) throws IOException {
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
	public boolean hasResource(PackType type, ResourceLocation location) {
		return type == PackType.CLIENT_RESOURCES && validPath(location) && Files.exists(blurModFile.findResource(location.getPath()));
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		return type == PackType.CLIENT_RESOURCES ? ImmutableSet.of("minecraft") : Collections.emptySet();
	}

	@SuppressWarnings({ "unchecked", "null" })
    @Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> arg0) throws IOException {
	    if ("pack".equals(arg0.getMetadataSectionName())) {
	        return (T) new PackMetadataSection(new TextComponent("Blur's default shaders"), 3);
	    }
	    return null;
    }

	@Override
	public void onResourceManagerReload(ResourceManager pResourceManager) {
		loadedData.clear();
	}

	@Override
	public String getName() {
		return "Blur dummy resource pack";
	}

	@Override
    public Collection<ResourceLocation> getResources(PackType p_225637_1_, String p_225637_2_, String p_225637_3_, int p_225637_4_, Predicate<String> p_225637_5_) {
        return Collections.emptyList();
    }
	
	@Override
	public InputStream getRootResource(String arg0) throws IOException {
        return Files.newInputStream(blurModFile.findResource("assets/blur/" + arg0));
	}
	
	@Override
	public void close() {}
}
