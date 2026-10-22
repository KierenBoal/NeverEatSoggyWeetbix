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
		assertEquals(1, selector.select(false, 1, new Rectangle(0, 0, 100, 100), local, actor ->
		{
			assertNotSame(local, actor);
			return new Rectangle(10, 10, 20, 20);
		}));
		assertTrue(selected.isEmpty());
		selector.maskSelected(selected::add);
		assertEquals(Arrays.asList(other), selected);
		selector.maskSelected(actor -> fail("Selection must be consumed only once"));
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
		assertEquals(12, players.select(false, 12, bounds, null, actor -> null));
		assertEquals(12, npcs.select(false, 12, bounds, null, actor -> null));
		players.maskSelected(actor -> playerMasks.incrementAndGet());
		npcs.maskSelected(actor -> npcMasks.incrementAndGet());
		assertEquals(12, playerMasks.get());
		assertEquals(12, npcMasks.get());
		assertEquals(0, players.select(true, 12, bounds, null,
			actor -> { fail("Disabled player masking must skip bounds"); return null; }));
		players.maskSelected(actor -> fail("Disabled player masking must skip models"));
		assertEquals(1, npcs.select(false, 1, bounds, null, actor -> null));
		npcs.maskSelected(actor -> npcMasks.incrementAndGet());
		assertEquals(13, npcMasks.get());
		assertEquals(0, players.select(false, 0, bounds, null,
			actor -> { fail("Zero player budget must skip bounds"); return null; }));
		players.maskSelected(actor -> fail("Zero player budget must skip models"));
	}

	@Test
	public void largestOverlapsWinAndOffscreenActorsNeverRequestModels()
	{
		ActorMaskSelector<Integer> selector = new ActorMaskSelector<>();
		for (int i = 1; i <= 100; i++)
		{
			selector.spawn(0, i, i);
		}
		List<Integer> selected = new ArrayList<>();
		Rectangle bounds = new Rectangle(0, 0, 100, 100);
		Rectangle2D.Double scratch = new Rectangle2D.Double();
		int count = selector.select(false, 2, bounds, null, actor ->
		{
			scratch.setRect(actor > 3 ? 200 : 0, 0, actor * 10, actor * 10);
			return scratch;
		});
		selector.maskSelected(selected::add);
		assertEquals(2, count);
		assertEquals(Arrays.asList(3, 2), selected);

		// Unknown bounds remain candidates; equal overlaps use world and actor index order.
		selector.spawn(-1, 3, 103);
		selector.spawn(-1, 2, 102);
		selected.clear();
		assertEquals(3, selector.select(false, 3, bounds, null, actor -> null));
		selector.maskSelected(selected::add);
		assertEquals(Arrays.asList(102, 103, 1), selected);

		selector.select(false, 2, bounds, null, actor -> null);
		assertEquals(0, selector.select(false, 2, bounds, null, actor -> new Rectangle(200, 200, 20, 20)));
		selector.maskSelected(actor -> fail("No overlaps must allow rendering without a mask"));
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
		assertEquals(0, selector.select(false, 12, new Rectangle(0, 0, 100, 100), null,
			actor -> { fail("Cleared actors must not be retained"); return null; }));
		selector.maskSelected(actor -> fail("Cleared actors must not be masked"));
	}
}
