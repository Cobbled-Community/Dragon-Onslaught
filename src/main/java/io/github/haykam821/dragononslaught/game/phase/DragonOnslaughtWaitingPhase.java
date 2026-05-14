package io.github.haykam821.dragononslaught.game.phase;

import java.util.Optional;

import io.github.haykam821.dragononslaught.game.DragonOnslaughtConfig;
import io.github.haykam821.dragononslaught.game.map.DragonOnslaughtMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class DragonOnslaughtWaitingPhase implements GameActivityEvents.RequestStart, GameActivityEvents.Tick, GamePlayerEvents.Accept, PlayerDamageEvent, PlayerDeathEvent {
	private final GameSpace gameSpace;
	private final ServerLevel level;
	private final DragonOnslaughtMap map;
	private final DragonOnslaughtConfig config;
	private final Optional<TeamSelectionLobby> teamSelection;

	public DragonOnslaughtWaitingPhase(GameSpace gameSpace, ServerLevel level, DragonOnslaughtMap map, DragonOnslaughtConfig config, Optional<TeamSelectionLobby> teamSelection) {
		this.gameSpace = gameSpace;
		this.level = level;
		this.map = map;
		this.config = config;
		this.teamSelection = teamSelection;
	}

	public static GameOpenProcedure open(GameOpenContext<DragonOnslaughtConfig> context) {
		MinecraftServer server = context.server();
		DragonOnslaughtConfig config = context.config();

		DragonOnslaughtMap map = DragonOnslaughtMap.create(server, config);

		RuntimeLevelConfig levelConfig = new RuntimeLevelConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithLevel(levelConfig, (activity, level) -> {
			Optional<TeamSelectionLobby> teamSelection = config.teams().map(teams -> TeamSelectionLobby.addTo(activity, teams));
			DragonOnslaughtWaitingPhase phase = new DragonOnslaughtWaitingPhase(activity.getGameSpace(), level, map, config, teamSelection);

			GameWaitingLobby.addTo(activity, config.playerConfig());
			DragonOnslaughtActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.REQUEST_START, phase);
			activity.listen(GameActivityEvents.TICK, phase);
			activity.listen(GamePlayerEvents.ACCEPT, phase);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
			activity.listen(PlayerDamageEvent.EVENT, phase);
			activity.listen(PlayerDeathEvent.EVENT, phase);
		});
	}

	// Listeners

	@Override
	public GameResult onRequestStart() {
		DragonOnslaughtActivePhase.open(this.gameSpace, this.level, this.map, this.config, this.teamSelection);
		return GameResult.ok();
	}

	@Override
	public void onTick() {
		for (ServerPlayer player : this.gameSpace.getPlayers()) {
			if (this.map.isOutOfBounds(player) || player.isInWater()) {
				this.map.teleportToWaitingSpawn(player);
			}
		}
	}

	@Override
	public JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return this.map.acceptWaitingSpawnJoins(acceptor, level);
	}

	@Override
	public EventResult onDamage(ServerPlayer player, DamageSource source, float amount) {
		return EventResult.DENY;
	}

	@Override
	public EventResult onDeath(ServerPlayer player, DamageSource source) {
		this.map.teleportToWaitingSpawn(player);
		return EventResult.DENY;
	}
}
