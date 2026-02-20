package io.github.haykam821.dragononslaught.item;

import java.util.List;
import java.util.function.Function;

import io.github.haykam821.dragononslaught.DragonOnslaught;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.Identifier;

public final class DragonOnslaughtItems {
	public static final Item LEAP_FEATHER = register("leap_feather", LeapFeatherItem::new, new Item.Properties()
		.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
		.useCooldown(8)
		.stacksTo(1));

	public static final Item SPARKLER = register("sparkler", SparklerItem::new, new Item.Properties()
		.component(DataComponents.FIREWORKS, createSparklerFireworks())
		.useCooldown(0.5f));

	private DragonOnslaughtItems() {}

	public static void initialize() {}

	private static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties settings) {
		Identifier id = DragonOnslaught.identifier(path);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);

		Item item = factory.apply(settings.setId(key));
		Registry.register(BuiltInRegistries.ITEM, id, item);

		return item;
	}

	private static Fireworks createSparklerFireworks() {
		IntList colors = IntList.of(DyeColor.YELLOW.getFireworkColor());
		FireworkExplosion explosion = new FireworkExplosion(FireworkExplosion.Shape.BURST, colors, IntList.of(), false, false);

		return new Fireworks(0, List.of(explosion));
	}
}
