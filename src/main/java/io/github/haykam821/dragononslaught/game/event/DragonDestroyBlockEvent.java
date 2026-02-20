package io.github.haykam821.dragononslaught.game.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface DragonDestroyBlockEvent {
	StimulusEvent<DragonDestroyBlockEvent> EVENT = StimulusEvent.create(DragonDestroyBlockEvent.class, context -> (world, pos) -> {
        try {
            for (DragonDestroyBlockEvent listener : context.getListeners()) {
                listener.onDragonDestroyBlock(world, pos);
            }
        } catch (Throwable throwable) {
            context.handleException(throwable);
        }
    });

	void onDragonDestroyBlock(ServerLevel world, BlockPos pos);
}