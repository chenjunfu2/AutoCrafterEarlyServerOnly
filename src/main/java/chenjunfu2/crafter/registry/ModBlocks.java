package chenjunfu2.crafter.registry;

import chenjunfu2.crafter.block.CrafterBlock;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks
{
	public static final Identifier CRAFTER_ID = new Identifier("crafter");
	public static final Block CRAFTER_BLOCK = new CrafterBlock(
			FabricBlockSettings.create()
					.mapColor(MapColor.STONE_GRAY)
					.strength(1.5F, 3.5F)
	);
	//public static final BlockItem CRAFTER_ITEM = new BlockItem(CRAFTER_BLOCK, new FabricItemSettings());
	public static final PolymerBlockItem POLYMER_CRAFTER_ITEM = new PolymerBlockItem(CRAFTER_BLOCK, new FabricItemSettings(), Items.DROPPER);
	
	public static void registerBlocks()
	{
		Registry.register(Registries.BLOCK, CRAFTER_ID, CRAFTER_BLOCK);
		//Registry.register(Registries.ITEM, CRAFTER_ID, CRAFTER_ITEM);
		Registry.register(Registries.ITEM, CRAFTER_ID, POLYMER_CRAFTER_ITEM);
	}
}
