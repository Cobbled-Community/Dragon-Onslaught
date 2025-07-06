package io.github.haykam821.dragononslaught.game.win;

import java.util.List;

import io.github.haykam821.dragononslaught.game.phase.DragonOnslaughtActivePhase;
import io.github.haykam821.dragononslaught.game.player.PlayerEntry;

public abstract class WinManager {
	protected final DragonOnslaughtActivePhase phase;

	public WinManager(DragonOnslaughtActivePhase phase) {
		this.phase = phase;
	}

	public abstract WinResult checkRemainingWin(List<PlayerEntry> alivePlayers);

	public final WinResult checkWin() {
		List<PlayerEntry> alivePlayers = this.phase.getPlayers()
			.stream()
			.filter(player -> player.getAlivePlayer() != null)
			.toList();

		if (alivePlayers.isEmpty()) return WinResult.NONE;
		if (this.phase.isSingleplayer()) return null;

		return this.checkRemainingWin(alivePlayers);
	}
}
