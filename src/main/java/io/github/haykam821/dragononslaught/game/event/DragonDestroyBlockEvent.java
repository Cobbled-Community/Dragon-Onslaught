package io.github.haykam821.dragononslaught.game.event;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.stimuli.event.StimulusEvent;

public interface DragonDestroyBlockEvent {
	public StimulusEvent<DragonDestroyBlockEvent> EVENT = StimulusEvent.create(DragonDestroyBlockEvent.class, context -> {
		return (world, pos) -> {
			try {
				for (DragonDestroyBlockEvent listener : context.getListeners()) {
					listener.onDragonDestroyBlock(world, pos);
				}
			} catch (Throwable throwable) {
				context.handleException(throwable);
			}
		};
	});

	public void onDragonDestroyBlock(ServerWorld world, BlockPos pos);
}