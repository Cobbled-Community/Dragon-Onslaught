package io.github.haykam821.dragononslaught.game.spawner.target;

import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;

public interface DragonTarget {
	Vec3 getPos();

	int getWeight();

	static void addTo(WeightedList.Builder<DragonTarget> builder, DragonTarget target) {
		if (target != null) {
			builder.add(target, target.getWeight());
		}
	}
}
