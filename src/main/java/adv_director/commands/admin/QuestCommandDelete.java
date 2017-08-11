package adv_director.commands.admin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import adv_director.api.questing.IQuest;
import adv_director.commands.QuestCommandBase;
import adv_director.network.PacketSender;
import adv_director.questing.QuestDatabase;
import adv_director.questing.QuestLineDatabase;

public class QuestCommandDelete extends QuestCommandBase
{
	@Override
	public String getUsageSuffix()
	{
		return "[all|<quest_id>]";
	}
	
	@Override
	public boolean validArgs(String[] args)
	{
		return args.length == 2;
	}
	
	@Override
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			list.add("all");
			
			for(int i : QuestDatabase.INSTANCE.getAllKeys())
			{
				list.add("" + i);
			}
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "delete";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		if(args[1].equalsIgnoreCase("all"))
		{
			QuestDatabase.INSTANCE.reset();
			QuestLineDatabase.INSTANCE.reset();
			PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
			PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
		    
			sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.delete.all"));
		} else
		{
			try
			{
				int id = Integer.parseInt(args[1].trim());
				IQuest quest = QuestDatabase.INSTANCE.getValue(id);
				QuestDatabase.INSTANCE.removeKey(id);
				PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
				
				sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.delete.single", new TextComponentTranslation(quest.getUnlocalisedName())));
			} catch(Exception e)
			{
				throw getException(command);
			}
		}
	}
	
}