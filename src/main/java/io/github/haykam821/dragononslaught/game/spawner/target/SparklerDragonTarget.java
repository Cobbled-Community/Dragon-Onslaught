package io.github.haykam821.dragononslaught.game.spawner.target;

import io.github.haykam821.dragononslaught.entity.SparklerEntity;
import net.minecraft.util.math.Vec3d;

public record SparklerDragonTarget(SparklerEntity sparkler) implements DragonTarget {
	@Override
	public Vec3d getPos() {
		return this.sparkler.getEyePos();
	}

	@Override
	public int getWeight() {
		return 10;
	}
}
