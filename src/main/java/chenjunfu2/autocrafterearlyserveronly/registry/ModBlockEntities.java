package chenjunfu2.autocrafterearlyserveronly.registry;

import chenjunfu2.autocrafterearlyserveronly.block.entity.CrafterBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities
{
	public static final BlockEntityType<CrafterBlockEntity> CRAFTER_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(CrafterBlockEntity::new, ModBlocks.CRAFTER_BLOCK).build();
	
	public static void registerBlockEntities()
	{
		Registry.register(Registries.BLOCK_ENTITY_TYPE, ModBlocks.CRAFTER_ID, CRAFTER_BLOCK_ENTITY);
		PolymerBlockUtils.registerBlockEntity(CRAFTER_BLOCK_ENTITY);
	}
}
