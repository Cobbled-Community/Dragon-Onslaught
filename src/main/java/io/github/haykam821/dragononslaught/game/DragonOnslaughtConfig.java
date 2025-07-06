package io.github.haykam821.dragononslaught.game;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.dragononslaught.game.map.DragonOnslaughtMapConfig;
import io.github.haykam821.dragononslaught.game.spawner.DragonSpawnerConfig;
import net.minecraft.SharedConstants;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamList;

public record DragonOnslaughtConfig(
	DragonOnslaughtMapConfig map,
	WaitingLobbyConfig playerConfig,
	Optional<GameTeamList> teams,
	DragonSpawnerConfig dragonSpawner,
	IntProvider sparklerInterval,
	IntProvider ticksUntilClose
) {
	public static final MapCodec<DragonOnslaughtConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
			DragonOnslaughtMapConfig.CODEC.fieldOf("map").forGetter(DragonOnslaughtConfig::map),
			WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(DragonOnslaughtConfig::playerConfig),
			GameTeamList.CODEC.optionalFieldOf("teams").forGetter(DragonOnslaughtConfig::teams),
			DragonSpawnerConfig.CODEC.optionalFieldOf("dragon_spawner", DragonSpawnerConfig.DEFAULT).forGetter(DragonOnslaughtConfig::dragonSpawner),
			IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("sparker_interval", ConstantIntProvider.create(SharedConstants.TICKS_PER_SECOND * 25)).forGetter(DragonOnslaughtConfig::sparklerInterval),
			IntProvider.NON_NEGATIVE_CODEC.optionalFieldOf("ticks_until_close", ConstantIntProvider.create(SharedConstants.TICKS_PER_SECOND * 5)).forGetter(DragonOnslaughtConfig::ticksUntilClose)
		).apply(instance, DragonOnslaughtConfig::new);
	});
}
