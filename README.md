# Player Compass
Future badge link: (https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/PlayerCompass)
Future badge link: (https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/PlayerCompass)

## Which way is north again lol?

Tired of saying dumb shit like "Never Eat Soggy Weet-Bix" to calculate which direction you are calling? Player Compass solves this by drawing a compass directly on the ground.

![PlayerCompass](md_images/hero.jpg)

## Standout Config Options

- **Anchor mode**, to choose where the compass sits, like feet, head, screen center, mouse, true tile, etc
- **Ring size in tiles**, how far to draw the compass away from the render location; defaults to 1.0 tile
- **Highlight calculation**, highlight directions using your camera heading, your player's in-game direction, fixed north, or **From player to mouse**. The mouse option uses a flat plane at your player's feet, ignoring raised scenery and compass placement/height offsets. Outside the viewport, while a menu is open, at your feet, or without a valid ground intersection, it falls back to camera heading. Only colors change; geographic labels stay in place.
- **Cardinal and diagonal labels**, can be hidden independently, optional custom colors, completely customizable  
- **Directional indicator**, an optional shape that smoothly follows the exact highlight heading just outside the outer ring, including the camera fallback for invalid mouse targets. Enabled by default with Arrow selected, plus Triangle, Square, and Circle options, separate white fill and outline colors (with alpha), and a 1 px outline. Width defaults to 32 px across the heading and height to 32 px outward. Dimensions use the anchor's horizontal pixel scale before projection: the shape lies flat with the compass and foreshortens as the camera tilts. Unequal dimensions stretch Square and Circle into a rectangle or ellipse. Overall opacity and actor masking apply, and it remains visible when rings are hidden.
- **Connectors**, an extra way to help you visualize the directions by connecting a line between the center and the letters. Black drop shadows are enabled by default with a 1 px offset; toggle, offset, and color controls are in Connectors.
- **Draw above player/NPCs**, optional to disable and draw below them, this can make it harder to see
- **Direction colors**, bright sky blue (`#56B4E9`) for current directions and orange (`#E69F00`) for opposite directions, with white adjacent directions. The blue/orange pair comes from the [Okabe–Ito Color Universal Design palette](https://jfly.uni-koeln.de/color/index.html).
- **Overall opacity**, globally make the whole thing less or more see through; defaults to 100% for clearer colors and shadows. Saved custom settings are preserved; reset individual settings in RuneLite to use the new defaults.

## Requirements

- Java 11
- A RuneLite development client

## Running Locally

From the plugin root, start the development client with:

```powershell
.\gradlew.bat run
```

On Windows, the helper script builds the plugin, runs the tests, and then starts the development client:

```powershell
.\run.bat
```

If you use a Jagex Account, follow RuneLite's [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) guide to log in to the development client.

## Building

```powershell
.\gradlew.bat build
```

## Notes

- This is still a local development plugin. It has not been added to the Plugin Hub yet.
- Feel free to fork it and make changes. If you do, credit would be appreciated.

## Creator: Kieren Boal

I can't remember the name of directions to save my life, so when I am running any content that requires me to call out a direction, I always end up needing to say "Never Eat Soggy Weet-Bix", so like if it's east I only have to say "Never Eat", but other times I have to say the whole damn thing and my mates give me shit about it so this was the fix lmao 


### Usage of OpenAIs Codex:

- **GPT-6.1 Sol:** Wrote the initial implementation and follow-up code changes
- **GPT-6.0 Luna:** Grunt work, workhorse
