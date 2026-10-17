# Player Compass
Future badge link: (https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/NeverEatSoggyWeetbix)
Future badge link: (https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/NeverEatSoggyWeetbix)

## Which way is north again lol?

Tired of saying dumb shit like "Never Eat Soggy Weet-Bix" to remember which direction you need to call out for  is all well and good until someone says they're heading southeast. PlayerCompass draws the directions right there on the ground.

![PlayerCompass](md_images/hero.png)

## About

Player Compass draws a compass on the floor around your player which follow the camera's perspective.
There are many config options that you can use to tailor it to fit your desired style.

## Standout Config Options

- **Anchor mode**, to choose where the compass sits, like feet, head, screen center, mouse, true tile, etc
- **Highlight calculation**, Highlight two of the directions depending on which way your camera is facing, your players in-game direction, or just fixed in place
- **Cardinal and diagonal labels**, can be hidden independently, optional custom colors, completely customizable  
- **Connectors**, an extra way to help you visualize the directions by connecting a line between the center and the letters
- **Draw above player**, to allow the compass to overlap your character instead of masking it out if you prefer that look 
- **Overall opacity**, globally make the whole thing less or more see through

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

- This is still a local development plugin. It has not been added to the Plugin Hub yet; the badge above is pointed at the planned PlayerCompass page.
- GPT-6.1 Sol and GPT-6 Luna helped with the code, debugging, and this README. I made the final calls on how it should work.
- Feel free to fork it and make changes. If you do, credit would be appreciated.

## Creator: Kieren Boal

I can not remember the cardinal directions to save my life, so when I am running any content that requires me to call out a direction, I always end up needing to say "Never Eat Soggy Weet-Bix", so liek if its east I only have to say "Never Eat", but other times I have to say the whole damn thing and my mates give me shit about it so this was the fix lmao 


### Usage of OpenAIs Codex:

- **GPT-6.1 Sol:** Wrote the initial implementation and follow-up code changes
- **GPT-6.0 Luna:** Grunt work, workhorse
