package juuxel.sodiumbiomeblendingfix;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public record Config(List<String> blockClassWhitelist, List<Identifier> blockIdWhitelist) {
    public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.listOf().fieldOf("block_class_whitelist").forGetter(Config::blockClassWhitelist),
            Identifier.CODEC.listOf().fieldOf("block_id_whitelist").forGetter(Config::blockIdWhitelist)
        ).apply(instance, Config::new)
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(SodiumBiomeBlendingFix.ID);
    private static final String[] DEFAULT_CLASS_NAMES = {
        "net.minecraft.class_2397", // LeavesBlock
        "net.minecraft.class_2372", // GrassBlock
        "net.minecraft.class_2523", // SugarCaneBlock
        "net.minecraft.class_2261", // PlantBlock
        "net.minecraft.class_2541", // VineBlock
        "net.minecraft.class_2258", // BubbleColumnBlock
    };
    private static final String[] DEFAULT_BLOCK_IDS = {
        "minecraft:water",
        "minecraft:water_cauldron",
    };
    private static final String FILE_NAME = SodiumBiomeBlendingFix.ID + ".json";
    public static final Supplier<Config> INSTANCE = Suppliers.memoize(Config::load);

    private static Config createDefault() {
        return new Config(List.of(DEFAULT_CLASS_NAMES), Arrays.stream(DEFAULT_BLOCK_IDS).map(Identifier::new).toList());
    }

    private static Config load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        Config result;
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

        try {
            if (Files.notExists(path)) {
                result = createDefault();
                var json = CODEC.encodeStart(JsonOps.INSTANCE, result).result().orElseThrow();
                Files.writeString(path, gson.toJson(json));
            } else {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    var json = gson.fromJson(reader, JsonObject.class);
                    result = CODEC.decode(JsonOps.INSTANCE, json).result().orElseThrow().getFirst();
                }
            }
        } catch (Exception e) {
            result = createDefault();
            LOGGER.error("Could not load Sodium Biome Blending Fix config", e);
        }

        return result;
    }
}
