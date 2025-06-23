package chenjunfu2.crafter.gui;

import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class CrafterVirtualOutputSlot extends Slot
{
	private final CrafterVirtualGUI gui;
	
	public CrafterVirtualOutputSlot(Inventory inventory, int i, int j, int k, CrafterVirtualGUI gui) {
		super(inventory, i, j, k);
		this.gui = gui;
	}
	
	@Override
	public void onQuickTransfer(ItemStack newItem, ItemStack original) {
	}
	
	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		return false;
	}
	
	@Override
	public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
		return Optional.empty();
	}
	
	@Override
	public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public ItemStack insertStack(ItemStack stack) {
		return stack;
	}
	
	@Override
	public ItemStack insertStack(ItemStack stack, int count) {
		return this.insertStack(stack);
	}
	
	@Override
	public boolean canTakePartial(PlayerEntity player) {
		return false;
	}
	
	@Override
	public boolean canInsert(ItemStack stack) {
		return false;
	}
	
	@Override
	public ItemStack takeStack(int amount) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public void onTakeItem(PlayerEntity player, ItemStack stack) {
	}
	
	@Override
	public boolean canBeHighlighted() {
		return false;
	}
}
