package cavern.miner.item;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CavenicAxeItem extends AxeItem implements CavenicTool
{
	public CavenicAxeItem(IItemTier tier, float attackDamage, float attackSpeed, Properties builder)
	{
		super(tier, attackDamage, attackSpeed, builder);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity livingEntity)
	{
		if (!super.onBlockDestroyed(stack, world, state, pos, livingEntity))
		{
			return false;
		}

		CavenicTool.growUseCount(stack);

		return true;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state)
	{
		return super.getDestroySpeed(stack, state) + (CavenicTool.getUseCount(stack) * 0.02F);
	}

	@Override
	public Rarity getRarity(ItemStack stack)
	{
		if (CavenicTool.getUseCount(stack) < 900)
		{
			return super.getRarity(stack);
		}

		return Rarity.EPIC;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		super.addInformation(stack, world, tooltip, flag);

		ITextComponent desc = new TranslationTextComponent("item.cavern.cavenic_tool.boost");
		int count = CavenicTool.getUseCount(stack);
		int progress = 0;

		if (count > 0)
		{
			progress = Math.max(MathHelper.floor((count / 900.0D) * 100.0D), 1);
		}

		if (progress > 0)
		{
			desc.appendText(" " + progress + "%");
		}

		desc.applyTextStyle(TextFormatting.GRAY);

		tooltip.add(desc);
	}
}