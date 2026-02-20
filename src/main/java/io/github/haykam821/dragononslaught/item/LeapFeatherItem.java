package io.github.haykam821.dragononslaught.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import xyz.nucleoid.packettweaker.PacketContext;

public class LeapFeatherItem extends Item implements PolymerItem {
	private static final double STRENGTH = 1;

	public LeapFeatherItem(Item.Properties settings) {
		super(settings);
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer) {
			Vec3 velocity = player.getLookAngle().scale(STRENGTH);

			Packet<?> packet = new ClientboundSetEntityMotionPacket(player.getId(), velocity);
			serverPlayer.connection.send(packet);
			serverPlayer.connection.send(new ClientboundSoundEntityPacket(Holder.direct(SoundEvents.ENDER_DRAGON_SHOOT), SoundSource.PLAYERS, serverPlayer, 1, 1.2f, world.getRandom().nextLong()));
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
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
