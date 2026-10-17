# Player compass

A RuneLite compass projected onto a horizontal world plane. Its rings and spokes follow the camera's perspective while uppercase direction labels stay readable. It displays geographic directions only; it does not suggest movement, safe tiles, or combat actions.

## Placement

The default anchor follows the player's rendered feet. Other modes follow the animated model's top, the center of the server-position true tile, the middle of the game canvas, the continuous mouse position on the hovered tile's ground-height plane, or the center of the hovered tile. Mouse anchors hide without a valid hovered scene tile or while a menu is open. Continuous mouse mode uses a flat plane at the selected tile's center height, rather than following every bump on sloped terrain.

Render distance is the **inner ring radius in tiles**, default 2 (range 1–128). Both rings use a horizontal plane at the anchor's elevation. The height offset is in world units, from −512 to 512; positive values raise the plane. Rings do not bend over terrain. Middle of screen centers the compass on half the canvas width and height, preserving the perspective and scale of the player's anchor. All drawing is clipped to the scene viewport and sits beneath interface widgets.

## Labels, connectors and rings

- N/E/S/W use the RuneScape big/bold font at 32 px. NE/SE/SW/NW use the small font at 16 px. Independent **Hide cardinal letters** and **Hide diagonal letters** switches default off and affect letters only.
- **Always up** keeps glyphs upright. **Rotation** points the top of each label outward along its projected direction.
- **Use Custom font** defaults off. Enable it to use the custom font picker's installed family, main-letter size and style; diagonal labels remain half-size. The picker remains visible but is ignored while custom fonts are disabled.
- **Show cardinal connectors** and **Show diagonal connectors** both default on. Spokes run from 35% to 85% of the inner radius, leaving the middle clear. They remain independent of letter visibility.
- Connectors use direction colors by default; disable that setting to use Base color. Thickness defaults to 3 screen pixels, range 1–12.
- Inner and outer rings have independent visibility switches, both on, and independent thickness settings, both 3 px (range 1–12).
- The band between rings reserves room for the largest label, including rotated text. Padding adds extra screen pixels on each side of that band, default 4 (range 0–128). The outer boundary expands in screen space, so band spacing stays readable as the camera zooms. Hiding letters retains the same layout.

## Colors and opacity

The dedicated Colors section has separate current, opposite, left-adjacent and right-adjacent colors for each of the cardinal and diagonal groups. The nearest direction to the camera's horizontal viewing heading is current in each group independently; exact ties choose clockwise. Left means 90° counterclockwise, and right means 90° clockwise. Direction names and world positions remain geographic as color roles move with the camera.

| Color | Default |
|---|---|
| Current cardinal | Red, alpha 255 |
| Opposite cardinal | Green, alpha 255 |
| Left/right adjacent cardinals | White, alpha 255 |
| Current diagonal | Light red (255, 128, 128), alpha 128 |
| Opposite diagonal | Light green (128, 255, 128), alpha 128 |
| Left/right adjacent diagonals | White, alpha 128 |
| Base, inner ring, outer ring | White, alpha 255 |

Overall opacity defaults to 75%, range 0–100. It multiplies each color's alpha: alpha 128 at 50% becomes 64, while alpha 255 at 50% becomes 128.

**Draw above player** defaults off. The compass is masked out of your projected model faces so your player remains visible. Turn it on to allow overlap. Masking follows the model silhouette and excludes hidden/fully transparent faces; it is not full scene depth occlusion against other actors or scenery. When no model is available, drawing continues without a player mask.

## Development

- Use Java 11, as recommended by RuneLite's [Plugin Hub development guide](https://github.com/runelite/plugin-hub#creating-new-plugins).
- Open this repository in IntelliJ IDEA and run the Gradle `run` task, or run `./gradlew run` on macOS/Linux and `./gradlew.bat run` on Windows.
- If you use a Jagex account, follow RuneLite's [development login instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) for the development client.
- Run `./gradlew test` (Windows: `./gradlew.bat test`) for automated projection, geometry, color and raster-mask checks.

Only a human can validate the plugin in-game. A clean build or client startup is not a passing visual test. Test:

1. Walk and run with the feet anchor, then compare it with true-tile snapping. Test head anchoring with animations and different equipment.
2. Rotate, pitch and zoom the camera. Check geographic positions, camera-relative colors and clockwise transition boundaries, and both letter rotation modes.
3. Try screen-center and both mouse anchors. Move within one tile and between tiles, over sloped terrain, over interface panels and outside the viewport. Check hiding on invalid targets and while menus are open.
4. Toggle cardinal/diagonal letters and connectors independently, then both ring switches. Check thickness bounds, label padding, base-color connectors and custom fonts.
5. Try 0%, 50%, 75% and 100% opacity with partially transparent colors. Check player masking on/off, including arms, capes, walking and animations.
6. Exercise positive/negative height limits, radius 1 and 128, low camera pitches, scene edges, plane changes, instanced areas, moving world views where available, and GPU/software rendering. Check for stray ring connections, exceptions or stale drawing after disabling the plugin.

## Project layout

- `src/main/java/com/nevereatsoggyweetbix/PlayerCompassPlugin.java` is the plugin entry point.
- `src/test/java/com/nevereatsoggyweetbix/PlayerCompassPluginTest.java` starts RuneLite with the local plugin loaded.
- `runelite-plugin.properties` contains the Plugin Hub listing metadata.

The plugin display name is **Player compass**, its internal name remains `never_eat_soggy_weetbix`, and its config group is `player-compass`. The Gradle project is named `NeverEatSoggyWeetbix`. This adds no network requests, file access, input automation, or game actions.
