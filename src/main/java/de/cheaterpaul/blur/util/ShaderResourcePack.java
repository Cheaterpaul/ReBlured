package de.cheaterpaul.blur.util;

import com.google.common.collect.ImmutableSet;
import de.cheaterpaul.blur.Blur;
import net.minecraft.FileUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ShaderResourcePack extends AbstractPackResources implements ResourceManagerReloadListener {

	private final ModFile blurModFile = FMLLoader.getLoadingModList().getModFileById(Blur.MODID).getFile();
    private final Path root;

	public ShaderResourcePack(String name, boolean buildIn) {
		super(name, buildIn);
        this.root = blurModFile.getFilePath();
	}

	protected boolean validPath(ResourceLocation location) {
		return location.getNamespace().equals("minecraft") && location.getPath().startsWith("shaders/");
	}

	private final Map<ResourceLocation, String> loadedData = new HashMap<>();

	@Override
	public @Nullable IoSupplier<InputStream> getResource(@NotNull PackType type, @NotNull ResourceLocation location) {
        if (type == PackType.CLIENT_RESOURCES && validPath(location)) {
            return PathPackResources.getResource(location, this.root);
        } else {
            return null;
        }
	}

	@Nullable
	@Override
	public IoSupplier<InputStream> getRootResource(String @NotNull ... subPaths) {
        FileUtil.validatePath(subPaths);
        Path path = FileUtil.resolvePath(this.root, List.of(subPaths));
        return Files.exists(path) ? IoSupplier.create(path) : null;
	}

	@Override
	public void listResources(@NotNull PackType p_10289_, @NotNull String p_251379_, @NotNull String p_251932_, @NotNull ResourceOutput p_249347_) {

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
	public void close() {}
}
