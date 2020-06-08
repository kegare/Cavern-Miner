package cavern.miner.storage;

import cavern.miner.init.CaveSounds;
import cavern.miner.network.CaveNetworkConstants;
import cavern.miner.network.MinerPointMessage;
import cavern.miner.network.MinerUpdateMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
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

	private MinerCache cache;

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

	@OnlyIn(Dist.CLIENT)
	public MinerRank.DisplayEntry getDisplayRank()
	{
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
		boolean promoted = false;

		if (positive)
		{
			MinerRank.RankEntry next = MinerRank.getNextEntry(getRank());

			if (!getRank().equals(next) && getPoint() >= next.getPhase())
			{
				setPoint(0);
				setRank(next);

				promoted = true;
			}
		}

		if (player != null)
		{
			if (positive && getPoint() % 100 == 0)
			{
				player.giveExperiencePoints(player.xpBarCap() / 2);
			}

			if (promoted)
			{
				MinecraftServer server = player.getServer();

				if (server != null)
				{
					TextComponent rankName = new TranslationTextComponent(getRank().getTranslationKey());
					rankName.getStyle().setBold(true);

					TextComponent message = new TranslationTextComponent("cavern.miner.promoted", player.getDisplayName(), rankName);
					message.getStyle().setColor(TextFormatting.GRAY).setItalic(true);

					server.getPlayerList().sendMessage(message);
				}

				player.world.playSound(null, player.getPosX(), player.getPosY() + 1.0D, player.getPosZ(), CaveSounds.MINER_RANKUP.get(), SoundCategory.AMBIENT, 0.5F, 1.0F);
			}
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

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();

		nbt.putInt("Point", getPoint());
		nbt.putString("Rank", getRank().getName());

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		setPoint(nbt.getInt("Point"));
		setRank(MinerRank.get(nbt.getString("Rank")));
	}

	public MinerCache getCache()
	{
		if (cache == null)
		{
			cache = new MinerCache();
		}

		return cache;
	}

	public Miner clearCache()
	{
		cache = null;
		prevUpdate = null;

		return this;
	}
}