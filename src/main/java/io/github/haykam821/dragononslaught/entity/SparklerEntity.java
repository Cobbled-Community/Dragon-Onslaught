package io.github.haykam821.dragononslaught.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import io.github.haykam821.dragononslaught.game.spawner.target.DragonTarget;
import io.github.haykam821.dragononslaught.game.spawner.target.SparklerDragonTarget;
import io.github.haykam821.dragononslaught.item.DragonOnslaughtItems;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import xyz.nucleoid.packettweaker.PacketContext;

public class SparklerEntity extends ItemEntity implements PolymerEntity {
	private static final int DURATION = SharedConstants.TICKS_PER_SECOND * 5;
	private static final int EFFECT_INTERVAL = SharedConstants.TICKS_PER_SECOND / 2;

	public SparklerEntity(EntityType<? extends ItemEntity> type, Level world) {
		super(type, world);

		this.setItem(new ItemStack(DragonOnslaughtItems.SPARKLER));
		this.setNeverPickUp();
	}

	public DragonTarget getDragonTarget() {
		return new SparklerDragonTarget(this);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.level() instanceof ServerLevel world) {
			if (this.tickCount % EFFECT_INTERVAL == 0) {
				Fireworks fireworks = this.getItem().get(DataComponents.FIREWORKS);
				spawnFireworks(world, this.position(), fireworks);

				this.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 3, 1);
			}

			if (this.tickCount > DURATION) {
				this.discard();
			}
		}
	}

	@Override
	public EntityType<?> getPolymerEntityType(PacketContext context) {
		return EntityType.ITEM;
	}

	private static void spawnFireworks(ServerLevel world, Vec3 pos, Fireworks fireworks) {
		ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
		stack.set(DataComponents.FIREWORKS, fireworks);

		FireworkRocketEntity rocket = new FireworkRocketEntity(world, pos.x(), pos.y(), pos.z(), stack);

		// Immediately explode the firework rocket on the client
		world.addFreshEntity(rocket);
		world.broadcastEntityEvent(rocket, EntityEvent.FIREWORKS_EXPLODE);

		rocket.discard();
	}
}
