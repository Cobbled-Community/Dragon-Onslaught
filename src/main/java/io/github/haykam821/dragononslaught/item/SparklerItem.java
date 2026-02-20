package io.github.haykam821.dragononslaught.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.haykam821.dragononslaught.entity.DragonOnslaughtEntityTypes;
import io.github.haykam821.dragononslaught.entity.SparklerEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import xyz.nucleoid.packettweaker.PacketContext;

public class SparklerItem extends Item implements PolymerItem {
	private static final double STRENGTH = 0.9;

	public SparklerItem(Item.Properties settings) {
		super(settings);
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		if (!world.isClientSide()) {
			ItemStack stack = player.getItemInHand(hand);

			SparklerEntity entity = new SparklerEntity(DragonOnslaughtEntityTypes.SPARKLER, world);

			entity.setPos(player.getX(), player.getEyeY() - 0.3, player.getZ());
			entity.setThrower(player);

			Vec3 velocity = player.getLookAngle().scale(STRENGTH).add(0, 0.1, 0);
			entity.setDeltaMovement(velocity);

			world.addFreshEntity(entity);

			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
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
