package chenjunfu2.crafter.block.entity;

import chenjunfu2.crafter.block.CrafterBlock;
import chenjunfu2.crafter.registry.ModBlockEntities;
import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class CrafterBlockEntity extends LootableContainerBlockEntity implements RecipeInputInventory {
	public static final int GRID_WIDTH = 3;
	public static final int GRID_HEIGHT = 3;
	public static final int GRID_SIZE = 9;
	public static final int SLOT_DISABLED = 1;
	public static final int SLOT_ENABLED = 0;
	public static final int TRIGGERED_PROPERTY = 9;
	public static final int PROPERTIES_COUNT = 10;
	private DefaultedList<ItemStack> inputStacks = DefaultedList.ofSize(GRID_SIZE, ItemStack.EMPTY);
	private int craftingTicksRemaining = 0;
	protected final PropertyDelegate propertyDelegate = new PropertyDelegate() {
		private final int[] disabledSlots = new int[GRID_SIZE];
		private int triggered = 0;
		
		@Override
		public int get(int index) {
			return index == TRIGGERED_PROPERTY ? this.triggered : this.disabledSlots[index];
		}
		
		@Override
		public void set(int index, int value) {
			if (index == TRIGGERED_PROPERTY) {
				this.triggered = value;
			} else {
				this.disabledSlots[index] = value;
			}
		}
		
		@Override
		public int size() {
			return PROPERTIES_COUNT;
		}
	};
	
	public CrafterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CRAFTER_BLOCK_ENTITY, pos, state);
	}
	
	public PropertyDelegate getPropertyDelegate()
	{
		return propertyDelegate;
	}
	
	@Override
	protected Text getContainerName() {
		return Text.translatable("container.crafter");
	}
	
	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		//return new CrafterScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
		return null;
	}
	
	public void setSlotEnabled(int slot, boolean enabled) {
		if (this.canToggleSlot(slot)) {
			this.propertyDelegate.set(slot, enabled ? SLOT_ENABLED : SLOT_DISABLED);
			this.markDirty();
		}
	}
	
	public boolean isSlotDisabled(int slot) {
		return slot >= 0 && slot < GRID_SIZE ? this.propertyDelegate.get(slot) == SLOT_DISABLED : false;
	}
	
	@Override
	public boolean isValid(int slot, ItemStack stack) {
		if (this.propertyDelegate.get(slot) == SLOT_DISABLED) {
			return false;
		} else {
			ItemStack itemStack = this.inputStacks.get(slot);
			int i = itemStack.getCount();
			if (i >= itemStack.getMaxCount()) {
				return false;
			} else {
				return itemStack.isEmpty() ? true : !this.betterSlotExists(i, itemStack, slot);
			}
		}
	}
	
	private boolean betterSlotExists(int count, ItemStack stack, int slot) {
		for (int i = slot + 1; i < GRID_SIZE; i++) {
			if (!this.isSlotDisabled(i)) {
				ItemStack itemStack = this.getStack(i);
				if (itemStack.isEmpty() || itemStack.getCount() < count && ItemStack.canCombine(itemStack, stack)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		this.craftingTicksRemaining = nbt.getInt("crafting_ticks_remaining");
		this.inputStacks = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
		if (!this.deserializeLootTable(nbt)) {
			Inventories.readNbt(nbt, this.inputStacks);
		}
		
		int[] is = nbt.getIntArray("disabled_slots");
		
		for (int i = 0; i < GRID_SIZE; i++) {
			this.propertyDelegate.set(i, 0);
		}
		
		for (int j : is) {
			if (this.canToggleSlot(j)) {
				this.propertyDelegate.set(j, 1);
			}
		}
		
		this.propertyDelegate.set(TRIGGERED_PROPERTY, nbt.getInt("triggered"));
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putInt("crafting_ticks_remaining", this.craftingTicksRemaining);
		if (!this.serializeLootTable(nbt)) {
			Inventories.writeNbt(nbt, this.inputStacks);
		}
		
		this.putDisabledSlots(nbt);
		this.putTriggered(nbt);
	}
	
	@Override
	public int size() {
		return GRID_SIZE;
	}
	
	@Override
	public boolean isEmpty() {
		for (ItemStack itemStack : this.inputStacks) {
			if (!itemStack.isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public ItemStack getStack(int slot) {
		return this.inputStacks.get(slot);
	}
	
	@Override
	public void setStack(int slot, ItemStack stack) {
		if (this.isSlotDisabled(slot)) {
			this.setSlotEnabled(slot, true);
		}
		
		super.setStack(slot, stack);
	}
	
	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return this.world != null && this.world.getBlockEntity(this.pos) == this
				? !(player.squaredDistanceTo(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) > 64.0)
				: false;
	}
	
	@Override
	public DefaultedList<ItemStack> getInvStackList() {
		return this.inputStacks;
	}
	
	@Override
	protected void setInvStackList(DefaultedList<ItemStack> list) {
		this.inputStacks = list;
	}
	
	@Override
	public int getWidth() {
		return GRID_WIDTH;
	}
	
	@Override
	public int getHeight() {
		return GRID_HEIGHT;
	}
	
	@Override
	public List<ItemStack> getInputStacks()
	{
		return this.inputStacks;
	}
	
	@Override
	public void provideRecipeInputs(RecipeMatcher finder) {
		for (ItemStack itemStack : this.inputStacks) {
			finder.addUnenchantedInput(itemStack);
		}
	}
	
	private void putDisabledSlots(NbtCompound nbt) {
		IntList intList = new IntArrayList();
		
		for (int i = 0; i < GRID_SIZE; i++) {
			if (this.isSlotDisabled(i)) {
				intList.add(i);
			}
		}
		
		nbt.putIntArray("disabled_slots", intList);
	}
	
	private void putTriggered(NbtCompound nbt) {
		nbt.putInt("triggered", this.propertyDelegate.get(TRIGGERED_PROPERTY));
	}
	
	public void setTriggered(boolean triggered) {
		this.propertyDelegate.set(TRIGGERED_PROPERTY, triggered ? 1 : 0);
	}
	
	@VisibleForTesting
	public boolean isTriggered() {
		return this.propertyDelegate.get(TRIGGERED_PROPERTY) == 1;
	}
	
	public static void tickCrafting(World world, BlockPos pos, BlockState state, CrafterBlockEntity blockEntity) {
		int i = blockEntity.craftingTicksRemaining - 1;
		if (i >= 0) {
			blockEntity.craftingTicksRemaining = i;
			if (i == 0) {
				world.setBlockState(pos, state.with(CrafterBlock.CRAFTING, false), Block.NOTIFY_ALL);
			}
		}
	}
	
	public void setCraftingTicksRemaining(int craftingTicksRemaining) {
		this.craftingTicksRemaining = craftingTicksRemaining;
	}
	
	public int getComparatorOutput() {
		int i = 0;
		
		for (int j = 0; j < this.size(); j++) {
			ItemStack itemStack = this.getStack(j);
			if (!itemStack.isEmpty() || this.isSlotDisabled(j)) {
				i++;
			}
		}
		
		return i;
	}
	
	private boolean canToggleSlot(int slot) {
		return slot > -1 && slot < GRID_SIZE && this.inputStacks.get(slot).isEmpty();
	}
}
