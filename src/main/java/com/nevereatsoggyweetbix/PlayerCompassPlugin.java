package com.nevereatsoggyweetbix;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.api.events.WorldViewUnloaded;
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
		clientThread.invoke(overlay::start);
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

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		overlay.npcOcclusion().spawn(event.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		overlay.npcOcclusion().despawn(event.getNpc());
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned event)
	{
		overlay.playerOcclusion().spawn(event.getPlayer());
	}

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned event)
	{
		overlay.playerOcclusion().despawn(event.getPlayer());
	}

	@Subscribe
	public void onWorldViewLoaded(WorldViewLoaded event)
	{
		overlay.npcOcclusion().worldLoaded(event.getWorldView());
		overlay.playerOcclusion().worldLoaded(event.getWorldView());
	}

	@Subscribe
	public void onWorldViewUnloaded(WorldViewUnloaded event)
	{
		overlay.npcOcclusion().worldUnloaded(event.getWorldView());
		overlay.playerOcclusion().worldUnloaded(event.getWorldView());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			overlay.npcOcclusion().refresh();
			overlay.playerOcclusion().refresh();
		}
		else if (event.getGameState() == GameState.LOADING || event.getGameState() == GameState.HOPPING
			|| event.getGameState() == GameState.LOGIN_SCREEN)
		{
			overlay.npcOcclusion().reset();
			overlay.playerOcclusion().reset();
		}
	}
}
