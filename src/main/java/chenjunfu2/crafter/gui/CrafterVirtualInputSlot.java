package chenjunfu2.crafter.gui;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class CrafterVirtualInputSlot extends Slot
{
	private final CrafterVirtualGUI gui;
	
	public CrafterVirtualInputSlot(Inventory inventory, int i, int j, int k, CrafterVirtualGUI gui) {
		super(inventory, i, j, k);
		this.gui = gui;
	}
	
	@Override
	public boolean canInsert(ItemStack stack) {
		return !this.gui.getBlockEntity().isSlotDisabled(this.id) && super.canInsert(stack);
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		this.gui.onContentChanged(this.inventory);
	}
}
