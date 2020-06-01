package cavern.miner.data;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import cavern.miner.api.data.IMinerAccess;
import cavern.miner.api.event.MinerEvent;
import cavern.miner.capability.CaveCapabilities;
import cavern.miner.core.CaveSounds;
import cavern.miner.network.CaveNetworkRegistry;
import cavern.miner.network.client.MinerMessage;
import cavern.miner.util.BlockMeta;
import cavern.miner.util.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;

public class Miner implements IMinerAccess
{
	private final EntityPlayer entityPlayer;

	private int point;
	private int rank;

	private final Map<BlockMeta, Integer> records = Maps.newHashMap();

	public Miner()
	{
		this(null);
	}

	public Miner(@Nullable EntityPlayer player)
	{
		this.entityPlayer = player;
	}

	@Override
	public int getPoint()
	{
		return point;
	}

	@Override
	public void setPoint(int value)
	{
		setPoint(value, true);
	}

	public void setPoint(int value, boolean send)
	{
		point = Math.max(value, 0);

		if (send) sendToClient();
	}

	@Override
	public void addPoint(int value)
	{
		addPoint(value, true);
	}

	public void addPoint(int value, boolean send)
	{
		MinerEvent.AddPoint event = new MinerEvent.AddPoint(entityPlayer, this, value);

		if (MinecraftForge.EVENT_BUS.post(event))
		{
			return;
		}

		setPoint(point + event.getNewPoint(), false);

		if (entityPlayer != null && value > 0 && point > 0 && point % 100 == 0)
		{
			entityPlayer.addExperience(entityPlayer.xpBarCap() / 2);
		}

		MinerRank current = MinerRank.get(rank);
		int max = MinerRank.values().length - 1;
		boolean promoted = false;

		while (current.getRank() < max)
		{
			MinerRank next = MinerRank.get(rank + 1);

			if (point >= next.getPhase())
			{
				++rank;

				promoted = true;
				current = next;

				setPoint(point - current.getPhase(), false);
			}
			else break;
		}

		if (promoted)
		{
			if (entityPlayer != null && entityPlayer instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP)entityPlayer;
				MinecraftServer server = player.mcServer;

				ITextComponent name = new TextComponentTranslation(current.getUnlocalizedName());
				name.getStyle().setBold(true);

				ITextComponent component = new TextComponentTranslation("cavern.minerrank.promoted", player.getDisplayName(), name);
				component.getStyle().setColor(TextFormatting.GRAY).setItalic(true);

				server.getPlayerList().sendMessage(component);

				double x = player.posX;
				double y = player.posY + player.getEyeHeight();
				double z = player.posZ;

				player.getServerWorld().playSound(null, x, y, z, CaveSounds.RANKUP, SoundCategory.AMBIENT, 0.5F, 1.0F);

				switch (current)
				{
					case IRON_MINER:
						PlayerUtils.grantAdvancement(player, "iron_miner");
						break;
					case GOLD_MINER:
						PlayerUtils.grantAdvancement(player, "gold_miner");
						break;
					case HEXCITE_MINER:
						PlayerUtils.grantAdvancement(player, "hexcite_miner");
						break;
					case DIAMOND_MINER:
						PlayerUtils.grantAdvancement(player, "diamond_miner");
						break;
					default:
				}
			}

			MinecraftForge.EVENT_BUS.post(new MinerEvent.PromoteRank(entityPlayer, this));
		}

		if (send) sendToClient();
	}

	@Override
	public int getRank()
	{
		return rank;
	}

	@Override
	public void setRank(int value)
	{
		setRank(value, true);
	}

	public void setRank(int value, boolean adjust)
	{
		int prev = rank;

		rank = MinerRank.get(value).getRank();

		if (rank != prev && adjust)
		{
			sendToClient();
		}
	}

	public void sendToClient()
	{
		CaveNetworkRegistry.sendTo(() -> new MinerMessage(this), entityPlayer);
	}

	public void setMiningRecord(BlockMeta blockMeta, int count)
	{
		records.put(blockMeta, count);
	}

	public void addMiningRecord(BlockMeta blockMeta)
	{
		 int count = records.getOrDefault(blockMeta, 0);

		 setMiningRecord(blockMeta, ++count);
	}

	@Override
	public ImmutableMap<BlockMeta, Integer> getMiningRecords()
	{
		return ImmutableMap.copyOf(records);
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("Point", getPoint());
		nbt.setInteger("Rank", getRank());

		NBTTagList list = new NBTTagList();

		for (Entry<BlockMeta, Integer> record : records.entrySet())
		{
			NBTTagCompound tag = new NBTTagCompound();

			tag.setString("Name", record.getKey().getRegistryName().toString());
			tag.setInteger("Meta", record.getKey().getMeta());
			tag.setInteger("Count", record.getValue());

			list.appendTag(tag);
		}

		nbt.setTag("Records", list);
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		setPoint(nbt.getInteger("Point"), false);
		setRank(nbt.getInteger("Rank"), false);

		NBTTagList list = nbt.getTagList("Records", NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound tag = list.getCompoundTagAt(i);

			records.put(new BlockMeta(tag.getString("Name"), tag.getInteger("Meta")), tag.getInteger("Count"));
		}
	}

	public static Miner get(EntityPlayer player)
	{
		return get(player, false);
	}

	public static Miner get(EntityPlayer player, boolean nullable)
	{
		return ObjectUtils.defaultIfNull(CaveCapabilities.getCapability(player, CaveCapabilities.MINER), nullable ? null : new Miner(player));
	}
}