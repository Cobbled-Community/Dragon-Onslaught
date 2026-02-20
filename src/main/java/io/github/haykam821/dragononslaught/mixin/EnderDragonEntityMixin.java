package io.github.haykam821.dragononslaught.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import io.github.haykam821.dragononslaught.DragonOnslaught;
import io.github.haykam821.dragononslaught.game.event.DragonDestroyBlockEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.EventInvokers;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin extends Entity {
	private EnderDragonEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@WrapOperation(method = "launchLivingEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/phase/Phase;isSittingOrHovering()Z"))
	private boolean preventLaunchDamage(Phase phase, Operation<Boolean> operation, ServerWorld world, List<Entity> entities, @Local LocalRef<LivingEntity> entity) {
		GameSpace gameSpace = GameSpaceManager.get().byWorld(world);

		if (gameSpace != null && gameSpace.getBehavior().testRule(DragonOnslaught.DRAGON_COLLISION_DAMAGE) == EventResult.DENY) {
			DamageSource source = this.getDamageSources().mobAttack((EnderDragonEntity) (Object) this);
			entity.get().damage(world, source, Float.MIN_VALUE);

			// Prevents entering if-statement block that checks !this.phaseManager.getCurrent().isSittingOrHovering()
			return true;
		}

		return operation.call(phase);
	}

	@Inject(method = "damageLivingEntities", at = @At("HEAD"), cancellable = true)
	private void preventCollisionDamage(CallbackInfo ci) {
		GameSpace gameSpace = GameSpaceManager.get().byWorld(this.getEntityWorld());

		if (gameSpace != null && gameSpace.getBehavior().testRule(DragonOnslaught.DRAGON_COLLISION_DAMAGE) == EventResult.DENY) {
			ci.cancel();
		}
	}

	@WrapWithCondition(method = "launchLivingEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
	private boolean preventPeerLaunching(Entity entity, double x, double y, double z) {
		GameSpace gameSpace = GameSpaceManager.get().byWorld(entity.getEntityWorld());

		if (gameSpace != null && gameSpace.getBehavior().testRule(DragonOnslaught.DRAGON_PEER_LAUNCHING) == EventResult.DENY) {
			return !(entity instanceof EnderDragonEntity);
		}

		return true;
	}

	@ModifyConstant(method = "tickMovement", constant = @Constant(doubleValue = 0.01))
	private double increaseVerticalMovement(double original) {
		GameSpace gameSpace = GameSpaceManager.get().byWorld(this.getEntityWorld());

		if (gameSpace != null && gameSpace.getBehavior().testRule(DragonOnslaught.INCREASED_VERTICAL_DRAGON_MOVEMENT) == EventResult.ALLOW) {
			return 0.1;
		}

		return original;
	}

	@WrapOperation(method = "destroyBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
	private boolean onDragonDestroyBlock(ServerWorld world, BlockPos pos, boolean moved, Operation<Boolean> operation) {
		boolean result = operation.call(world, pos, moved);

		try (EventInvokers invokers = Stimuli.select().forEntity(this)) {
			invokers.get(DragonDestroyBlockEvent.EVENT).onDragonDestroyBlock(world, pos);
		}

		return result;
	}
}
