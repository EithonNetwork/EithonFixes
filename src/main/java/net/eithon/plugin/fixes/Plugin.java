package net.eithon.plugin.fixes;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.plugin.fixes.logic.CommandScheduler;
import net.eithon.plugin.fixes.logic.Controller;

import org.bukkit.event.Listener;

public final class Plugin extends EithonPlugin {
	private Controller _controller;

	@Override
	public void onEnable() {
		CommandScheduler.initialize();
		super.onEnable();
		Config.load(this);
		this._controller = new Controller(this);
		CommandHandler commandHandler = new CommandHandler(this, this._controller);
		Listener eventListener = new EventListener(this, this._controller);
		super.activate(commandHandler.getCommandSyntax(), eventListener);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		this._controller = null;
	}
}
