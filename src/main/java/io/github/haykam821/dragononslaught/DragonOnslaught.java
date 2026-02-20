package io.github.haykam821.dragononslaught;

import io.github.haykam821.dragononslaught.entity.DragonOnslaughtEntityTypes;
import io.github.haykam821.dragononslaught.game.DragonOnslaughtConfig;
import io.github.haykam821.dragononslaught.game.phase.DragonOnslaughtWaitingPhase;
import io.github.haykam821.dragononslaught.item.DragonOnslaughtItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.GameTypes;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;

public class DragonOnslaught implements ModInitializer {
	private static final String MOD_ID = "dragononslaught";

	private static final Identifier GAME_TYPE_ID = DragonOnslaught.identifier("dragon_onslaught");
	public static final GameType<DragonOnslaughtConfig> GAME_TYPE = GameTypes.register(GAME_TYPE_ID, DragonOnslaughtConfig.CODEC, DragonOnslaughtWaitingPhase::open);

	public static final GameRuleType DRAGON_COLLISION_DAMAGE = GameRuleType.create();
	public static final GameRuleType DRAGON_PEER_LAUNCHING = GameRuleType.create();
	public static final GameRuleType INCREASED_VERTICAL_DRAGON_MOVEMENT = GameRuleType.create();

	@Override
	public void onInitialize() {
		DragonOnslaughtEntityTypes.register();
		DragonOnslaughtItems.initialize();
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
