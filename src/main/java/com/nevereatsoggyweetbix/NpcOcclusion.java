package com.nevereatsoggyweetbix;

import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.WorldView;

final class NpcOcclusion extends ActorOcclusion<NPC>
{
	NpcOcclusion(Client client)
	{
		super(client);
	}

	@Override
	protected Iterable<? extends NPC> actors(WorldView worldView)
	{
		return worldView.npcs();
	}

	@Override
	protected int index(NPC npc)
	{
		return npc.getIndex();
	}

	int select(Rectangle bounds, NeverEatSoggyWeetbixConfig config)
	{
		return super.select(bounds, config.drawAboveNpcs(), config.maxNpcs());
	}
}
