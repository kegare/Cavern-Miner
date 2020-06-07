package cavern.miner.storage;

import cavern.miner.init.CaveSounds;
import cavern.miner.network.CaveNetworkConstants;
import cavern.miner.network.MinerUpdateMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.network.PacketDistributor;

public class Miner implements INBTSerializable<CompoundNBT>
{
	private final PlayerEntity player;

	private int point;
	private MinerRank rank;

	private MinerCache cache;

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
		point = value;

		return this;
	}

	public MinerRank getRank()
	{
		if (rank == null)
		{
			rank = MinerRank.BEGINNER;
		}

		return rank;
	}

	public Miner setRank(MinerRank value)
	{
		rank = value;

		return this;
	}

	public Miner setRank(int value)
	{
		MinerRank[] ranks = MinerRank.values();

		return setRank(ranks[MathHelper.clamp(value, 0, ranks.length - 1)]);
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
			MinerRank next = getRank().next();

			if (getPoint() >= next.getPhase())
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
			CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new MinerUpdateMessage(this));
		}

		return this;
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();

		nbt.putInt("Point", getPoint());
		nbt.putInt("Rank", getRank().ordinal());

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		setPoint(nbt.getInt("Point"));
		setRank(nbt.getInt("Rank"));
	}

	public MinerCache getMiningCache()
	{
		if (cache == null)
		{
			cache = new MinerCache();
		}

		return cache;
	}
}