package io.github.haykam821.dragononslaught.game.player;

import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;

public class TeamEntry {
	private final GameTeam team;

	public TeamEntry(GameTeam team) {
		this.team = team;
	}

	public BlockState getBlock() {
		return ColoredBlocks.concrete(this.team.config().blockDyeColor()).getDefaultState();
	}

	public Text getWinMessage() {
		return Text.translatable("text.dragononslaught.win.team", this.team.config().name()).formatted(Formatting.GOLD);
	}

	@Override
	public String toString() {
		return "TeamEntry{team=" + this.team + "}";
	}
}
