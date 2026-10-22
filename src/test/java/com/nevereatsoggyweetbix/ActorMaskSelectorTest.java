package com.nevereatsoggyweetbix;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActorMaskSelectorTest
{
	@Test
	public void localPlayerIsExcludedBeforeBoundsWorkAndDoesNotConsumeBudget()
	{
		ActorMaskSelector<Object> selector = new ActorMaskSelector<>();
		Object local = new Object(), other = new Object();
		selector.spawn(0, 0, local);
		selector.spawn(0, 1, other);
		List<Object> selected = new ArrayList<>();
		assertEquals(1, selector.mask(false, 1, new Rectangle(0, 0, 100, 100), local, actor ->
		{
			assertNotSame(local, actor);
			return new Rectangle(10, 10, 20, 20);
		}, selected::add));
		assertEquals(Arrays.asList(other), selected);
	}

	@Test
	public void playerAndNpcBudgetsAndSwitchesAreIndependent()
	{
		ActorMaskSelector<Integer> players = new ActorMaskSelector<>();
		ActorMaskSelector<Integer> npcs = new ActorMaskSelector<>();
		for (int i = 0; i < 20; i++)
		{
			players.spawn(0, i, i);
			npcs.spawn(0, i, i);
		}
		Rectangle bounds = new Rectangle(0, 0, 100, 100);
		AtomicInteger playerMasks = new AtomicInteger(), npcMasks = new AtomicInteger();
		assertEquals(12, players.mask(false, 12, bounds, actor -> null, actor -> playerMasks.incrementAndGet()));
		assertEquals(12, npcs.mask(false, 12, bounds, actor -> null, actor -> npcMasks.incrementAndGet()));
		assertEquals(12, playerMasks.get());
		assertEquals(12, npcMasks.get());
		assertEquals(0, players.mask(true, 12, bounds,
			actor -> { fail("Disabled player masking must skip bounds"); return null; },
			actor -> fail("Disabled player masking must skip models")));
		assertEquals(1, npcs.mask(false, 1, bounds, actor -> null, actor -> npcMasks.incrementAndGet()));
		assertEquals(0, players.mask(false, 0, bounds,
			actor -> { fail("Zero player budget must skip bounds"); return null; },
			actor -> fail("Zero player budget must skip models")));
	}

	@Test
	public void largestOverlapsWinAndOffscreenActorsNeverRequestModels()
	{
		ActorMaskSelector<Integer> selector = new ActorMaskSelector<>();
		for (int i = 1; i <= 4; i++)
		{
			selector.spawn(0, i, i);
		}
		List<Integer> selected = new ArrayList<>();
		int count = selector.mask(false, 2, new Rectangle(0, 0, 100, 100),
			actor -> actor == 4 ? new Rectangle(200, 200, 20, 20) : new Rectangle(0, 0, actor * 10, actor * 10),
			selected::add);
		assertEquals(2, count);
		assertEquals(Arrays.asList(3, 2), selected);
	}

	@Test
	public void registryHandlesDespawnIndexReuseWorldUnloadAndShutdown()
	{
		ActorMaskSelector<Object> selector = new ActorMaskSelector<>();
		Object oldActor = new Object(), replacement = new Object();
		selector.spawn(1, 3, oldActor);
		selector.spawn(1, 3, replacement);
		selector.despawn(1, 3, oldActor);
		assertEquals(1, selector.size());
		selector.spawn(2, 3, new Object());
		selector.removeWorld(1);
		assertEquals(1, selector.size());
		selector.spawn(1, 3, replacement);
		selector.despawn(1, 3, replacement);
		assertEquals(1, selector.size());
		selector.clear();
		assertEquals(0, selector.size());
		selector.mask(false, 12, new Rectangle(0, 0, 100, 100),
			actor -> { fail("Cleared actors must not be retained"); return new Rectangle2D.Double(); },
			actor -> fail("Cleared actors must not be masked"));
	}
}
