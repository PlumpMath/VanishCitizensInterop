package au.com.mineauz.vci;

import java.lang.reflect.Field;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.event.SpeechEvent;
import net.citizensnpcs.api.ai.speech.event.SpeechTargetedEvent;
import net.citizensnpcs.api.trait.TraitInfo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.kitteh.vanish.staticaccess.VanishNoPacket;
import org.kitteh.vanish.staticaccess.VanishNotLoadedException;

public class InteropPlugin extends JavaPlugin implements Listener
{
	private Field mTalkableField;
	
	@Override
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);
		
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(LookCloseSafe.class).withName("lookclosesafe"));
		
		try
		{
			mTalkableField = SpeechEvent.class.getDeclaredField("target");
			mTalkableField.setAccessible(true);
		}
		catch ( NoSuchFieldException e )
		{
			e.printStackTrace();
			setEnabled(false);
		}
		catch ( SecurityException e )
		{
			e.printStackTrace();
			setEnabled(false);
		}
	}
	
	public static boolean isPlayerVanished(Player player)
	{
		try
		{
			return VanishNoPacket.isVanished(player.getName());
		}
		catch ( VanishNotLoadedException e )
		{
			return false;
		}
	}
	
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	private void onNCPTalk(SpeechTargetedEvent event)
	{
		try
		{
			Talkable talkable = (Talkable)mTalkableField.get(event);
			
			if(talkable.getEntity() instanceof Player)
			{
				Player player = (Player)talkable.getEntity();
				
				if(!CitizensAPI.getNPCRegistry().isNPC(player) && isPlayerVanished(player))
					event.setCancelled(true);
			}
		}
		catch ( Exception e )
		{
			throw new RuntimeException(e);
		}
	}
}
