package io.github.haykam821.dragononslaught.game.phase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.haykam821.dragononslaught.DragonOnslaught;
import io.github.haykam821.dragononslaught.game.DragonOnslaughtConfig;
import io.github.haykam821.dragononslaught.game.event.DragonDestroyBlockEvent;
import io.github.haykam821.dragononslaught.game.map.DragonOnslaughtMap;
import io.github.haykam821.dragononslaught.game.player.PlayerEntry;
import io.github.haykam821.dragononslaught.game.player.TeamEntry;
import io.github.haykam821.dragononslaught.game.spawner.DragonSpawner;
import io.github.haykam821.dragononslaught.game.win.FreeForAllWinManager;
import io.github.haykam821.dragononslaught.game.win.TeamWinManager;
import io.github.haykam821.dragononslaught.game.win.WinManager;
import io.github.haykam821.dragononslaught.game.win.WinResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamChat;
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.api.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class DragonOnslaughtActivePhase implements GameActivityEvents.Enable, GameActivityEvents.Tick, GamePlayerEvents.Accept, GamePlayerEvents.Remove, PlayerDeathEvent, DragonDestroyBlockEvent {
	private final GameSpace gameSpace;
	private final RandomSource random;
	private final ServerLevel level;
	private final DragonOnslaughtMap map;
	private final DragonOnslaughtConfig config;

	private final List<PlayerEntry> players;
	private final boolean singleplayer;

	private final DragonSpawner dragonSpawner;
	private final WinManager winManager;

	private int ticksUntilClose = -1;

	public DragonOnslaughtActivePhase(GameSpace gameSpace, ServerLevel level, DragonOnslaughtMap map, DragonOnslaughtConfig config, Optional<TeamSelectionLobby> maybeTeamSelection, Optional<TeamManager> maybeTeamManager) {
		this.gameSpace = gameSpace;
		this.level = level;
		this.random = level.getRandom();
		this.map = map;
		this.config = config;

		PlayerSet participants = this.gameSpace.getPlayers().participants();

		List<ServerPlayer> shuffledPlayers = participants.stream().collect(Collectors.toCollection(ArrayList::new));
		Util.shuffle(shuffledPlayers, this.random);

		this.players = new ArrayList<>(shuffledPlayers.size());

		Map<UUID, GameTeamKey> playersToTeams = new HashMap<>();
		Map<GameTeamKey, TeamEntry> keysToTeams = new HashMap<>();

		maybeTeamSelection.ifPresent(teamSelection -> teamSelection.allocate(participants, (key, player) -> {
            playersToTeams.put(player.getUUID(), key);
            maybeTeamManager.get().addPlayerTo(player, key);
        }));

		for (ServerPlayer player : shuffledPlayers) {
			GameTeamKey key = playersToTeams.get(player.getUUID());

			TeamEntry team = key == null ? null : keysToTeams.computeIfAbsent(key, k -> new TeamEntry(this.config.teams().orElseThrow().byKey(k)));

			this.players.add(new PlayerEntry(this, player, team));
		}

		this.singleplayer = players.size() == 1;

		this.dragonSpawner = new DragonSpawner(this.config.dragonSpawner(), this);
		this.winManager = maybeTeamSelection.isPresent() ? new TeamWinManager(this) : new FreeForAllWinManager(this);
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.BLOCK_DROPS);
		activity.deny(GameRuleType.BREAK_BLOCKS);
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(DragonOnslaught.DRAGON_COLLISION_DAMAGE);
		activity.deny(DragonOnslaught.DRAGON_PEER_LAUNCHING);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.ICE_MELT);
		activity.allow(DragonOnslaught.INCREASED_VERTICAL_DRAGON_MOVEMENT);
		activity.deny(GameRuleType.MODIFY_ARMOR);
		activity.deny(GameRuleType.MODIFY_INVENTORY);
		activity.deny(GameRuleType.PICKUP_ITEMS);
		activity.deny(GameRuleType.PLACE_BLOCKS);
		activity.deny(GameRuleType.PORTALS);
		activity.deny(GameRuleType.PVP);
		activity.deny(GameRuleType.SATURATED_REGENERATION);
		activity.deny(GameRuleType.THROW_ITEMS);
	}

	public static void open(GameSpace gameSpace, ServerLevel level, DragonOnslaughtMap map, DragonOnslaughtConfig config, Optional<TeamSelectionLobby> teamSelection) {
		gameSpace.setActivity(activity -> {
			Optional<TeamManager> maybeTeamManager = config.teams().map(teams -> {
				TeamManager teamManager = TeamManager.addTo(activity);

				TeamChat.addTo(activity, teamManager);
				teamManager.addTeams(teams);

				return teamManager;
			});

			DragonOnslaughtActivePhase phase = new DragonOnslaughtActivePhase(gameSpace, level, map, config, teamSelection, maybeTeamManager);
			DragonOnslaughtActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, phase);
			activity.listen(GameActivityEvents.TICK, phase);
			activity.listen(GamePlayerEvents.ACCEPT, phase);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::acceptSpectators);
			activity.listen(GamePlayerEvents.REMOVE, phase);
			activity.listen(PlayerDeathEvent.EVENT, phase);
			activity.listen(DragonDestroyBlockEvent.EVENT, phase);
		});
	}

	// Listeners

	@Override
	public void onEnable() {
		List<TemplateRegion> spawns = this.map.getSpawns(random);
		int index = 0;

		for (PlayerEntry player : this.players) {
			TemplateRegion spawn = spawns.get(index % spawns.size());
			player.spawn(this.map, spawn);

			index += 1;
		}
	}

	@Override
	public void onTick() {
		// Decrease ticks until game end to zero
		if (this.isGameEnding()) {
			if (this.ticksUntilClose == 0) {
				this.gameSpace.close(GameCloseReason.FINISHED);
			}

			this.ticksUntilClose -= 1;
			return;
		}

		for (PlayerEntry entry : this.players) {
			entry.tick(this.level);
		}

		this.dragonSpawner.tick();

		WinResult win = this.winManager.checkWin();

		if (win != null) {
			this.gameSpace.getPlayers().sendMessage(win.message());
			this.ticksUntilClose = this.config.ticksUntilClose().sample(this.random);
		}
	}

	@Override
	public JoinAcceptorResult onAcceptPlayers(JoinAcceptor acceptor) {
		return this.map.acceptSpectatorJoins(acceptor, level);
	}

	@Override
	public void onRemovePlayer(ServerPlayer player) {
		this.eliminate(this.getPlayerEntry(player));
	}

	@Override
	public EventResult onDeath(ServerPlayer player, DamageSource source) {
		this.eliminate(this.getPlayerEntry(player));
		return EventResult.DENY;
	}

	@Override
	public void onDragonDestroyBlock(ServerLevel world, BlockPos pos) {
		if (this.shouldDestroyFluid(pos)) {
			world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}
	}

	// Utilities

	/**
	 * Attempts to eliminate a player.
	 */
	public void eliminate(PlayerEntry player) {
		if (this.isGameEnding()) return;

		if (player == null) return;
		if (player.getAlivePlayer() == null) return;

		// Send elimination message
		Component message = player.getEliminationMessage();
		this.gameSpace.getPlayers().sendMessage(message);

		// Perform removal operations
		player.reset(GameType.SPECTATOR);
		player.clearAlivePlayer();

	}

	public RandomSource getRandom() {
		return this.random;
	}

	public ServerLevel getLevel() {
		return this.level;
	}

	public DragonOnslaughtMap getMap() {
		return this.map;
	}

	public DragonOnslaughtConfig getConfig() {
		return this.config;
	}

	public List<PlayerEntry> getPlayers() {
		return this.players;
	}

	public boolean isSingleplayer() {
		return this.singleplayer;
	}

	private boolean isGameEnding() {
		return this.ticksUntilClose >= 0;
	}

	private boolean shouldDestroyFluid(BlockPos pos) {
		Optional<Integer> seaLevel = this.config.map().seaLevel();

        return seaLevel.map(integer -> pos.getY() > integer).orElse(true);

    }

	private PlayerEntry getPlayerEntry(ServerPlayer player) {
		if (player != null) {
			for (PlayerEntry entry : this.players) {
				if (player == entry.getAlivePlayer()) {
					return entry;
				}
			}
		}

		return null;
	}
}
