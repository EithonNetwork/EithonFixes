package net.eithon.plugin.eithonfixes;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin {
	@Override
	public void onEnable() {
		EithonPlugin eithonPlugin = EithonPlugin.get(this);
		eithonPlugin.enable();
		Events.get().enable(eithonPlugin);
		Fixes.get().enable(eithonPlugin);
		Commands.get().enable(eithonPlugin);
		getServer().getPluginManager().registerEvents(Events.get(), this);		
	}

	@Override
	public void onDisable() {
		EithonPlugin eithonPlugin = EithonPlugin.get(this);
		eithonPlugin.disable();
		Fixes.get().disable();
		Commands.get().disable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return Commands.get().onCommand(sender, args);
	}
}
