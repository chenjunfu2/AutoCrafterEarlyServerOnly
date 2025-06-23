package chenjunfu2.crafter.gui;

import chenjunfu2.crafter.block.entity.CrafterBlockEntity;
import com.google.common.collect.Lists;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class CrafterGUI extends SimpleGui
{
	//public CrafterGUI(ScreenHandlerType<?> type, ServerPlayerEntity player, boolean manipulatePlayerSlots)
	//{
	//	super(type, player, manipulatePlayerSlots);
	//}
	
	// 左侧3x3区域（0-8号槽位）
	private static final int SLOTS_SIZE = 9*3;
	private static final int[] CRAFTER_SLOTS = {1, 2, 3, 10, 11, 12, 19, 20, 21};
	private static int[] CRAFTER_SLOTS_MAP = new int[SLOTS_SIZE];
	private static final int ARROW_SLOTS = 14;//火药与红石粉切换
	private static final int RESULT_SLOTS = 16;//合成产物显示
	
	private final GuiElement EMPTY = new GuiElement(ItemStack.EMPTY, this::handleSlotClick);
	protected CrafterBlockEntity blockEntity;
	protected ServerPlayerEntity player;
	
	public CrafterGUI(ServerPlayerEntity player, CrafterBlockEntity blockEntity)
	{
		super(ScreenHandlerType.GENERIC_9X3, player, false);
		this.blockEntity = blockEntity;
		this.player = player;
		this.setTitle(Text.literal("合成器"));
		setupGui();
	}
	
	private void setupGui()
	{
		// 设置屏蔽区域（红色染色玻璃板）
		final GuiElement RED_PANE = new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE)
				.setName(Text.empty())
				.build();
		// 填充所有槽位
		for (int i = 0; i < 27; i++)
		{
			this.setSlot(i, RED_PANE);
		}
		
		// 初始化左侧3x3区域
		Arrays.fill(CRAFTER_SLOTS_MAP,-1);
		int i = 0;
		for (int slot : CRAFTER_SLOTS)
		{
			//this.setSlot(slot,EMPTY);
			CRAFTER_SLOTS_MAP[slot] = i;
			this.setSlot(slot, new CrafterSlot(blockEntity, i, this::handleSlotClick));
			++i;
		}
		
		this.setSlot(ARROW_SLOTS, new GuiElementBuilder(Items.GUNPOWDER));
		this.setSlot(RESULT_SLOTS, ItemStack.EMPTY);
	}
	
	private void handleSlotClick(int slot, ClickType click, SlotActionType actionType,  SlotGuiInterface gui)
	{
		int mapSlot = CRAFTER_SLOTS_MAP[slot];
		if(mapSlot != -1)
		{
			switch (actionType)
			{
			case PICKUP:
				if(blockEntity.isSlotDisabled(mapSlot))
				{
					blockEntity.setSlotEnabled(mapSlot,true);
				}
				else if (blockEntity.getStack(mapSlot).isEmpty())
				{
					blockEntity.setSlotEnabled(mapSlot,false);
				}
				else//都没有变动，直接跳出
				{
					break;
				}
				
				//到这里说明上面变动，更新
				var curSlot = this.getSlot(slot);
				if(curSlot instanceof CrafterSlot crafterSlot)
				{
					crafterSlot.Update();
				}
				break;
			}
		}
		
		// 如果槽位是空的并且被点击了
		//if (currentItem.isEmpty() && click == ClickType.MOUSE_LEFT)
		//{
		//	this.setSlot(slot, new GuiElement(LOCK_ITEM.getItemStack(),(index,click1,action,gui1)-> handleItemClick(index,click1,action,gui1,cfs.getSlot())));
		//}
		//else// 否则处理物品操作逻辑
		//{
		//	super.screenHandler.onSlotClick(slot, ClickTypeHelper.getOriginalButtonAndRmv(click), actionType, gui.getPlayer());
		//}
		//var tmp = gui.getPlayer();
		//tmp.sendMessage(Text.of("123"));
	}
	
	@Override
	public boolean canPlayerClose() {
		// 玩家可以正常关闭GUI
		return true;
	}
	
	@Override
	public void onClose()
	{
		super.onClose();
	}
	
}
