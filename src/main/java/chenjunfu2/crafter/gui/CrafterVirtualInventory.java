package chenjunfu2.crafter.gui;

import chenjunfu2.crafter.block.entity.CrafterBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CrafterVirtualInventory implements Inventory
{
	public static final String VIRTUAL_ITEM_TAG = new String("Crafter$Virtual$Item");

	public static final int NUM_COLUMNS = 9;
	public static final int NUM_ROWS = 3;
	public static final int SLOTS_SIZE = 9 * 3;
	
	public static final int CRAFTER_SIZE = 9;
	public static final int CRAFTER_SLOTS_MAP_NULL = -1;
	public static final int[] CRAFTER_SLOTS = {1, 2, 3, 10, 11, 12, 19, 20, 21};//箱子里用于代表合成器内部9格的位置
	public static final int[] CRAFTER_SLOTS_MAP = MakeSlotsMap(CRAFTER_SLOTS,CRAFTER_SIZE,SLOTS_SIZE, CRAFTER_SLOTS_MAP_NULL);//从箱子位置获取合成器内部映射
	public static final int ARROW_SLOTS = 14;//火药与红石粉切换
	public static final int RESULT_SLOTS = 16;//合成产物显示
	
	public final ItemStack EMPTY_STACK = ItemStack.EMPTY;
	public final ItemStack TAG_EMPTY_STACK = MakeVirtualItemWithTag(Items.AIR,1, null,null);
	public final ItemStack LOCK_STACK = MakeVirtualItemWithTag(Items.BARRIER,1, Text.literal("槽位已禁用").setStyle(Style.EMPTY.withColor(Formatting.RED).withBold(true).withItalic(false)),
																						List.of(Text.literal("点击以解锁").setStyle(Style.EMPTY.withColor(Formatting.GREEN).withItalic(false))));
	public final ItemStack DISABLE_STACK = MakeVirtualItemWithTag(Items.RED_STAINED_GLASS_PANE,1,Text.of(""),null);
	public final ItemStack TRIGGERED_STACK = MakeVirtualItemWithTag(Items.REDSTONE,1,Text.literal("已激活").setStyle(Style.EMPTY.withColor(Formatting.RED).withItalic(false)),null);
	public final ItemStack UNTRIGGERED_STACK = MakeVirtualItemWithTag(Items.GUNPOWDER,1,Text.literal("未激活").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)),null);
	
	private final CrafterBlockEntity blockEntity;
	public final CraftingResultInventory resultInventory = new CraftingResultInventory();
	
	static ItemStack MakeVirtualItemWithTag(ItemConvertible item, int count, @Nullable Text name, @Nullable List<Text> loreLines)
	{
		var tmpItem = new ItemStack(item,count);//创建物品
		tmpItem.getOrCreateNbt().putBoolean(VIRTUAL_ITEM_TAG, true);//插入自定义mod标签
		
		//设置名称
		if(name != null)
		{
			tmpItem.setCustomName(name);
		}
		
		//设置附加描述
		if(loreLines!=null && !loreLines.isEmpty())
		{
			NbtCompound displayNbt = tmpItem.getOrCreateSubNbt("display");
			NbtList loreList = new NbtList();
			
			for (Text line : loreLines) {
				// 将 Text 转换为 JSON 字符串（保留样式）
				String jsonText = Text.Serializer.toJson(line);
				loreList.add(NbtString.of(jsonText));
			}
			
			displayNbt.put("Lore", loreList);
		}
		
		return tmpItem;
	}
	
	static int[] MakeSlotsMap(int[] mapKeyArr, int valSize, int mapSize, int mapNull)
	{
		var map = new int [mapSize];
		Arrays.fill(map,mapNull);
		
		if(mapKeyArr.length != valSize)
		{
			return map;
		}
		//进行映射
		int val = 0;
		for(int key : mapKeyArr)
		{
			map[key] = val++;
		}
		
		return map;
	}
	
	CrafterVirtualInventory(CrafterBlockEntity blockEntity)
	{
		this.blockEntity = blockEntity;
	}
	
	public boolean canUseSlot(int slot)
	{
		if(slot < 0 || slot >= SLOTS_SIZE)
		{
			return false;
		}
		
		return CRAFTER_SLOTS_MAP[slot] != CRAFTER_SLOTS_MAP_NULL;
	}
	

	@Override
	public int size()
	{
		return SLOTS_SIZE;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	@Override
	public ItemStack getStack(int slot)
	{
		if(canUseSlot(slot))
		{
			int mapSlot = CRAFTER_SLOTS_MAP[slot];
			//判断slot是不是锁定状态，是返回屏障方块，否正常返回
			if(blockEntity.isSlotDisabled(mapSlot))
			{
				return LOCK_STACK;
			}
			else
			{
				return blockEntity.getStack(mapSlot);
			}
		}
		else if(slot == ARROW_SLOTS)//更新激活状态
		{
			return blockEntity.isTriggered()? TRIGGERED_STACK : UNTRIGGERED_STACK;
		}
		else if(slot == RESULT_SLOTS)
		{
			return resultInventory.getStack(0);
		}
		else
		{
			return DISABLE_STACK;
		}
	}
	
	@Override
	public ItemStack removeStack(int slot, int amount)
	{
		if(canUseSlot(slot))
		{
			int mapSlot = CRAFTER_SLOTS_MAP[slot];
			return blockEntity.removeStack(mapSlot,amount);
		}
		return EMPTY_STACK;
	}
	
	@Override
	public ItemStack removeStack(int slot)
	{
		if(canUseSlot(slot))
		{
			int mapSlot = CRAFTER_SLOTS_MAP[slot];
			return blockEntity.removeStack(mapSlot);
		}
		return EMPTY_STACK;
	}

	@Override
	public void setStack(int slot, ItemStack stack)
	{
		if(canUseSlot(slot))
		{
			int mapSlot = CRAFTER_SLOTS_MAP[slot];
			blockEntity.setStack(mapSlot,stack);
		}
	}
	
	@Override
	public void markDirty()
	{
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player)
	{
		return true;
	}

	@Override
	public void clear()
	{
	}
}
