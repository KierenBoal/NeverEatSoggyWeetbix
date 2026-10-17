package com.nevereatsoggyweetbix;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldArea;

/** Other-player masking has its own budget and never includes the local player. */
final class PlayerOcclusion extends ActorOcclusion<Player>
{
	PlayerOcclusion(Client client)
	{
		super(client);
	}

	@Override
	protected Iterable<? extends Player> actors(WorldView worldView)
	{
		return worldView.players();
	}

	@Override
	protected int index(Player player)
	{
		return player.getId();
	}

	@Override
	protected int footprintTiles(Player player)
	{
		WorldArea area = player.getWorldArea();
		return area == null ? 0 : Math.max(1, Math.max(area.getWidth(), area.getHeight()));
	}

	@Override
	protected Player excludedActor()
	{
		return client.getLocalPlayer();
	}

	void erase(Graphics2D graphics, Rectangle bounds, PlayerCompassConfig config, ActorCompassMask mask)
	{
		super.erase(graphics, bounds, config.drawAbovePlayers(), config.maxPlayers(), mask);
	}
}
