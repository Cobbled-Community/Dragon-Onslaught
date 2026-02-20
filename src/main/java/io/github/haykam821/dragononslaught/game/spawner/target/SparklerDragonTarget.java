package io.github.haykam821.dragononslaught.game.spawner.target;

import io.github.haykam821.dragononslaught.entity.SparklerEntity;
import net.minecraft.world.phys.Vec3;

public record SparklerDragonTarget(SparklerEntity sparkler) implements DragonTarget {
	@Override
	public Vec3 getPos() {
		return this.sparkler.getEyePosition();
	}

	@Override
	public int getWeight() {
		return 10;
	}
}
