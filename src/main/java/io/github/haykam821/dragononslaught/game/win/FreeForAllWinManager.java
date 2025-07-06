package io.github.haykam821.dragononslaught.game.win;

import java.util.List;

import io.github.haykam821.dragononslaught.game.phase.DragonOnslaughtActivePhase;
import io.github.haykam821.dragononslaught.game.player.PlayerEntry;

public class FreeForAllWinManager extends WinManager {
	public FreeForAllWinManager(DragonOnslaughtActivePhase phase) {
		super(phase);
	}

	@Override
	public WinResult checkRemainingWin(List<PlayerEntry> alivePlayers) {
		if (alivePlayers.size() == 1) {
			PlayerEntry winner = alivePlayers.get(0);
			return new WinResult(winner.getWinMessage());
		}

		return null;
	}
}
