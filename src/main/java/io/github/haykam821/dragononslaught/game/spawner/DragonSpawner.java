package io.github.haykam821.dragononslaught.game.spawner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.haykam821.dragononslaught.entity.SparklerEntity;
import io.github.haykam821.dragononslaught.game.phase.DragonOnslaughtActivePhase;
import io.github.haykam821.dragononslaught.game.player.PlayerEntry;
import io.github.haykam821.dragononslaught.game.spawner.target.DragonTarget;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class DragonSpawner {
	private static final double MAX_YAW_DEVIATION = 15;

	private final DragonSpawnerConfig config;
	private final DragonOnslaughtActivePhase phase;

	private final List<EnderDragon> dragons = new ArrayList<>();

	private int ticksUntilDragonSpawn;

	public DragonSpawner(DragonSpawnerConfig config, DragonOnslaughtActivePhase phase) {
		this.config = config;
		this.phase = phase;

		this.ticksUntilDragonSpawn = config.initialDelay().sample(this.phase.getRandom());
	}

	public void tick() {
		this.ticksUntilDragonSpawn -= 1;

		if (this.ticksUntilDragonSpawn <= 0 && this.dragons.size() < this.config.maximum()) {
			this.ticksUntilDragonSpawn = this.config.interval().sample(this.phase.getRandom());

			Vec3 targetPos = this.getTargetPos();

			if (targetPos != null) {
				ServerLevel world = this.phase.getWorld();
				Vec3 spawnPos = this.phase.getMap().getDragonSpawnPos(world);

				EnderDragon dragon = new EnderDragon(EntityType.ENDER_DRAGON, world);
				dragon.setPos(spawnPos.x(), spawnPos.y(), spawnPos.z());

				chargeTowards(dragon, targetPos);

				double dx = targetPos.x - spawnPos.x;
				double dy = targetPos.y - spawnPos.y;
				double dz = targetPos.z - spawnPos.z;

				double deviation = dragon.getRandom().triangle(90, MAX_YAW_DEVIATION);

				float yaw = (float) ((Mth.atan2(dx, dz) * Mth.RAD_TO_DEG) + deviation);
				float pitch = (float) (-Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG);

				dragon.setYRot(yaw);
				dragon.yRotO = yaw;

				dragon.setYHeadRot(yaw);
				dragon.yHeadRotO = yaw;

				dragon.setYBodyRot(yaw);
				dragon.yBodyRotO = yaw;

				dragon.setXRot(pitch);
				dragon.xRotO = pitch;

				world.addFreshEntity(dragon);
				this.dragons.add(dragon);
			}
		}

		Iterator<EnderDragon> iterator = this.dragons.iterator();

		while (iterator.hasNext()) {
			EnderDragon dragon = iterator.next();

			if (dragon.isRemoved()) {
				iterator.remove();
			} else {
				EnderDragonPhaseManager phaseManager = dragon.getPhaseManager();
				DragonPhaseInstance phase = phaseManager.getCurrentPhase();

				if (!(phase instanceof DragonChargePlayerPhase) && !(phase instanceof DragonHoldingPatternPhase)) {
					Vec3 targetPos = this.getTargetPos();
					chargeTowards(dragon, targetPos);
				}
			}
		}
	}

	public Vec3 getTargetPos() {
		WeightedList.Builder<DragonTarget> builder = WeightedList.builder();

		for (PlayerEntry player : this.phase.getPlayers()) {
			DragonTarget.addTo(builder, player.getDragonTarget());
		}

		for (SparklerEntity sparkler : this.phase.getWorld().getEntities(EntityTypeTest.forClass(SparklerEntity.class), Entity::isAlive)) {
			DragonTarget.addTo(builder, sparkler.getDragonTarget());
		}

		return builder.build()
			.getRandom(this.phase.getRandom())
			.map(DragonTarget::getPos)
			.orElse(null);
	}

	private static void chargeTowards(EnderDragon dragon, Vec3 pos) {
		if (pos != null) {
			EnderDragonPhaseManager phaseManager = dragon.getPhaseManager();

			phaseManager.setPhase(EnderDragonPhase.CHARGING_PLAYER);
			phaseManager.getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(pos);
		}
	}
}
