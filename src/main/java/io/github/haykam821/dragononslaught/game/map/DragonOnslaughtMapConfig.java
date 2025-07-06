package io.github.haykam821.dragononslaught.game.map;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

public record DragonOnslaughtMapConfig(
	Identifier id,
	Optional<Integer> seaLevel
) {
	public static final Codec<DragonOnslaughtMapConfig> SIMPLE_CODEC = Identifier.CODEC.xmap(id -> {
		return new DragonOnslaughtMapConfig(id, Optional.empty());
	}, config -> {
		return config.id();
	});

	public static final Codec<DragonOnslaughtMapConfig> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(DragonOnslaughtMapConfig::id),
			Codec.INT.optionalFieldOf("sea_level").forGetter(DragonOnslaughtMapConfig::seaLevel)
		).apply(instance, DragonOnslaughtMapConfig::new);
	});

	public static final Codec<DragonOnslaughtMapConfig> CODEC = Codec.either(SIMPLE_CODEC, RECORD_CODEC).xmap(Either::unwrap, config -> {
		return config.seaLevel().isEmpty() ? Either.left(config) : Either.right(config);
	});
}
