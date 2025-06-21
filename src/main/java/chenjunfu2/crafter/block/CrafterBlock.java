package chenjunfu2.crafter.block;

import chenjunfu2.crafter.block.entity.CrafterBlockEntity;
import chenjunfu2.crafter.recipe.RecipeCache;
import chenjunfu2.crafter.registry.ModBlockEntities;
import chenjunfu2.crafter.registry.ModProperties;
import chenjunfu2.crafter.registry.ModWorldEvents;
import chenjunfu2.crafter.util.ItemScattererAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.mixin.object.builder.AbstractBlockAccessor;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.enums.JigsawOrientation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CrafterBlock extends BlockWithEntity implements PolymerBlock
{
	public static final Codec<Settings> SETTINGS_CODEC = Codec.unit(Settings::create);
	public static final MapCodec<CrafterBlock> CODEC = createCodec(CrafterBlock::new);
	public static final BooleanProperty CRAFTING = ModProperties.CRAFTING;
	public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
	private static final EnumProperty<JigsawOrientation> ORIENTATION = ModProperties.ORIENTATION;
	private static final int field_46802 = 6;
	private static final int TRIGGER_DELAY = 4;
	private static final RecipeCache recipeCache = new RecipeCache(10);
	
	protected static <B extends Block> RecordCodecBuilder<B, AbstractBlock.Settings> createSettingsCodec() {
		return SETTINGS_CODEC.fieldOf("properties").forGetter(b -> ((AbstractBlockAccessor)b).getSettings());
	}
	public static <B extends Block> MapCodec<B> createCodec(Function<Settings, B> blockFromSettings) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(createSettingsCodec()).apply(instance, blockFromSettings));
	}
	
	public CrafterBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(ORIENTATION, JigsawOrientation.NORTH_UP).with(TRIGGERED, false).with(CRAFTING, false));
	}
	
	protected MapCodec<CrafterBlock> getCodec() {
		return CODEC;
	}
	
	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return world.getBlockEntity(pos) instanceof CrafterBlockEntity crafterBlockEntity ? crafterBlockEntity.getComparatorOutput() : 0;
	}
	
	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		boolean bl = world.isReceivingRedstonePower(pos);
		boolean bl2 = (Boolean)state.get(TRIGGERED);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (bl && !bl2) {
			world.scheduleBlockTick(pos, this, TRIGGER_DELAY);
			world.setBlockState(pos, state.with(TRIGGERED, true), Block.NOTIFY_LISTENERS);
			this.setTriggered(blockEntity, true);
		} else if (!bl && bl2) {
			world.setBlockState(pos, state.with(TRIGGERED, false).with(CRAFTING, false), Block.NOTIFY_LISTENERS);
			this.setTriggered(blockEntity, false);
		}
	}
	
	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		this.craft(state, world, pos);
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : checkType(type, ModBlockEntities.CRAFTER_BLOCK_ENTITY, CrafterBlockEntity::tickCrafting);
	}
	
	private void setTriggered(@Nullable BlockEntity blockEntity, boolean triggered) {
		if (blockEntity instanceof CrafterBlockEntity crafterBlockEntity) {
			crafterBlockEntity.setTriggered(triggered);
		}
	}
	
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		CrafterBlockEntity crafterBlockEntity = new CrafterBlockEntity(pos, state);
		crafterBlockEntity.setTriggered(state.contains(TRIGGERED) && (Boolean)state.get(TRIGGERED));
		return crafterBlockEntity;
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		Direction direction = ctx.getPlayerLookDirection().getOpposite();
		
		Direction direction2 = switch (direction) {
			case DOWN -> ctx.getHorizontalPlayerFacing().getOpposite();
			case UP -> ctx.getHorizontalPlayerFacing();
			case NORTH, SOUTH, WEST, EAST -> Direction.UP;
		};
		return this.getDefaultState()
				.with(ORIENTATION, JigsawOrientation.byDirections(direction, direction2))
				.with(TRIGGERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
	}
	
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		if (itemStack.hasCustomName() && world.getBlockEntity(pos) instanceof CrafterBlockEntity crafterBlockEntity) {
			crafterBlockEntity.setCustomName(itemStack.getName());
		}
		
		if ((Boolean)state.get(TRIGGERED)) {
			world.scheduleBlockTick(pos, this, TRIGGER_DELAY);
		}
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		ItemScattererAccessor.onStateReplaced(state, newState, world, pos);
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		} else {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof CrafterBlockEntity) {
				player.openHandledScreen((CrafterBlockEntity)blockEntity);
			}
			
			return ActionResult.CONSUME;
		}
	}
	
	protected void craft(BlockState state, ServerWorld world, BlockPos pos) {
		if (world.getBlockEntity(pos) instanceof CrafterBlockEntity crafterBlockEntity) {
			Optional<CraftingRecipe> optional = getCraftingRecipe(world, crafterBlockEntity);
			if (optional.isEmpty()) {
				world.syncWorldEvent(ModWorldEvents.CRAFTER_FAILS, pos, 0);
			} else {
				crafterBlockEntity.setCraftingTicksRemaining(6);
				world.setBlockState(pos, state.with(CRAFTING, true), Block.NOTIFY_LISTENERS);
				CraftingRecipe craftingRecipe = (CraftingRecipe)optional.get();
				ItemStack itemStack = craftingRecipe.craft(crafterBlockEntity, world.getRegistryManager());
				this.transferOrSpawnStack(world, pos, crafterBlockEntity, itemStack, state);
				craftingRecipe.getRemainder(crafterBlockEntity).forEach(stack -> this.transferOrSpawnStack(world, pos, crafterBlockEntity, stack, state));
				crafterBlockEntity.getInputStacks().forEach(stack -> {
					if (!stack.isEmpty()) {
						stack.decrement(1);
					}
				});
				crafterBlockEntity.markDirty();
			}
		}
	}
	
	public static Optional<CraftingRecipe> getCraftingRecipe(World world, RecipeInputInventory inputInventory) {
		return recipeCache.getRecipe(world, inputInventory);
	}
	
	private void transferOrSpawnStack(World world, BlockPos pos, CrafterBlockEntity blockEntity, ItemStack stack, BlockState state) {
		Direction direction = ((JigsawOrientation)state.get(ORIENTATION)).getFacing();
		Inventory inventory = HopperBlockEntity.getInventoryAt(world, pos.offset(direction));
		ItemStack itemStack = stack.copy();
		if (inventory != null && (inventory instanceof CrafterBlockEntity || stack.getCount() > inventory.getMaxCountPerStack())) {
			while (!itemStack.isEmpty()) {
				ItemStack itemStack2 = itemStack.copyWithCount(1);
				ItemStack itemStack3 = HopperBlockEntity.transfer(blockEntity, inventory, itemStack2, direction.getOpposite());
				if (!itemStack3.isEmpty()) {
					break;
				}
				
				itemStack.decrement(1);
			}
		} else if (inventory != null) {
			while (!itemStack.isEmpty()) {
				int i = itemStack.getCount();
				itemStack = HopperBlockEntity.transfer(blockEntity, inventory, itemStack, direction.getOpposite());
				if (i == itemStack.getCount()) {
					break;
				}
			}
		}
		
		if (!itemStack.isEmpty()) {
			Vec3d vec3d = Vec3d.ofCenter(pos).offset(direction, 0.7);
			ItemDispenserBehavior.spawnItem(world, itemStack, 6, direction, vec3d);
			world.syncWorldEvent(ModWorldEvents.CRAFTER_CRAFTS, pos, 0);
			world.syncWorldEvent(ModWorldEvents.CRAFTER_SHOOTS, pos, direction.getId());
		}
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(ORIENTATION, rotation.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
	}
	
	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.with(ORIENTATION, mirror.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ORIENTATION, TRIGGERED, CRAFTING);
	}

	@Override
	public Block getPolymerBlock(BlockState state)
	{
		return Blocks.DROPPER;
	}
	
	private static final Map<JigsawOrientation, Direction> TO_FACING_MAP = Map.ofEntries(
			Map.entry(JigsawOrientation.DOWN_EAST, Direction.DOWN),
			Map.entry(JigsawOrientation.DOWN_NORTH, Direction.DOWN),
			Map.entry(JigsawOrientation.DOWN_SOUTH, Direction.DOWN),
			Map.entry(JigsawOrientation.DOWN_WEST, Direction.DOWN),
			Map.entry(JigsawOrientation.UP_EAST, Direction.UP),
			Map.entry(JigsawOrientation.UP_NORTH, Direction.UP),
			Map.entry(JigsawOrientation.UP_SOUTH, Direction.UP),
			Map.entry(JigsawOrientation.UP_WEST, Direction.UP),
			Map.entry(JigsawOrientation.WEST_UP, Direction.WEST),
			Map.entry(JigsawOrientation.EAST_UP, Direction.EAST),
			Map.entry(JigsawOrientation.NORTH_UP, Direction.NORTH),
			Map.entry(JigsawOrientation.SOUTH_UP, Direction.SOUTH)
	);
	
	@Override
	public BlockState getPolymerBlockState(BlockState state)
	{
		return Blocks.DROPPER.getDefaultState()
				.with(Properties.FACING, TO_FACING_MAP.get(state.get(ModProperties.ORIENTATION)))
				.with(Properties.TRIGGERED,state.get(Properties.TRIGGERED));
	}
}
