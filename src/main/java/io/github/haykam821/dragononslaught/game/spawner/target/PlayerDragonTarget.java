package io.github.haykam821.dragononslaught.game.spawner.target;

import java.util.Optional;

import io.github.haykam821.dragononslaught.game.DragonOnslaughtConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public record PlayerDragonTarget(ServerPlayer player, DragonOnslaughtConfig config) implements DragonTarget {
	private static final int RANGE = 24;
	private static final int MAXIMUM_UNDER_SEA_LEVEL = 16;

	@Override
	public Vec3 getPos() {
		Vec3 pos = getRandomPosInside(player.getBoundingBox().inflate(RANGE), player.getRandom());
		int y = player.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) pos.x(), (int) pos.z());

		Optional<Integer> seaLevel = config.map().seaLevel();

		if (seaLevel.isPresent()) {
			y = Math.max(y, seaLevel.get() - MAXIMUM_UNDER_SEA_LEVEL);
		}

		return pos.with(Direction.Axis.Y, y);
	}

	@Override
	public int getWeight() {
		return 1;
	}

	private static Vec3 getRandomPosInside(AABB box, RandomSource random) {
		double x = Mth.lerp(random.nextDouble(), box.minX, box.maxX);
		double y = Mth.lerp(random.nextDouble(), box.minY, box.maxY);
		double z = Mth.lerp(random.nextDouble(), box.minZ, box.maxZ);

		return new Vec3(x, y, z);
	}
}
