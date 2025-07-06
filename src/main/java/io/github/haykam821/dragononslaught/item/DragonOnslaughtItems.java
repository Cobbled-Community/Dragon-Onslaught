package io.github.haykam821.dragononslaught.item;

import java.util.List;
import java.util.function.Function;

import io.github.haykam821.dragononslaught.DragonOnslaught;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworkExplosionComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public final class DragonOnslaughtItems {
	public static final Item LEAP_FEATHER = register("leap_feather", LeapFeatherItem::new, new Item.Settings()
		.component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
		.useCooldown(8)
		.maxCount(1));

	public static final Item SPARKLER = register("sparkler", SparklerItem::new, new Item.Settings()
		.component(DataComponentTypes.FIREWORKS, createSparklerFireworks())
		.useCooldown(0.5f));

	private DragonOnslaughtItems() {
		return;
	}

	public static void initialize() {
		return;
	}

	private static Item register(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
		Identifier id = DragonOnslaught.identifier(path);
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

		Item item = factory.apply(settings.registryKey(key));
		Registry.register(Registries.ITEM, id, item);

		return item;
	}

	private static FireworksComponent createSparklerFireworks() {
		IntList colors = IntList.of(DyeColor.YELLOW.getFireworkColor());
		FireworkExplosionComponent explosion = new FireworkExplosionComponent(FireworkExplosionComponent.Type.BURST, colors, IntList.of(), false, false);

		return new FireworksComponent(0, List.of(explosion));
	}
}
