package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import java.util.Collections;
import java.util.List;

public class ToolboxToolNew implements IToolboxTool
{
	private CanvasQuestLine gui = null;
	private PanelButtonQuest nQuest;
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
		
		nQuest = new PanelButtonQuest(new GuiRectangle(0, 0, 24, 24), -1, "", null);
	}
	
	@Override
    public void refresh(CanvasQuestLine gui)
    {
    }
	
	@Override
	public void drawCanvas(int mx, int my, float partialTick)
	{
		if(nQuest == null)
		{
			return;
		}
		
		int snap = ToolboxTabMain.INSTANCE.getSnapValue();
		int modX = ((mx%snap) + snap)%snap;
		int modY = ((my%snap) + snap)%snap;
		mx -= modX;
		my -= modY;
		
		nQuest.rect.x = mx;
		nQuest.rect.y = my;
		nQuest.drawPanel(mx, my, partialTick); // TODO: Draw relative
	}
	
	@Override
    public void drawOverlay(int mx, int my, float partialTick)
    {
        ToolboxTabMain.INSTANCE.drawGrid(gui);
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return Collections.emptyList();
    }
	
	@Override
	public void disableTool()
	{
		if(nQuest != null) nQuest = null;
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || !gui.getTransform().contains(mx, my))
		{
			return false;
		}
		
		// Pre-sync
		IQuestLine qLine = gui.getQuestLine();
		int qID = QuestDatabase.INSTANCE.nextID();
		int lID = QuestLineDatabase.INSTANCE.getID(qLine);
		IQuestLineEntry qe = qLine.getValue(qID);//new QuestLineEntry(mx, my, 24);
		
		
		if(qe == null)
		{
			qe = new QuestLineEntry(nQuest.rect.x, nQuest.rect.y, 24, 24);
			qLine.add(qID, qe);
		} else
		{
			qe.setPosition(nQuest.rect.x, nQuest.rect.y);
			qe.setSize(24, 24);
		}
		
		// Sync Quest
		NBTTagCompound tag1 = new NBTTagCompound();
		tag1.setInteger("action", EnumPacketAction.ADD.ordinal());
		tag1.setInteger("questID", qID);
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag1));
		
		// Sync Line
		NBTTagCompound tag2 = new NBTTagCompound();
		NBTTagCompound base2 = new NBTTagCompound();
		base2.setTag("line", qLine.writeToNBT(new NBTTagCompound(), null));
		tag2.setTag("data", base2);
		tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
		tag2.setInteger("lineID", lID);
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tag2));
		
		return true;
	}
	
	@Override
    public boolean onMouseRelease(int mx, int my, int click)
    {
        return false;
    }
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
	    return false;
	}
	
	@Override
	public boolean onKeyPressed(char c, int keyCode)
	{
	    return false;
	}
	
	@Override
	public boolean clampScrolling()
	{
		return false;
	}
	
	@Override
    public void onSelection(NonNullList<PanelButtonQuest> buttons)
    {
    }
	
	@Override
    public boolean useSelection()
    {
        return false;
    }
}
