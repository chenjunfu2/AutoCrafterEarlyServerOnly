package chenjunfu2.crafter.gui;

import chenjunfu2.crafter.block.CrafterBlock;
import chenjunfu2.crafter.block.entity.CrafterBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;


public class CrafterVirtualGUI extends GenericContainerScreenHandler implements ScreenHandlerListener//ScreenHandler implements ScreenHandlerListener
{
	private final PlayerEntity player;
	private final RecipeInputInventory inputInventory;
	private final CrafterBlockEntity blockEntity;
	private final CrafterVirtualInventory VirtualInventory;
	
	public CrafterBlockEntity getBlockEntity()
	{
		return blockEntity;
	}
	
	public CrafterVirtualInventory getVirtualInventory()
	{
		return VirtualInventory;
	}
	
	public CrafterVirtualGUI(int syncId, PlayerEntity player, CrafterBlockEntity blockEntity)
	{
		super(ScreenHandlerType.GENERIC_9X3, syncId, player.getInventory(), new CrafterVirtualInventory(blockEntity), 3);
		
		this.VirtualInventory = (CrafterVirtualInventory)super.getInventory();
		this.player = player;
		this.blockEntity = blockEntity;
		this.inputInventory = blockEntity;
		
		//for(int slot : this.VirtualInventory.CRAFTER_SLOTS)
		//{
		//	this.addSlot(new CrafterVirtualInputSlot(this.VirtualInventory, this.VirtualInventory.CRAFTER_SLOTS_MAP[slot],0,0,this));
		//}
		//this.addSlot(new CrafterVirtualOutputSlot(this.VirtualInventory.resultInventory, this.VirtualInventory.RESULT_SLOTS, 0, 0, this));
		this.addListener(this);
	}
	
	
	void playSound(PlayerEntity player,
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
		this.blockEntity.setSlotEnabled(slotId, enabled);
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
		
		if (VirtualInventory.canUseSlot(slot) && !player.isSpectator()) {
			int mapSlot = VirtualInventory.CRAFTER_SLOTS_MAP[slot];
			switch (actionType)
			{
			case PICKUP:
				if (this.blockEntity.isSlotDisabled(mapSlot))
				{
					this.setSlotEnabled(mapSlot,true,player);
				}
				else if (this.blockEntity.getStack(mapSlot).isEmpty() &&
						 this.getCursorStack().isEmpty())
				{
					this.setSlotEnabled(mapSlot,false,player);
				}
				break;
			case SWAP:
				ItemStack itemStack = player.getInventory().getStack(button);
				if (this.blockEntity.isSlotDisabled(mapSlot) && !itemStack.isEmpty())
				{
					this.setSlotEnabled(mapSlot,true,player);
				}
			}
		}
		
		Slot slot1 = this.getSlot(slot);
		ItemStack stack = slot1.getStack();
		
		NbtCompound nbt = stack.getNbt();
		if(nbt!=null && nbt.get(VirtualInventory.VIRTUAL_ITEM_TAG) != null)
		{
			if(nbt.getBoolean(VirtualInventory.VIRTUAL_ITEM_TAG))
			{
				//stack.setCount(0);
				return;
			}
		}
		
		super.onSlotClick(slot, button, actionType, player);
		//this.sendContentUpdates();//强制更新回去
	}
	
	private boolean canUseSlot(int slotIdx)
	{
		return this.VirtualInventory.canUseSlot(slotIdx) && !this.blockEntity.isSlotDisabled(slotIdx);
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
			(fromLast == true && this.canUseSlot(slotIdx))))//容器移动到玩家，直接检查
		{
			while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
				if(fromLast == false && !this.canUseSlot(i))//在玩家内部移动到容器，检测容器目标i是否可用
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
			(fromLast == true && this.canUseSlot(slotIdx))))//容器移动到玩家，直接检查
		{
			if (fromLast) {
				i = endIndex - 1;
			} else {
				i = startIndex;
			}
			
			while (fromLast ? i >= startIndex : i < endIndex) {
				if(fromLast == false && !this.canUseSlot(i))//在玩家内部移动到容器，检测容器目标i是否可用
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
	public boolean canUse(PlayerEntity player) {
		return this.inputInventory.canPlayerUse(player);
	}
	
	@Override
	public ItemStack quickMove(PlayerEntity player, int slot)
	{
		//可以的情况下再操作
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot2 = this.slots.get(slot);
		if (slot2 != null && slot2.hasStack()) {
			ItemStack itemStack2 = slot2.getStack();
			itemStack = itemStack2.copy();
			if (slot < super.getRows() * 9) {//如果点击位置在容器内部
				if (!this.insertItemEx(itemStack2, slot,super.getRows() * 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItemEx(itemStack2, slot,0, super.getRows() * 9, false)) {//否则点击位置在玩家内部
				return ItemStack.EMPTY;
			}
	
			if (itemStack2.isEmpty()) {
				slot2.setStack(ItemStack.EMPTY);
			} else {
				slot2.markDirty();
			}
		}
	
		return itemStack;
	}
	
	private void updateResult() {
		if (this.player instanceof ServerPlayerEntity serverPlayerEntity) {
			World world = serverPlayerEntity.getWorld();
			var tmp = CrafterBlock.getCraftingRecipe(world, this.inputInventory).map((recipe) ->
					recipe.craft(this.inputInventory, world.getRegistryManager()));
			ItemStack itemStack;
			if(tmp.isEmpty())
			{
				itemStack = VirtualInventory.TAG_EMPTY_STACK;
			}
			else
			{
				itemStack = tmp.get();
				itemStack.getOrCreateNbt().putBoolean(VirtualInventory.VIRTUAL_ITEM_TAG, true);
			}
			
			VirtualInventory.resultInventory.setStack(0, itemStack);
		}
		
	}
	
	@Override
	public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack)
	{
		this.updateResult();
	}

	@Override
	public void onPropertyUpdate(ScreenHandler handler, int property, int value)
	{
	
	}
}
