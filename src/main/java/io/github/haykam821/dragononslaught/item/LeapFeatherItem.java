package io.github.haykam821.dragononslaught.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class LeapFeatherItem extends Item implements PolymerItem {
	private static final double STRENGTH = 1;

	public LeapFeatherItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult use(World world, PlayerEntity player, Hand hand) {
		if (player instanceof ServerPlayerEntity serverPlayer) {
			Vec3d velocity = player.getRotationVector().multiply(STRENGTH);

			Packet<?> packet = new EntityVelocityUpdateS2CPacket(player.getId(), velocity);
			serverPlayer.networkHandler.sendPacket(packet);
			serverPlayer.networkHandler.sendPacket(new PlaySoundFromEntityS2CPacket(RegistryEntry.of(SoundEvents.ENTITY_ENDER_DRAGON_SHOOT), SoundCategory.PLAYERS, serverPlayer, 1, 1.2f, world.getRandom().nextLong()));
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	@Override
	public Item getPolymerItem(ItemStack stack, PacketContext context) {
		return Items.FEATHER;
	}

	@Override
	public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
		return null;
	}
}
