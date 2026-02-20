package io.github.haykam821.dragononslaught.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import io.github.haykam821.dragononslaught.game.spawner.target.DragonTarget;
import io.github.haykam821.dragononslaught.game.spawner.target.SparklerDragonTarget;
import io.github.haykam821.dragononslaught.item.DragonOnslaughtItems;
import net.minecraft.SharedConstants;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class SparklerEntity extends ItemEntity implements PolymerEntity {
	private static final int DURATION = SharedConstants.TICKS_PER_SECOND * 5;
	private static final int EFFECT_INTERVAL = SharedConstants.TICKS_PER_SECOND / 2;

	public SparklerEntity(EntityType<? extends ItemEntity> type, World world) {
		super(type, world);

		this.setStack(new ItemStack(DragonOnslaughtItems.SPARKLER));
		this.setPickupDelayInfinite();
	}

	public DragonTarget getDragonTarget() {
		return new SparklerDragonTarget(this);
	}

	@Override
	public void tick() {
		super.tick();

		if (this.getEntityWorld() instanceof ServerWorld world) {
			if (this.age % EFFECT_INTERVAL == 0) {
				FireworksComponent fireworks = this.getStack().get(DataComponentTypes.FIREWORKS);
				spawnFireworks(world, this.getEntityPos(), fireworks);

				this.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 3, 1);
			}

			if (this.age > DURATION) {
				this.discard();
			}
		}
	}

	@Override
	public EntityType<?> getPolymerEntityType(PacketContext context) {
		return EntityType.ITEM;
	}

	private static void spawnFireworks(ServerWorld world, Vec3d pos, FireworksComponent fireworks) {
		ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
		stack.set(DataComponentTypes.FIREWORKS, fireworks);

		FireworkRocketEntity rocket = new FireworkRocketEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);

		// Immediately explode the firework rocket on the client
		world.spawnEntity(rocket);
		world.sendEntityStatus(rocket, EntityStatuses.EXPLODE_FIREWORK_CLIENT);

		rocket.discard();
	}
}
