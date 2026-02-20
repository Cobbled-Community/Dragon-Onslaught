package io.github.haykam821.dragononslaught.game.player;

import io.github.haykam821.dragononslaught.game.map.DragonOnslaughtMap;
import io.github.haykam821.dragononslaught.game.phase.DragonOnslaughtActivePhase;
import io.github.haykam821.dragononslaught.game.spawner.target.DragonTarget;
import io.github.haykam821.dragononslaught.game.spawner.target.PlayerDragonTarget;
import io.github.haykam821.dragononslaught.item.DragonOnslaughtItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.util.InventoryUtil;

public class PlayerEntry {
	private final DragonOnslaughtActivePhase phase;

	private ServerPlayer alivePlayer;
	private final TeamEntry team;

	private int ticksUntilNextSparkler;

	public PlayerEntry(DragonOnslaughtActivePhase phase, ServerPlayer player, TeamEntry team) {
		this.phase = phase;

		this.alivePlayer = player;
		this.team = team;

		this.resetTicksUntilNextSparkler();
	}

	/**
	 * {@return the player entity, or {@code null} if the player has been eliminated}
	 */
	public ServerPlayer getAlivePlayer() {
		return this.alivePlayer;
	}

	public void clearAlivePlayer() {
		this.alivePlayer = null;
	}

	public TeamEntry getTeam() {
		return this.team;
	}

	public void spawn(DragonOnslaughtMap map, TemplateRegion spawn) {
		this.reset(GameType.ADVENTURE);

		this.alivePlayer.addItem(new ItemStack(DragonOnslaughtItems.LEAP_FEATHER));
		map.teleportToRegion(this.alivePlayer, spawn);
	}

	public void tick(ServerLevel world) {
		if (this.alivePlayer != null) {
			if (this.phase.getMap().isOutOfBounds(this.alivePlayer)) {
				this.phase.eliminate(this);
				return;
			} else if (this.alivePlayer.isInWater()) {
				this.alivePlayer.hurtServer(world, world.damageSources().drown(), 3);
				if (this.alivePlayer == null) return;
			}

			this.ticksUntilNextSparkler -= 1;

			if (this.ticksUntilNextSparkler <= 0 && this.alivePlayer.getInventory().countItem(DragonOnslaughtItems.SPARKLER) < 2) {
				this.resetTicksUntilNextSparkler();
				this.alivePlayer.getInventory().add(8, new ItemStack(DragonOnslaughtItems.SPARKLER));
			}
		}
	}

	public Component getWinMessage() {
		return Component.translatable("text.dragononslaught.win", this.alivePlayer.getDisplayName()).withStyle(ChatFormatting.GOLD);
	}

	public Component getEliminationMessage() {
		return Component.translatable("text.dragononslaught.eliminated", this.alivePlayer.getDisplayName()).withStyle(ChatFormatting.RED);
	}

	public DragonTarget getDragonTarget() {
		return this.alivePlayer == null ? null : new PlayerDragonTarget(this.alivePlayer, this.phase.getConfig());
	}

	public void reset(GameType gameMode) {
		this.alivePlayer.setGameMode(gameMode);
		InventoryUtil.clear(this.alivePlayer);
	}

	private void resetTicksUntilNextSparkler() {
		this.ticksUntilNextSparkler = phase.getConfig().sparklerInterval().sample(this.phase.getWorld().getRandom());
	}

	@Override
	public String toString() {
		return "PlayerEntry{alivePlayer=" + this.alivePlayer + "}";
	}
}
