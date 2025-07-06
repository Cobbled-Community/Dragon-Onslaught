package io.github.haykam821.dragononslaught.game.spawner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.haykam821.dragononslaught.entity.SparklerEntity;
import io.github.haykam821.dragononslaught.game.phase.DragonOnslaughtActivePhase;
import io.github.haykam821.dragononslaught.game.player.PlayerEntry;
import io.github.haykam821.dragononslaught.game.spawner.target.DragonTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.ChargingPlayerPhase;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DragonSpawner {
	private static final double MAX_YAW_DEVIATION = 15;

	private final DragonSpawnerConfig config;
	private final DragonOnslaughtActivePhase phase;

	private final List<EnderDragonEntity> dragons = new ArrayList<>();

	private int ticksUntilDragonSpawn;

	public DragonSpawner(DragonSpawnerConfig config, DragonOnslaughtActivePhase phase) {
		this.config = config;
		this.phase = phase;

		this.ticksUntilDragonSpawn = config.initialDelay().get(this.phase.getRandom());
	}

	public void tick() {
		this.ticksUntilDragonSpawn -= 1;

		if (this.ticksUntilDragonSpawn <= 0 && this.dragons.size() < this.config.maximum()) {
			this.ticksUntilDragonSpawn = this.config.interval().get(this.phase.getRandom());

			Vec3d targetPos = this.getTargetPos();

			if (targetPos != null) {
				ServerWorld world = this.phase.getWorld();
				Vec3d spawnPos = this.phase.getMap().getDragonSpawnPos(world);

				EnderDragonEntity dragon = new EnderDragonEntity(EntityType.ENDER_DRAGON, world);
				dragon.setPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

				chargeTowards(dragon, targetPos);

				double dx = targetPos.x - spawnPos.x;
				double dy = targetPos.y - spawnPos.y;
				double dz = targetPos.z - spawnPos.z;

				double deviation = dragon.getRandom().nextTriangular(90, MAX_YAW_DEVIATION);

				float yaw = (float) ((MathHelper.atan2(dx, dz) * MathHelper.DEGREES_PER_RADIAN) + deviation);
				float pitch = (float) (-MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * MathHelper.DEGREES_PER_RADIAN);

				dragon.setYaw(yaw);
				dragon.lastYaw = yaw;

				dragon.setHeadYaw(yaw);
				dragon.lastHeadYaw = yaw;

				dragon.setBodyYaw(yaw);
				dragon.lastBodyYaw = yaw;

				dragon.setPitch(pitch);
				dragon.lastPitch = pitch;

				world.spawnEntity(dragon);
				this.dragons.add(dragon);
			}
		}

		Iterator<EnderDragonEntity> iterator = this.dragons.iterator();

		while (iterator.hasNext()) {
			EnderDragonEntity dragon = iterator.next();

			if (dragon.isRemoved()) {
				iterator.remove();
			} else {
				PhaseManager phaseManager = dragon.getPhaseManager();
				Phase phase = phaseManager.getCurrent();

				if (!(phase instanceof ChargingPlayerPhase) && !(phase instanceof HoldingPatternPhase)) {
					Vec3d targetPos = this.getTargetPos();
					chargeTowards(dragon, targetPos);
				}
			}
		}
	}

	public Vec3d getTargetPos() {
		Pool.Builder<DragonTarget> builder = Pool.builder();

		for (PlayerEntry player : this.phase.getPlayers()) {
			DragonTarget.addTo(builder, player.getDragonTarget());
		}

		for (SparklerEntity sparkler : this.phase.getWorld().getEntitiesByType(TypeFilter.instanceOf(SparklerEntity.class), Entity::isAlive)) {
			DragonTarget.addTo(builder, sparkler.getDragonTarget());
		}

		return builder.build()
			.getOrEmpty(this.phase.getRandom())
			.map(DragonTarget::getPos)
			.orElse(null);
	}

	private static void chargeTowards(EnderDragonEntity dragon, Vec3d pos) {
		if (pos != null) {
			PhaseManager phaseManager = dragon.getPhaseManager();

			phaseManager.setPhase(PhaseType.CHARGING_PLAYER);
			phaseManager.create(PhaseType.CHARGING_PLAYER).setPathTarget(pos);
		}
	}
}
