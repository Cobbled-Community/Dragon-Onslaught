package io.github.haykam821.dragononslaught.game.spawner;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.SharedConstants;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;

public record DragonSpawnerConfig(
	IntProvider initialDelay,
	IntProvider interval,
	int maximum
) {
	public static final DragonSpawnerConfig DEFAULT = new DragonSpawnerConfig(
		ConstantIntProvider.create(SharedConstants.TICKS_PER_SECOND * 5),
		ConstantIntProvider.create(SharedConstants.TICKS_PER_SECOND * 10),
		32
	);

	public static final Codec<DragonSpawnerConfig> UNIFORM_CODEC = IntProvider.NON_NEGATIVE_CODEC.xmap(provider -> {
		return new DragonSpawnerConfig(provider, provider, DEFAULT.maximum());
	}, config -> {
		return config.interval();
	});

	public static final Codec<DragonSpawnerConfig> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("initial_delay", DEFAULT.initialDelay()).forGetter(DragonSpawnerConfig::initialDelay),
			IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("interval", DEFAULT.interval()).forGetter(DragonSpawnerConfig::interval),
			Codecs.POSITIVE_INT.optionalFieldOf("maximum", DEFAULT.maximum()).forGetter(DragonSpawnerConfig::maximum)
		).apply(instance, DragonSpawnerConfig::new);
	});

	public static final Codec<DragonSpawnerConfig> CODEC = Codec.either(UNIFORM_CODEC, RECORD_CODEC).xmap(Either::unwrap, config -> {
		return config.initialDelay().equals(config.interval()) && config.maximum() == DEFAULT.maximum() ? Either.left(config) : Either.right(config);
	});
}
