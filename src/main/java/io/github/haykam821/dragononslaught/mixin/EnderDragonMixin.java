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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.EventInvokers;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin extends Entity {
	private EnderDragonMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@WrapOperation(method = "knockBack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/DragonPhaseInstance;isSitting()Z"))
	private boolean preventLaunchDamage(DragonPhaseInstance phase, Operation<Boolean> operation, ServerLevel level, List<Entity> entities, @Local LocalRef<LivingEntity> entity) {
		GameSpace gameSpace = GameSpaceManager.get().byLevel(level);

		if (gameSpace != null && gameSpace.getBehavior().testRule(DragonOnslaught.DRAGON_COLLISION_DAMAGE) == EventResult.DENY) {
			DamageSource source = this.damageSources().mobAttack((EnderDragon) (Object) this);
			entity.get().hurtServer(level, source, Float.MIN_VALUE);

			// Prevents entering if-statement block that checks !this.phaseManager.getCurrent().isSittingOrHovering()
			return true;
		}

		return operation.call(phase);
	}

	@Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
	private void preventCollisionDamage(CallbackInfo ci) {
		GameSpace gameSpace = GameSpaceManager.get().byLevel(this.level());

		if (gameSpace != null && gameSpace.getBehavior().testRule(DragonOnslaught.DRAGON_COLLISION_DAMAGE) == EventResult.DENY) {
			ci.cancel();
		}
	}

	@WrapWithCondition(method = "knockBack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
	private boolean preventPeerLaunching(Entity entity, double x, double y, double z) {
		GameSpace gameSpace = GameSpaceManager.get().byLevel(entity.level());

		if (gameSpace != null && gameSpace.getBehavior().testRule(DragonOnslaught.DRAGON_PEER_LAUNCHING) == EventResult.DENY) {
			return !(entity instanceof EnderDragon);
		}

		return true;
	}

	@ModifyConstant(method = "aiStep", constant = @Constant(doubleValue = 0.01))
	private double increaseVerticalMovement(double original) {
		GameSpace gameSpace = GameSpaceManager.get().byLevel(this.level());

		if (gameSpace != null && gameSpace.getBehavior().testRule(DragonOnslaught.INCREASED_VERTICAL_DRAGON_MOVEMENT) == EventResult.ALLOW) {
			return 0.1;
		}

		return original;
	}

	@WrapOperation(method = "checkWalls", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
	private boolean onDragonDestroyBlock(ServerLevel level, BlockPos pos, boolean moved, Operation<Boolean> operation) {
		boolean result = operation.call(level, pos, moved);

		try (EventInvokers invokers = Stimuli.select().forEntity(this)) {
			invokers.get(DragonDestroyBlockEvent.EVENT).onDragonDestroyBlock(level, pos);
		}

		return result;
	}
}
