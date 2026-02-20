package io.github.haykam821.dragononslaught.game.win;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public record WinResult(Component message) {
	private static final Component NONE_MESSAGE = Component.translatable("text.dragononslaught.no_winners").withStyle(ChatFormatting.GOLD);
	static final WinResult NONE = new WinResult(NONE_MESSAGE);
}
