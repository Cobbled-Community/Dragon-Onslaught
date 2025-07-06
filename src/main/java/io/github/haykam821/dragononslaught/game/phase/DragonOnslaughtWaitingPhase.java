package io.github.haykam821.dragononslaught.game.phase;

import java.util.Optional;

import io.github.haykam821.dragononslaught.game.DragonOnslaughtConfig;
import io.github.haykam821.dragononslaught.game.map.DragonOnslaughtMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
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
	private final ServerWorld world;
	private final DragonOnslaughtMap map;
	private final DragonOnslaughtConfig config;
	private final Optional<TeamSelectionLobby> teamSelection;

	public DragonOnslaughtWaitingPhase(GameSpace gameSpace, ServerWorld world, DragonOnslaughtMap map, DragonOnslaughtConfig config, Optional<TeamSelectionLobby> teamSelection) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
		this.teamSelection = teamSelection;
	}

	public static GameOpenProcedure open(GameOpenContext<DragonOnslaughtConfig> context) {
		MinecraftServer server = context.server();
		DragonOnslaughtConfig config = context.config();

		DragonOnslaughtMap map = DragonOnslaughtMap.create(server, config);

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			Optional<TeamSelectionLobby> teamSelection = config.teams().map(teams -> TeamSelectionLobby.addTo(activity, teams));
			DragonOnslaughtWaitingPhase phase = new DragonOnslaughtWaitingPhase(activity.getGameSpace(), world, map, config, teamSelection);

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
		DragonOnslaughtActivePhase.open(this.gameSpace, this.world, this.map, this.config, this.teamSelection);
		return GameResult.ok();
	}

	@Override
	public void onTick() {
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			if (this.map.isOutOfBounds(player) || player.isTouchingWater()) {
				this.map.teleportToWaitingSpawn(player);
			}
		}
	}

	@Override
	public JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return this.map.acceptWaitingSpawnJoins(acceptor, world);
	}

	@Override
	public EventResult onDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return EventResult.DENY;
	}

	@Override
	public EventResult onDeath(ServerPlayerEntity player, DamageSource source) {
		this.map.teleportToWaitingSpawn(player);
		return EventResult.DENY;
	}
}
