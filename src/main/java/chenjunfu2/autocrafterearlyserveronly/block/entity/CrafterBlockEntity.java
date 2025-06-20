package chenjunfu2.autocrafterearlyserveronly.block.entity;

import chenjunfu2.autocrafterearlyserveronly.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class CrafterBlockEntity extends BlockEntity
{
	public CrafterBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModBlockEntities.CRAFTER_BLOCK_ENTITY, pos, state);
	}
}
