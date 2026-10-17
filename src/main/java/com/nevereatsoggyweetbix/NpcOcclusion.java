package com.nevereatsoggyweetbix;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
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

	@Override
	protected int footprintTiles(NPC npc)
	{
		NPCComposition composition = npc.getTransformedComposition();
		return composition == null ? 0 : Math.max(1, composition.getSize());
	}

	void erase(Graphics2D graphics, Rectangle bounds, PlayerCompassConfig config, ActorCompassMask mask)
	{
		super.erase(graphics, bounds, config.drawAboveNpcs(), config.maxNpcs(), mask);
	}
}
