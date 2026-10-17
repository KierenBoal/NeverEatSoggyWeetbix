package com.nevereatsoggyweetbix;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Player compass",
	internalName = "never_eat_soggy_weetbix"
)
public class PlayerCompassPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PlayerCompassOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Provides
	PlayerCompassConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayerCompassConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlay.invalidateFonts();
		overlayManager.add(overlay);
		log.debug("Player compass started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		clientThread.invoke(overlay::clear);
		log.debug("Player compass stopped");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (PlayerCompassConfig.GROUP.equals(event.getGroup()))
		{
			overlay.invalidateFonts();
		}
	}
}
