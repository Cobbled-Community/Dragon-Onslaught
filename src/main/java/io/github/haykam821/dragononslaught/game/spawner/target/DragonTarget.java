package io.github.haykam821.dragononslaught.game.spawner.target;

import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.Vec3d;

public interface DragonTarget {
	public Vec3d getPos();

	public int getWeight();

	public static void addTo(DataPool.Builder<DragonTarget> builder, DragonTarget target) {
		if (target != null) {
			builder.add(target, target.getWeight());
		}
	}
}
