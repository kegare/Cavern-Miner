package cavern.miner.storage;

import cavern.miner.enchantment.MinerUnit;
import cavern.miner.init.CaveSounds;
import cavern.miner.network.CaveNetworkConstants;
import cavern.miner.network.MinerPointMessage;
import cavern.miner.network.MinerUpdateMessage;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public class Miner implements INBTSerializable<CompoundNBT>
{
	private final PlayerEntity player;

	private int point;
	private MinerRank.RankEntry rank;

	@OnlyIn(Dist.CLIENT)
	private MinerRank.DisplayEntry displayRank;

	private MinerRecord record;

	private MinerCache cache;
	private MinerUnit unit;

	private MinerUpdateMessage prevUpdate;

	public Miner(PlayerEntity player)
	{
		this.player = player;
	}

	public int getPoint()
	{
		return point;
	}

	public Miner setPoint(int value)
	{
		point = Math.max(value, 0);

		return this;
	}

	public MinerRank.RankEntry getRank()
	{
		if (rank == null)
		{
			rank = MinerRank.BEGINNER;
		}

		return rank;
	}

	public Miner setRank(MinerRank.RankEntry value)
	{
		rank = value;

		return this;
	}

	public Miner setRank(String value)
	{
		setRank(MinerRank.get(value, getRank()));

		return this;
	}

	public Miner promoteRank(MinerRank.RankEntry value)
	{
		MinerRank.RankEntry prev = getRank();

		setRank(value).setPoint(0);

		if (prev != getRank() && player != null)
		{
			MinecraftServer server = player.getServer();

			if (server != null)
			{
				TextComponent rankName = new TranslationTextComponent(getRank().getTranslationKey());
				rankName.getStyle().setBold(true);

				TextComponent message = new TranslationTextComponent("cavern.miner.promoted", player.getDisplayName(), rankName);
				message.getStyle().setColor(TextFormatting.GRAY).setItalic(true);

				server.getPlayerList().sendMessage(message);

				ResourceLocation key = new ResourceLocation("cavern", getRank().getName().toLowerCase() + "_miner");
				Advancement advancement = server.getAdvancementManager().getAdvancement(key);

				if (advancement != null && player instanceof ServerPlayerEntity)
				{
					PlayerAdvancements advancements = ((ServerPlayerEntity)player).getAdvancements();

					if (!advancements.getProgress(advancement).isDone())
					{
						for (String criterionKey : advancements.getProgress(advancement).getRemaningCriteria())
						{
							advancements.grantCriterion(advancement, criterionKey);
						}
					}
				}
			}

			CaveSounds.MINER_RANKUP.ifPresent(o -> player.world.playSound(null, player.getPosX(), player.getPosY() + 1.0D, player.getPosZ(), o, SoundCategory.AMBIENT, 0.5F, 1.0F));
		}

		return this;
	}

	public Miner promoteRank(String value)
	{
		promoteRank(MinerRank.get(value, getRank()));

		return this;
	}

	@OnlyIn(Dist.CLIENT)
	public MinerRank.DisplayEntry getDisplayRank()
	{
		if (displayRank == null)
		{
			displayRank = new MinerRank.DisplayEntry(getRank());
		}

		return displayRank;
	}

	@OnlyIn(Dist.CLIENT)
	public void setDisplayRank(MinerRank.DisplayEntry rank)
	{
		displayRank = rank;
	}

	public Miner addPoint(int amount)
	{
		if (amount == 0)
		{
			return this;
		}

		setPoint(getPoint() + amount);

		boolean positive = amount > 0;

		if (positive)
		{
			MinerRank.RankEntry next = MinerRank.getNextEntry(getRank());

			if (!getRank().equals(next) && getPoint() >= next.getPhase())
			{
				promoteRank(next);
			}
		}

		if (positive && player != null && getPoint() % 100 == 0)
		{
			player.giveExperiencePoints(player.xpBarCap() / 2);
		}

		return this;
	}

	public Miner sendToClient()
	{
		if (player != null && player instanceof ServerPlayerEntity)
		{
			PacketTarget target = PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player);

			if (prevUpdate != null && getRank().equals(prevUpdate.getRank()))
			{
				CaveNetworkConstants.PLAY.send(target, new MinerPointMessage(getPoint()));
			}
			else
			{
				prevUpdate = new MinerUpdateMessage(getPoint(), getRank());

				CaveNetworkConstants.PLAY.send(target, prevUpdate);
			}
		}

		return this;
	}

	public MinerRecord getRecord()
	{
		if (record == null)
		{
			record = new MinerRecord();
		}

		return record;
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();

		nbt.putInt("Point", getPoint());
		nbt.putString("Rank", getRank().getName());
		nbt.put("Record", getRecord().serializeNBT());

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		setPoint(nbt.getInt("Point"));
		setRank(MinerRank.get(nbt.getString("Rank")));
		getRecord().deserializeNBT(nbt.getCompound("Record"));
	}

	public MinerCache getCache()
	{
		if (cache == null)
		{
			cache = new MinerCache();
		}

		return cache;
	}

	public MinerUnit getUnit()
	{
		if (unit == null)
		{
			unit = new MinerUnit(player);
		}

		return unit;
	}

	public Miner clearCache()
	{
		cache = null;
		unit = null;
		prevUpdate = null;

		return this;
	}
}