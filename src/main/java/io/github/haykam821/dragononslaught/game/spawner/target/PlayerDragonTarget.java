package io.github.haykam821.dragononslaught.game.spawner.target;

import java.util.Optional;

import io.github.haykam821.dragononslaught.game.DragonOnslaughtConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public record PlayerDragonTarget(ServerPlayerEntity player, DragonOnslaughtConfig config) implements DragonTarget {
	private static final int RANGE = 24;
	private static final int MAXIMUM_UNDER_SEA_LEVEL = 16;

	@Override
	public Vec3d getPos() {
		Vec3d pos = getRandomPosInside(player.getBoundingBox().expand(RANGE), player.getRandom());
		int y = player.getEntityWorld().getTopY(Heightmap.Type.WORLD_SURFACE, (int) pos.getX(), (int) pos.getZ());

		Optional<Integer> seaLevel = config.map().seaLevel();

		if (seaLevel.isPresent()) {
			y = Math.max(y, seaLevel.get() - MAXIMUM_UNDER_SEA_LEVEL);
		}

		return pos.withAxis(Direction.Axis.Y, y);
	}

	@Override
	public int getWeight() {
		return 1;
	}

	private static Vec3d getRandomPosInside(Box box, Random random) {
		double x = MathHelper.lerp(random.nextDouble(), box.minX, box.maxX);
		double y = MathHelper.lerp(random.nextDouble(), box.minY, box.maxY);
		double z = MathHelper.lerp(random.nextDouble(), box.minZ, box.maxZ);

		return new Vec3d(x, y, z);
	}
}
