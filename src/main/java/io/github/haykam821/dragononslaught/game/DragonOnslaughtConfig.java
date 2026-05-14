package io.github.haykam821.dragononslaught.game;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.dragononslaught.game.map.DragonOnslaughtMapConfig;
import io.github.haykam821.dragononslaught.game.spawner.DragonSpawnerConfig;
import net.minecraft.SharedConstants;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
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
	public static final MapCodec<DragonOnslaughtConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DragonOnslaughtMapConfig.CODEC.fieldOf("map").forGetter(DragonOnslaughtConfig::map),
        WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(DragonOnslaughtConfig::playerConfig),
        GameTeamList.CODEC.optionalFieldOf("teams").forGetter(DragonOnslaughtConfig::teams),
        DragonSpawnerConfig.CODEC.optionalFieldOf("dragon_spawner", DragonSpawnerConfig.DEFAULT).forGetter(DragonOnslaughtConfig::dragonSpawner),
        IntProviders.NON_NEGATIVE_CODEC.optionalFieldOf("sparker_interval", ConstantInt.of(SharedConstants.TICKS_PER_SECOND * 25)).forGetter(DragonOnslaughtConfig::sparklerInterval),
        IntProviders.NON_NEGATIVE_CODEC.optionalFieldOf("ticks_until_close", ConstantInt.of(SharedConstants.TICKS_PER_SECOND * 5)).forGetter(DragonOnslaughtConfig::ticksUntilClose)
    ).apply(instance, DragonOnslaughtConfig::new));
}
