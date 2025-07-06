package io.github.haykam821.dragononslaught.game.player;

import io.github.haykam821.dragononslaught.game.map.DragonOnslaughtMap;
import io.github.haykam821.dragononslaught.game.phase.DragonOnslaughtActivePhase;
import io.github.haykam821.dragononslaught.game.spawner.target.DragonTarget;
import io.github.haykam821.dragononslaught.game.spawner.target.PlayerDragonTarget;
import io.github.haykam821.dragononslaught.item.DragonOnslaughtItems;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.util.InventoryUtil;

public class PlayerEntry {
	private final DragonOnslaughtActivePhase phase;

	private ServerPlayerEntity alivePlayer;
	private final TeamEntry team;

	private int ticksUntilNextSparkler;

	public PlayerEntry(DragonOnslaughtActivePhase phase, ServerPlayerEntity player, TeamEntry team) {
		this.phase = phase;

		this.alivePlayer = player;
		this.team = team;

		this.resetTicksUntilNextSparkler();
	}

	/**
	 * {@return the player entity, or {@code null} if the player has been eliminated}
	 */
	public ServerPlayerEntity getAlivePlayer() {
		return this.alivePlayer;
	}

	public void clearAlivePlayer() {
		this.alivePlayer = null;
	}

	public TeamEntry getTeam() {
		return this.team;
	}

	public void spawn(DragonOnslaughtMap map, TemplateRegion spawn) {
		this.reset(GameMode.ADVENTURE);

		this.alivePlayer.giveItemStack(new ItemStack(DragonOnslaughtItems.LEAP_FEATHER));
		map.teleportToRegion(this.alivePlayer, spawn);
	}

	public void tick(ServerWorld world) {
		if (this.alivePlayer != null) {
			if (this.phase.getMap().isOutOfBounds(this.alivePlayer)) {
				this.phase.eliminate(this);
				return;
			} else if (this.alivePlayer.isTouchingWater()) {
				this.alivePlayer.damage(world, world.getDamageSources().drown(), 3);
				if (this.alivePlayer == null) return;
			}

			this.ticksUntilNextSparkler -= 1;

			if (this.ticksUntilNextSparkler <= 0 && this.alivePlayer.getInventory().count(DragonOnslaughtItems.SPARKLER) < 2) {
				this.resetTicksUntilNextSparkler();
				this.alivePlayer.getInventory().insertStack(8, new ItemStack(DragonOnslaughtItems.SPARKLER));
			}
		}
	}

	public Text getWinMessage() {
		return Text.translatable("text.dragononslaught.win", this.alivePlayer.getDisplayName()).formatted(Formatting.GOLD);
	}

	public Text getEliminationMessage() {
		return Text.translatable("text.dragononslaught.eliminated", this.alivePlayer.getDisplayName()).formatted(Formatting.RED);
	}

	public DragonTarget getDragonTarget() {
		return this.alivePlayer == null ? null : new PlayerDragonTarget(this.alivePlayer, this.phase.getConfig());
	}

	public void reset(GameMode gameMode) {
		this.alivePlayer.changeGameMode(gameMode);
		InventoryUtil.clear(this.alivePlayer);
	}

	private void resetTicksUntilNextSparkler() {
		this.ticksUntilNextSparkler = phase.getConfig().sparklerInterval().get(this.phase.getWorld().getRandom());
	}

	@Override
	public String toString() {
		return "PlayerEntry{alivePlayer=" + this.alivePlayer + "}";
	}
}
