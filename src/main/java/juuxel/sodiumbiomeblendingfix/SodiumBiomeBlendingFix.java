package juuxel.sodiumbiomeblendingfix;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class SodiumBiomeBlendingFix implements ModInitializer {
    private static final Object2BooleanMap<Block> whitelistCache = new Object2BooleanOpenHashMap<>();

    @Override
    public void onInitialize() {
        Config.INSTANCE.get(); // load the config
    }

    public static boolean isWhitelisted(Block block) {
        return whitelistCache.computeIfAbsent(block, b -> {
            MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

            // 1. Try ID lookup
            Identifier blockId = Registry.BLOCK.getId(block);

            for (Identifier id : Config.INSTANCE.get().blockIdWhitelist()) {
                if (blockId.equals(id)) {
                    return true;
                }
            }

            // 2. Try class lookup
            for (String className : Config.INSTANCE.get().blockClassWhitelist()) {
                className = resolver.mapClassName("intermediary", className);

                try {
                    Class<?> clazz = Class.forName(className);

                    if (clazz.isInstance(block)) {
                        return true;
                    }
                } catch (Exception e) {
                    // ignored
                }
            }

            return false;
        });
    }
}
