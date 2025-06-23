package chenjunfu2.crafter.gui;

import chenjunfu2.crafter.block.entity.CrafterBlockEntity;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CrafterSlot extends GuiElement
{
	protected CrafterBlockEntity blockEntity;
	protected int slot;
	
	// 屏障物品（用于锁定槽位）
	public final GuiElement LOCK_ITEM = new GuiElementBuilder(Items.BARRIER)
			.setName(Text.literal("已锁定").formatted(Formatting.RED))
			.addLoreLine(Text.literal("点击以解锁").formatted(Formatting.GRAY))
			.build();
	
	public CrafterSlot(CrafterBlockEntity blockEntity, int slot, ClickCallback callback)
	{
		super(blockEntity.getStack(slot), callback);
		this.blockEntity = blockEntity;
		this.slot = slot;
		Update();
	}
	
	public CrafterSlot(CrafterBlockEntity blockEntity, int slot, ItemClickCallback callback)
	{
		super(blockEntity.getStack(slot), callback);
		this.blockEntity = blockEntity;
		this.slot = slot;
		Update();
	}
	
	void Update()
	{
		if(blockEntity.isSlotDisabled(slot))
		{
			super.setItemStack(LOCK_ITEM.getItemStack());
		}
		else
		{
			super.setItemStack(this.blockEntity.getStack(this.slot));
		}
	}
	
	@Override
	public ItemStack getItemStackForDisplay(GuiInterface gui)
	{
		return this.item;
	}
}
