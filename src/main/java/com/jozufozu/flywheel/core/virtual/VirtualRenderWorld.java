package com.jozufozu.flywheel.core.virtual;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.FlywheelWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;

public class VirtualRenderWorld extends Level implements FlywheelWorld {
	public final Map<BlockPos, BlockState> blocksAdded = new HashMap<>();
	public final Map<BlockPos, BlockEntity> tesAdded = new HashMap<>();
	public final Set<SectionPos> spannedSections = new HashSet<>();
	private final BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();

	protected final Level level;
	protected final LevelLightEngine lighter;
	protected final VirtualChunkSource chunkSource;
	protected final LevelEntityGetter<Entity> entityGetter = new VirtualLevelEntityGetter<>();

	protected final int height;
	protected final int minBuildHeight;

	public VirtualRenderWorld(Level level) {
		this(level, level.getHeight(), level.getMinBuildHeight());
	}

	public VirtualRenderWorld(Level level, int height, int minBuildHeight) {
		super((WritableLevelData) level.getLevelData(), level.dimension(), level.dimensionType(), level::getProfiler,
				true, false, 0);
		this.level = level;
		this.height = height;
		this.minBuildHeight = minBuildHeight;
		this.chunkSource = new VirtualChunkSource(this);
		this.lighter = new LevelLightEngine(chunkSource, true, false);
	}

	/**
	 * Run this after you're done using setBlock().
	 */
	public void runLightingEngine() {
		for (Map.Entry<BlockPos, BlockState> entry : blocksAdded.entrySet()) {
			BlockPos pos = entry.getKey();
			BlockState state = entry.getValue();
			int light = state.getLightEmission(this, pos);
			if (light > 0) {
				lighter.onBlockEmissionIncrease(pos, light);
			}
		}

		lighter.runUpdates(Integer.MAX_VALUE, false, false);
	}

	public void setTileEntities(Collection<BlockEntity> tileEntities) {
		tesAdded.clear();
		tileEntities.forEach(te -> tesAdded.put(te.getBlockPos(), te));
	}

	public void clear() {
		blocksAdded.clear();
	}

	// MEANINGFUL OVERRIDES

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
		blocksAdded.put(pos, newState);

		SectionPos sectionPos = SectionPos.of(pos);
		if (spannedSections.add(sectionPos)) {
			lighter.updateSectionStatus(sectionPos, false);
		}

		if ((flags & Block.UPDATE_SUPPRESS_LIGHT) == 0) {
			lighter.checkBlock(pos);
		}

		return true;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getMinBuildHeight() {
		return minBuildHeight;
	}

	@Override
	public ChunkSource getChunkSource() {
		return chunkSource;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return lighter;
	}

	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return entityGetter;
	}

	@Override
	public BlockState getBlockState(@Nullable BlockPos pos) {
		BlockState state = blocksAdded.get(pos);
		if (state != null)
			return state;
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
		return setBlock(pos, state, 0);
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return tesAdded.get(pos);
	}

	@Override
	public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> condition) {
		return condition.test(getBlockState(pos));
	}

	public BlockState getBlockState(int x, int y, int z) {
		return getBlockState(scratch.set(x, y, z));
	}

	// RENDERING CONSTANTS

	@Override
	public int getMaxLocalRawBrightness(BlockPos pos) {
		return 15;
	}

	@Override
	public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
		return 1f;
	}

	// THIN WRAPPERS AHEAD

	@Override
	public RegistryAccess registryAccess() {
		return level.registryAccess();
	}

	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return level.getBlockTicks();
	}

	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return level.getFluidTicks();
	}

	@Override
	public RecipeManager getRecipeManager() {
		return level.getRecipeManager();
	}

	@Override
	public TagContainer getTagManager() {
		return level.getTagManager();
	}

	@Override
	public int getFreeMapId() {
		return level.getFreeMapId();
	}

	@Override
	public Scoreboard getScoreboard() {
		return level.getScoreboard();
	}

	@Override
	public Biome getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
		return level.getUncachedNoiseBiome(p_225604_1_, p_225604_2_, p_225604_3_);
	}

	// UNIMPORTANT CONSTANTS

	@Override
	@Nullable
	public Entity getEntity(int id) {
		return null;
	}

	@Override
	@Nullable
	public MapItemSavedData getMapData(String mapName) {
		return null;
	}

	@Override
	public boolean isLoaded(BlockPos pos) {
		return true;
	}

	@Override
	public boolean isAreaLoaded(BlockPos center, int range) {
		return true;
	}

	@Override
	public List<? extends Player> players() {
		return Collections.emptyList();
	}

	@Override
	public String gatherChunkSourceStats() {
		return "";
	}

	// NOOP

	@Override
	public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {}

	@Override
	public void playSound(@Nullable Player player, double x, double y, double z, SoundEvent soundIn,
			SoundSource category, float volume, float pitch) {}

	@Override
	public void playSound(@Nullable Player p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_,
			SoundSource p_217384_4_, float p_217384_5_, float p_217384_6_) {}

	@Override
	public void setMapData(String pMapId, MapItemSavedData pData) {}

	@Override
	public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

	@Override
	public void updateNeighbourForOutputSignal(BlockPos p_175666_1_, Block p_175666_2_) {}

	@Override
	public void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, BlockPos pPos) {}

	@Override
	public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

	// Override Starlight's ExtendedWorld interface methods:

	public LevelChunk getChunkAtImmediately(final int chunkX, final int chunkZ) {
		return chunkSource.getChunk(chunkX, chunkZ, false);
	}

	public ChunkAccess getAnyChunkImmediately(final int chunkX, final int chunkZ) {
		return chunkSource.getChunk(chunkX, chunkZ);
	}

	// Intentionally copied from LevelHeightAccessor. Lithium overrides these methods so we need to, too.

	@Override
	public int getMaxBuildHeight() {
		return this.getMinBuildHeight() + this.getHeight();
	}

	@Override
	public int getSectionsCount() {
		return this.getMaxSection() - this.getMinSection();
	}

	@Override
	public int getMinSection() {
		return SectionPos.blockToSectionCoord(this.getMinBuildHeight());
	}

	@Override
	public int getMaxSection() {
		return SectionPos.blockToSectionCoord(this.getMaxBuildHeight() - 1) + 1;
	}

	@Override
	public boolean isOutsideBuildHeight(BlockPos pos) {
		return this.isOutsideBuildHeight(pos.getY());
	}

	@Override
	public boolean isOutsideBuildHeight(int y) {
		return y < this.getMinBuildHeight() || y >= this.getMaxBuildHeight();
	}

	@Override
	public int getSectionIndex(int y) {
		return this.getSectionIndexFromSectionY(SectionPos.blockToSectionCoord(y));
	}

	@Override
	public int getSectionIndexFromSectionY(int sectionY) {
		return sectionY - this.getMinSection();
	}

	@Override
	public int getSectionYFromSectionIndex(int sectionIndex) {
		return sectionIndex + this.getMinSection();
	}
}
