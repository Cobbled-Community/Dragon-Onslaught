package io.github.haykam821.dragononslaught.game.spawner;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.SharedConstants;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;

public record DragonSpawnerConfig(
        IntProvider initialDelay,
        IntProvider interval,
        int maximum
) {
	public static final DragonSpawnerConfig DEFAULT = new DragonSpawnerConfig(
		ConstantInt.of(SharedConstants.TICKS_PER_SECOND * 5),
		ConstantInt.of(SharedConstants.TICKS_PER_SECOND * 10),
		32
	);

	public static final Codec<DragonSpawnerConfig> UNIFORM_CODEC = IntProviders.NON_NEGATIVE_CODEC.xmap(provider -> new DragonSpawnerConfig(provider, provider, DEFAULT.maximum()), DragonSpawnerConfig::interval);

	public static final Codec<DragonSpawnerConfig> RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        IntProviders.NON_NEGATIVE_CODEC.optionalFieldOf("initial_delay", DEFAULT.initialDelay()).forGetter(DragonSpawnerConfig::initialDelay),
        IntProviders.NON_NEGATIVE_CODEC.optionalFieldOf("interval", DEFAULT.interval()).forGetter(DragonSpawnerConfig::interval),
        ExtraCodecs.POSITIVE_INT.optionalFieldOf("maximum", DEFAULT.maximum()).forGetter(DragonSpawnerConfig::maximum)
    ).apply(instance, DragonSpawnerConfig::new));

	public static final Codec<DragonSpawnerConfig> CODEC = Codec.either(UNIFORM_CODEC, RECORD_CODEC).xmap(Either::unwrap, config -> config.initialDelay().equals(config.interval()) && config.maximum() == DEFAULT.maximum() ? Either.left(config) : Either.right(config));
}
