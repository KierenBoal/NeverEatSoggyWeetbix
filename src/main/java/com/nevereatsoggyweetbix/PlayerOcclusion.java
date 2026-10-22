package com.nevereatsoggyweetbix;

import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldView;

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
	protected Player excludedActor()
	{
		return client.getLocalPlayer();
	}

	int select(Rectangle bounds, NeverEatSoggyWeetbixConfig config)
	{
		return super.select(bounds, config.drawAbovePlayers(), config.maxPlayers());
	}
}
