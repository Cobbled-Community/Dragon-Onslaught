package io.github.haykam821.dragononslaught.game.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.haykam821.dragononslaught.game.DragonOnslaughtConfig;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

public class DragonOnslaughtMap {
	private static final String FACING_KEY = "Facing";
	private static final String PRIORITY_KEY = "Priority";

	private static final Comparator<TemplateRegion> PRIORITY_COMPARATOR = Comparator.comparingInt(region -> {
		NbtCompound data = region.getData();
		return data == null ? 0 : data.getInt(PRIORITY_KEY, 0);
	});

	private final MapTemplate template;

	public DragonOnslaughtMap(MapTemplate template) {
		this.template = template;
	}

	public boolean isOutOfBounds(ServerPlayerEntity player) {
		return !this.template.getBounds().contains(player.getBlockPos());
	}

	public boolean teleportToWaitingSpawn(ServerPlayerEntity player) {
		return this.teleportToRandomRegion(player, DragonOnslaughtMapMarkers.WAITING_SPAWN);
	}

	public JoinAcceptorResult.Teleport acceptWaitingSpawnJoins(JoinAcceptor acceptor, ServerWorld world) {
		return this.acceptJoins(acceptor, world, DragonOnslaughtMapMarkers.WAITING_SPAWN).thenRunForEach(player -> {
			player.changeGameMode(GameMode.ADVENTURE);
		});
	}

	public List<TemplateRegion> getSpawns(Random random) {
		return this.getRegions(DragonOnslaughtMapMarkers.SPAWN, random);
	}

	public boolean teleportToSpectatorSpawn(ServerPlayerEntity player) {
		return this.teleportToRandomRegion(player, DragonOnslaughtMapMarkers.SPECTATOR_SPAWN);
	}

	public JoinAcceptorResult.Teleport acceptSpectatorJoins(JoinAcceptor acceptor, ServerWorld world) {
		return this.acceptJoins(acceptor, world, DragonOnslaughtMapMarkers.SPECTATOR_SPAWN).thenRunForEach(player -> {
			player.changeGameMode(GameMode.SPECTATOR);
		});
	}

	private JoinAcceptorResult.Teleport acceptJoins(JoinAcceptor acceptor, ServerWorld world, String marker) {
		TemplateRegion region = this.getRandomRegion(marker, world.getRandom());

		if (region == null) {
			return acceptor.teleport(world, Vec3d.ZERO);
		}

		return this.acceptJoins(acceptor, world, region);
	}

	private JoinAcceptorResult.Teleport acceptJoins(JoinAcceptor acceptor, ServerWorld world, TemplateRegion region) {
		Vec3d pos = region.getBounds().centerBottom();
		float facing = region.getData().getFloat(FACING_KEY, 0);

		return acceptor.teleport(world, pos, facing, 0);
	}

	private boolean teleportToRandomRegion(ServerPlayerEntity player, String marker) {
		TemplateRegion region = this.getRandomRegion(marker, player.getRandom());
		if (region == null) return false;

		this.teleportToRegion(player, region);
		return true;
	}

	public void teleportToRegion(ServerPlayerEntity player, TemplateRegion region) {
		Vec3d pos = region.getBounds().centerBottom();
		float facing = region.getData().getFloat(FACING_KEY, 0);

		player.teleport(player.getWorld(), pos.getX(), pos.getY(), pos.getZ(), Set.of(), facing, 0, true);
	}

	public Vec3d getDragonSpawnPos(ServerWorld world) {
		TemplateRegion region = this.getRandomRegion(DragonOnslaughtMapMarkers.DRAGON_SPAWN, world.getRandom());
		if (region == null) return Vec3d.ZERO;

		return region.getBounds().center();
	}

	private TemplateRegion getRandomRegion(String marker, Random random) {
		List<TemplateRegion> regions = getRegions(marker, random);
		if (regions.isEmpty()) return null;

		return Util.getRandom(regions, random);
	}

	private List<TemplateRegion> getRegions(String marker, Random random) {
		List<TemplateRegion> regions = this.template.getMetadata()
			.getRegions(marker)
			.collect(Collectors.toCollection(ArrayList::new));

		Util.shuffle(regions, random);
		regions.sort(PRIORITY_COMPARATOR);

		return regions;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}

	public static DragonOnslaughtMap create(MinecraftServer server, DragonOnslaughtConfig config) {
		try {
			MapTemplate template = MapTemplateSerializer.loadFromResource(server, config.map().id());
			return new DragonOnslaughtMap(template);
		} catch (IOException exception) {
			throw new GameOpenException(Text.translatable("text.dragononslaught.template_load_failed"), exception);
		}
	}
}
