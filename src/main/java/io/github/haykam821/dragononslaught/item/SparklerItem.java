package io.github.haykam821.dragononslaught.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.haykam821.dragononslaught.entity.DragonOnslaughtEntityTypes;
import io.github.haykam821.dragononslaught.entity.SparklerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class SparklerItem extends Item implements PolymerItem {
	private static final double STRENGTH = 0.9;

	public SparklerItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
		if (!world.isClient()) {
			ItemStack stack = player.getStackInHand(hand);

			SparklerEntity entity = new SparklerEntity(DragonOnslaughtEntityTypes.SPARKLER, world);

			entity.setPosition(player.getX(), player.getEyeY() - 0.3, player.getZ());
			entity.setThrower(player);

			Vec3d velocity = player.getRotationVector().multiply(STRENGTH).add(0, 0.1, 0);
			entity.setVelocity(velocity);

			world.spawnEntity(entity);

			if (!player.getAbilities().creativeMode) {
				stack.decrement(1);
			}

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	@Override
	public Item getPolymerItem(ItemStack stack, PacketContext context) {
		return Items.EMERALD;
	}

	@Override
	public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
		return null;
	}
}
