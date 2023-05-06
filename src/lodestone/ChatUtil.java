package lodestone;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ChatUtil {

	public static String PLUGIN_NAME 				= "LodestonePorter";
	public static final ChatColor COMMAND_COLOR 	= ChatColor.GREEN;
	public static final ChatColor ERROR_COLOR 		= ChatColor.RED;
	public static final ChatColor PLUGIN_COLOR 		= ChatColor.DARK_AQUA;
	public static final ChatColor SYMBOL_COLOR 		= ChatColor.DARK_GRAY;
	
	public static final String ERROR_NOT_ENOUGH_PERMISSIONS = "You do not have permission to perform this command!";
	
	public static void sendMessage(CommandSender p, String message) {
		p.sendMessage(getPluginPrefix() + COMMAND_COLOR + message);
	}
	
	public static void sendMessageWithTooltip(CommandSender p, String message, String tooltip) {
		TextComponent text = new TextComponent(getPluginPrefix() + COMMAND_COLOR + message);
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(tooltip)));
		p.spigot().sendMessage(text);
	}
	
	public static void sendMessage(String message) {
		Bukkit.broadcastMessage(getPluginPrefix() + COMMAND_COLOR + message);
	}
	
	public static void sendErrorMessage(CommandSender p, String message) {
		p.sendMessage(getPluginPrefix() + ERROR_COLOR + message);
	}
	
	public static void sendErrorMessage(String message) {
		Bukkit.broadcastMessage(getPluginPrefix() + ERROR_COLOR + message);
	}
	
	public static String getPluginPrefix() {
		return SYMBOL_COLOR + "[" + PLUGIN_COLOR + "CustomChatColors" + SYMBOL_COLOR + "] " + COMMAND_COLOR; 
	}
}