package io.github.haykam821.dragononslaught.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import io.github.haykam821.dragononslaught.DragonOnslaught;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public final class DragonOnslaughtEntityTypes {
	private static final Identifier SPARKLER_ID = DragonOnslaught.identifier("sparkler");
	private static final ResourceKey<EntityType<?>> SPARKLER_KEY = ResourceKey.create(Registries.ENTITY_TYPE, SPARKLER_ID);

	public static final EntityType<SparklerEntity> SPARKLER = EntityType.Builder.of(SparklerEntity::new, MobCategory.MISC)
		.noLootTable()
		.sized(0.25f, 0.25f)
		.eyeHeight(0.2125f)
		.clientTrackingRange(6)
		.updateInterval(20)
		.build(SPARKLER_KEY);

	private DragonOnslaughtEntityTypes() {
	}

	public static void register() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, SPARKLER_ID, SPARKLER);
		PolymerEntityUtils.registerType(SPARKLER);
	}
}
