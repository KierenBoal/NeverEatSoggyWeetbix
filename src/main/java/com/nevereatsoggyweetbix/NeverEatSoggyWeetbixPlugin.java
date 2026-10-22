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
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "World compass",
	tags = {"world", "compass", "directions", "never", "eat", "soggy", "wheebix", "weetbix", "nesw"},
	internalName = "world-compass"
)
public class NeverEatSoggyWeetbixPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NeverEatSoggyWeetbixOverlay overlay;

	@Inject
	private ClientThread clientThread;

	@Inject
	private KeyManager keyManager;

	@Inject
	private NeverEatSoggyWeetbixConfig config;

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.showHideHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			KeybindMode mode = config.keybindMode();
			if (mode == KeybindMode.TOGGLE)
			{
				overlay.toggleVisibility();
			}
			else
			{
				overlay.setVisible(mode == KeybindMode.HOLD);
			}
		}

		@Override
		public void hotkeyReleased()
		{
			KeybindMode mode = config.keybindMode();
			if (mode != KeybindMode.TOGGLE)
			{
				overlay.setVisible(mode == KeybindMode.HOLD_INVISIBLE);
			}
		}
	};

	@Provides
	NeverEatSoggyWeetbixConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NeverEatSoggyWeetbixConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		resetHotkey();
		overlay.invalidateFonts();
		clientThread.invoke(overlay::start);
		overlayManager.add(overlay);
		keyManager.registerKeyListener(hotkeyListener);
		log.debug("NeverEatSoggyWeetbix started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(hotkeyListener);
		hotkeyListener.focusLost();
		overlayManager.remove(overlay);
		clientThread.invoke(overlay::clear);
		log.debug("NeverEatSoggyWeetbix stopped");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (NeverEatSoggyWeetbixConfig.GROUP.equals(event.getGroup()))
		{
			if ("showHideHotkey".equals(event.getKey()) || "keybindMode".equals(event.getKey()))
			{
				resetHotkey();
			}
			overlay.invalidateFonts();
		}
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		resetHotkey();
		overlay.invalidateFonts();
	}

	private void resetHotkey()
	{
		hotkeyListener.focusLost();
		overlay.setVisible(config.keybindMode() != KeybindMode.HOLD);
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
