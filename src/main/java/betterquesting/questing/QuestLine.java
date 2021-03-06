package betterquesting.questing;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.SimpleDatabase;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.PropertyContainer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class QuestLine extends SimpleDatabase<IQuestLineEntry> implements IQuestLine
{
	private IPropertyContainer info = new PropertyContainer();
	
	private IQuestLineDatabase parentDB;
	
	public QuestLine()
	{
		parentDB = QuestingAPI.getAPI(ApiReference.LINE_DB);
		
		setupProps();
	}
	
	private void setupProps()
	{
		this.setupValue(NativeProps.NAME, "New Quest Line");
		this.setupValue(NativeProps.DESC, "No Description");
		this.setupValue(NativeProps.ICON, new BigItemStack(Items.BOOK));
		this.setupValue(NativeProps.VISIBILITY, EnumQuestVisibility.NORMAL);
		this.setupValue(NativeProps.BG_IMAGE);
		this.setupValue(NativeProps.BG_SIZE);
	}
	
	private <T> void setupValue(IPropertyType<T> prop)
	{
		this.setupValue(prop, prop.getDefault());
	}
	
	private <T> void setupValue(IPropertyType<T> prop, T def)
	{
		info.setProperty(prop, info.getProperty(prop, def));
	}
	
	@Override
    public IQuestLineEntry createNew(int id)
    {
        IQuestLineEntry qle = new QuestLineEntry(0, 0, 24, 24);
        this.add(id, qle);
        return qle;
    }
	
	@Override
	public void setParentDatabase(IQuestLineDatabase lineDB)
	{
		this.parentDB = lineDB;
	}
	
	@Override
	public String getUnlocalisedName()
	{
		String def = "New Quest Line";
		
		if(!info.hasProperty(NativeProps.NAME))
		{
			info.setProperty(NativeProps.NAME, def);
			return def;
		}
		
		return info.getProperty(NativeProps.NAME, def);
	}
	
	@Override
	public String getUnlocalisedDescription()
	{
		String def = "No Description";
		
		if(!info.hasProperty(NativeProps.DESC))
		{
			info.setProperty(NativeProps.DESC, def);
			return def;
		}
		
		return info.getProperty(NativeProps.DESC, def);
	}
	
	@Override
	public DBEntry<IQuestLineEntry> getEntryAt(int x, int y)
	{
		for(DBEntry<IQuestLineEntry> entry : getEntries())
		{
			int i1 = entry.getValue().getPosX();
			int j1 = entry.getValue().getPosY();
			int i2 = i1 + entry.getValue().getSizeX();
			int j2 = j1 + entry.getValue().getSizeY();
			
			if(x >= i1 && x < i2 && y >= j1 && y < j2)
			{
				return entry;
			}
		}
		
		return null;
	}
	
	@Override
    @Deprecated
	public QuestingPacket getSyncPacket()
	{
		return getSyncPacket(null);
	}
	
	@Override
	public QuestingPacket getSyncPacket(@Nullable List<UUID> users)
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("line", writeToNBT(new NBTTagCompound(), users));
		tags.setTag("data", base);
		tags.setInteger("lineID", parentDB.getID(this));
		
		return new QuestingPacket(PacketTypeNative.LINE_SYNC.GetLocation(), tags);
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
		readFromNBT(payload.getCompoundTag("data").getCompoundTag("line"), false);
	}
	
	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        return writeToNBT(nbt, null);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        readFromNBT(nbt, false);
    }
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, List<UUID> users)
	{
		json.setTag("properties", info.writeToNBT(new NBTTagCompound()));
		
		NBTTagList jArr = new NBTTagList();
		
		for(DBEntry<IQuestLineEntry> entry : getEntries())
		{
			NBTTagCompound qle = entry.getValue().writeToNBT(new NBTTagCompound());
			qle.setInteger("id", entry.getID());
			jArr.appendTag(qle);
		}
		
		json.setTag("quests", jArr);
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, boolean merge)
	{
		info.readFromNBT(json.getCompoundTag("properties"));
		
		reset();
		
		NBTTagList qList = json.getTagList("quests", 10);
		for(int i = 0; i < qList.tagCount(); i++)
		{
			NBTTagCompound qTag = qList.getCompoundTagAt(i);
			
			int id = qTag.hasKey("id", 99) ? qTag.getInteger("id") : -1;
			if(id< 0) continue;
			
			add(id, new QuestLineEntry(qTag));
		}
		
		this.setupProps();
	}
    
    @Override
    public <T> T getProperty(IPropertyType<T> prop)
    {
        return info.getProperty(prop);
    }
    
    @Override
    public <T> T getProperty(IPropertyType<T> prop, T def)
    {
        return info.getProperty(prop, def);
    }
    
    @Override
    public boolean hasProperty(IPropertyType<?> prop)
    {
        return info.hasProperty(prop);
    }
    
    @Override
    public <T> void setProperty(IPropertyType<T> prop, T value)
    {
        info.setProperty(prop, value);
    }
    
    @Override
    public void removeProperty(IPropertyType<?> prop)
    {
        info.removeProperty(prop);
    }
    
    @Override
    public void removeAllProps()
    {
        info.removeAllProps();
    }
}
