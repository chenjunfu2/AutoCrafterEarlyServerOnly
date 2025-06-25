package chenjunfu2.crafter.gui;

import chenjunfu2.crafter.block.entity.CrafterBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;


public class CrafterVirtualGUI extends GenericContainerScreenHandler implements ScreenHandlerListener//ScreenHandler implements ScreenHandlerListener
{
	private final PlayerEntity player;
	private final CrafterVirtualInventory VirtualInventory;
	
	public CrafterVirtualGUI(int syncId, PlayerEntity player, CrafterBlockEntity blockEntity)
	{
		super(ScreenHandlerType.GENERIC_9X3, syncId, player.getInventory(), new CrafterVirtualInventory(blockEntity), 3);
		
		//这个就是刚刚创建的在super内部的，Java导致必须先super初始化，所以只能用这种丑陋的方式再次从父类中获取
		this.VirtualInventory = (CrafterVirtualInventory)super.getInventory();
		this.player = player;
		
		this.addListener(this);
	}
	
	
	static void playSound(PlayerEntity player,
	               RegistryEntry<SoundEvent> sound,
	               SoundCategory category,
	               float volume,
	               float pitch)
	{
		if(player instanceof ServerPlayerEntity serverPlayerEntity)
		{
			serverPlayerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(sound, category, serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), volume, pitch, serverPlayerEntity.getWorld().getRandom().nextLong()));
		}
	}
	
	private void setSlotEnabled(int slotId, boolean enabled, PlayerEntity player) {
		this.VirtualInventory.setCrafterMapSlotEnabled(slotId, enabled);
		this.sendContentUpdates();
		playSound(player, SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 0.4F, enabled ? 1.0F : 0.75F);
	}
	
	@Override
	public void onSlotClick(int slot, int button, SlotActionType actionType, PlayerEntity player)
	{
		if(slot < 0)
		{
			super.onSlotClick(slot, button, actionType, player);
			return;
		}
		
		if (VirtualInventory.isCrafterSlot(slot) &&//首先检测槽位是否可用
			!player.isSpectator())//并确认玩家非旁观
		{
			switch (actionType)
			{
			case PICKUP://点击事件
				if (this.VirtualInventory.isCrafterMapSlotDisabled(slot))//如果合成器slot是禁用的情况下
				{
					this.setSlotEnabled(slot,true,player);//启用槽位
				}
				else if (this.VirtualInventory.isCrafterMapSlotEmpty(slot) &&//如果合成器slot为空
						 this.getCursorStack().isEmpty())//并且鼠标没悬挂物品
				{
					this.setSlotEnabled(slot,false,player);//那么锁定槽位
				}
				break;
			case SWAP://交换物品事件
				if (this.VirtualInventory.isCrafterMapSlotDisabled(slot) &&//如果槽位是禁用的
					!player.getInventory().getStack(button).isEmpty())//并且玩家槽位不为空
				{
					this.setSlotEnabled(slot,true,player);//启用槽位，并在后面委托父类处理
				}
			}
		}
		
		//如果槽位刚刚被禁用则这里直接返回，否则启用后此处不会返回，其他mod标签物品跳过处理
		if(VirtualInventory.isVirtualItem(this.getSlot(slot).getStack()))
		{
			return;//虚拟物品跳过父类处理，防止玩家使用
		}
		
		
		super.onSlotClick(slot, button, actionType, player);
	}
	
	private boolean canPutSlotItem(int slotIdx)
	{
		return this.VirtualInventory.isCrafterSlot(slotIdx) && !this.VirtualInventory.isCrafterMapSlotDisabled(slotIdx);
	}
	
	protected boolean insertItemEx(ItemStack stack,int slotIdx, int startIndex, int endIndex, boolean fromLast) {
		boolean bl = false;
		int i = startIndex;
		if (fromLast) {
			i = endIndex - 1;
		}
		//fromLast == true 容器移动到玩家
		//fromLast == false 玩家移动到容器
		
		if (stack.isStackable() &&
			(fromLast == false ||// 玩家移动到容器 在内部while内检查
			(fromLast == true && this.canPutSlotItem(slotIdx))))//容器移动到玩家，直接检查
		{
			while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
				if(fromLast == false && !this.canPutSlotItem(i))//在玩家内部移动到容器，检测容器目标i是否可用
				{
					if (fromLast) {
						i--;
					} else {
						i++;
					}
					continue;//不可用跳过
				}
				
				Slot slot = this.slots.get(i);
				ItemStack itemStack = slot.getStack();
				if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack)) {
					int j = itemStack.getCount() + stack.getCount();
					if (j <= stack.getMaxCount()) {
						stack.setCount(0);
						itemStack.setCount(j);
						slot.markDirty();
						bl = true;
					} else if (itemStack.getCount() < stack.getMaxCount()) {
						stack.decrement(stack.getMaxCount() - itemStack.getCount());
						itemStack.setCount(stack.getMaxCount());
						slot.markDirty();
						bl = true;
					}
				}
				
				if (fromLast) {
					i--;
				} else {
					i++;
				}
			}
		}
		
		if (!stack.isEmpty() &&
			(fromLast == false ||// 玩家移动到容器 在内部while内检查
			(fromLast == true && this.canPutSlotItem(slotIdx))))//容器移动到玩家，直接检查
		{
			if (fromLast) {
				i = endIndex - 1;
			} else {
				i = startIndex;
			}
			
			while (fromLast ? i >= startIndex : i < endIndex) {
				if(fromLast == false && !this.canPutSlotItem(i))//在玩家内部移动到容器，检测容器目标i是否可用
				{
					if (fromLast) {
						i--;
					} else {
						i++;
					}
					continue;//不可用跳过
				}
				
				Slot slotx = this.slots.get(i);
				ItemStack itemStackx = slotx.getStack();
				if (itemStackx.isEmpty() && slotx.canInsert(stack)) {
					if (stack.getCount() > slotx.getMaxItemCount()) {
						slotx.setStack(stack.split(slotx.getMaxItemCount()));
					} else {
						slotx.setStack(stack.split(stack.getCount()));
					}
					
					slotx.markDirty();
					bl = true;
					break;
				}
				
				if (fromLast) {
					i--;
				} else {
					i++;
				}
			}
		}
		
		return bl;
	}
	
	@Override
	public boolean canUse(PlayerEntity player)
	{
		return this.VirtualInventory.canPlayerUse(player);
	}
	
	@Override
	public ItemStack quickMove(PlayerEntity player, int slot)
	{
		//可以的情况下再操作
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot2 = this.slots.get(slot);
		if (slot2 != null && slot2.hasStack())
		{
			ItemStack itemStack2 = slot2.getStack();
			itemStack = itemStack2.copy();
			if (slot < super.getRows() * 9)
			{//如果点击位置在容器内部
				if (!this.insertItemEx(itemStack2, slot,super.getRows() * 9, this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.insertItemEx(itemStack2, slot,0, super.getRows() * 9, false))
			{//否则点击位置在玩家内部
				return ItemStack.EMPTY;
			}
	
			if (itemStack2.isEmpty())
			{
				slot2.setStack(ItemStack.EMPTY);
			}
			else
			{
				slot2.markDirty();
			}
		}
	
		return itemStack;
	}
	
	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack)
	{
		if (this.player instanceof ServerPlayerEntity serverPlayerEntity)
		{
			this.VirtualInventory.UpdateCraftingRecipe(serverPlayerEntity.getWorld());
		}
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value)
	{
	}
}
