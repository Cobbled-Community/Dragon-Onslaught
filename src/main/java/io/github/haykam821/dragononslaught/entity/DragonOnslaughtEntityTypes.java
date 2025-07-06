package io.github.haykam821.dragononslaught.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import io.github.haykam821.dragononslaught.DragonOnslaught;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class DragonOnslaughtEntityTypes {
	private static final Identifier SPARKLER_ID = DragonOnslaught.identifier("sparkler");
	private static final RegistryKey<EntityType<?>> SPARKLER_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, SPARKLER_ID);

	public static final EntityType<SparklerEntity> SPARKLER = EntityType.Builder.create(SparklerEntity::new, SpawnGroup.MISC)
		.dropsNothing()
		.dimensions(0.25f, 0.25f)
		.eyeHeight(0.2125f)
		.maxTrackingRange(6)
		.trackingTickInterval(20)
		.build(SPARKLER_KEY);

	private DragonOnslaughtEntityTypes() {
		return;
	}

	public static void register() {
		Registry.register(Registries.ENTITY_TYPE, SPARKLER_ID, SPARKLER);
		PolymerEntityUtils.registerType(SPARKLER);
	}
}
