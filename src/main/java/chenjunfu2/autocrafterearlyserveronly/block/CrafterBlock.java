package chenjunfu2.autocrafterearlyserveronly.block;

import chenjunfu2.autocrafterearlyserveronly.block.entity.CrafterBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class CrafterBlock extends BlockWithEntity implements PolymerBlock
{
	public CrafterBlock(AbstractBlock.Settings settings)
	{
		super(settings);
	}
	
	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state)
	{
		CrafterBlockEntity crafterBlockEntity = new CrafterBlockEntity(pos, state);
		return crafterBlockEntity;
	}
	
	//protected static <B extends Block> RecordCodecBuilder<B, Settings> createSettingsCodec() {
	//	return SETTINGS_CODEC.fieldOf("properties").forGetter(b -> ((AbstractBlockAccessor)b).getSettings());
	//}
	//public static <B extends Block> MapCodec<B> createCodec(Function<Settings, B> blockFromSettings) {
	//	return RecordCodecBuilder.mapCodec((instance) -> instance.group(createSettingsCodec()).apply(instance, blockFromSettings));
	//}
	//
	//@Override
	//public MapCodec<? extends CrafterBlock> getCodec()
	//{
	//	return createCodec(CrafterBlock::new);
	//}
	
	//@Override
	//public BlockRenderType getRenderType(BlockState state) {
	//	return BlockRenderType.MODEL;
	//}
	
	@Override
	public Block getPolymerBlock(BlockState state)
	{
		return Blocks.DROPPER;
	}

}
