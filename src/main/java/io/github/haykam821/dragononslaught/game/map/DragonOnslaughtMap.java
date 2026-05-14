package io.github.haykam821.dragononslaught.game.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.haykam821.dragononslaught.game.DragonOnslaughtConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameOpenException;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;

public class DragonOnslaughtMap {
	private static final String FACING_KEY = "Facing";
	private static final String PRIORITY_KEY = "Priority";

	private static final Comparator<TemplateRegion> PRIORITY_COMPARATOR = Comparator.comparingInt(region -> {
		CompoundTag data = region.getData();
		return data == null ? 0 : data.getIntOr(PRIORITY_KEY, 0);
	});

	private final MapTemplate template;

	public DragonOnslaughtMap(MapTemplate template) {
		this.template = template;
	}

	public boolean isOutOfBounds(ServerPlayer player) {
		return !this.template.getBounds().contains(player.blockPosition());
	}

	public void teleportToWaitingSpawn(ServerPlayer player) {
		this.teleportToRandomRegion(player, DragonOnslaughtMapMarkers.WAITING_SPAWN);
	}

	public JoinAcceptorResult.Teleport acceptWaitingSpawnJoins(JoinAcceptor acceptor, ServerLevel level) {
		return this.acceptJoins(acceptor, level, DragonOnslaughtMapMarkers.WAITING_SPAWN).thenRunForEach(player -> player.setGameMode(GameType.ADVENTURE));
	}

	public List<TemplateRegion> getSpawns(RandomSource random) {
		return this.getRegions(DragonOnslaughtMapMarkers.SPAWN, random);
	}

	public boolean teleportToSpectatorSpawn(ServerPlayer player) {
		return this.teleportToRandomRegion(player, DragonOnslaughtMapMarkers.SPECTATOR_SPAWN);
	}

	public JoinAcceptorResult.Teleport acceptSpectatorJoins(JoinAcceptor acceptor, ServerLevel level) {
		return this.acceptJoins(acceptor, level, DragonOnslaughtMapMarkers.SPECTATOR_SPAWN).thenRunForEach(player -> player.setGameMode(GameType.SPECTATOR));
	}

	private JoinAcceptorResult.Teleport acceptJoins(JoinAcceptor acceptor, ServerLevel level, String marker) {
		TemplateRegion region = this.getRandomRegion(marker, level.getRandom());

		if (region == null) {
			return acceptor.teleport(level, Vec3.ZERO);
		}

		return this.acceptJoins(acceptor, level, region);
	}

	private JoinAcceptorResult.Teleport acceptJoins(JoinAcceptor acceptor, ServerLevel level, TemplateRegion region) {
		Vec3 pos = region.getBounds().centerBottom();
		float facing = region.getData().getFloatOr(FACING_KEY, 0);

		return acceptor.teleport(level, pos, facing, 0);
	}

	private boolean teleportToRandomRegion(ServerPlayer player, String marker) {
		TemplateRegion region = this.getRandomRegion(marker, player.getRandom());
		if (region == null) return false;

		this.teleportToRegion(player, region);
		return true;
	}

	public void teleportToRegion(ServerPlayer player, TemplateRegion region) {
		Vec3 pos = region.getBounds().centerBottom();
		float facing = region.getData().getFloatOr(FACING_KEY, 0);

		player.teleportTo(player.level(), pos.x(), pos.y(), pos.z(), Set.of(), facing, 0, true);
	}

	public Vec3 getDragonSpawnPos(ServerLevel level) {
		TemplateRegion region = this.getRandomRegion(DragonOnslaughtMapMarkers.DRAGON_SPAWN, level.getRandom());
		if (region == null) return Vec3.ZERO;

		return region.getBounds().center();
	}

	private TemplateRegion getRandomRegion(String marker, RandomSource random) {
		List<TemplateRegion> regions = getRegions(marker, random);
		if (regions.isEmpty()) return null;

		return Util.getRandom(regions, random);
	}

	private List<TemplateRegion> getRegions(String marker, RandomSource random) {
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
			throw new GameOpenException(Component.translatable("text.dragononslaught.template_load_failed"), exception);
		}
	}
}
