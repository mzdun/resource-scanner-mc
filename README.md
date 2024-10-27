# Resource Scanner

<!-- modrinth_exclude.start -->
![Version](https://img.shields.io/github/v/tag/mzdun/resource-scanner-mc?label=version&style=flat-square) <!-- modrinth_exclude.end -->
[![GitHub License](https://img.shields.io/github/license/mzdun/resource-scanner-mc?style=flat-square)](https://raw.githubusercontent.com/mzdun/resource-scanner-mc/refs/heads/main/LICENSE)
[![Mod loader: Fabric]][fabric]
![Environment: Client](https://img.shields.io/badge/client-envirnment?style=flat-square&label=environment) <!-- modrinth_exclude.start -->
![Java 21](https://img.shields.io/badge/language-Java%2021-9115ff.svg?style=flat-square) <!-- modrinth_exclude.end -->

Resource locator mod for Fabric.

## What is this mod?

It helps players to find resources more easily. At start, it only looks for coal, so the player can produce torches and fend-off mobs. At later stages, it can help locate resources near the player that are needed for advancing the session.

**WARNING** - mod is alpha quality, not tested with large number of mods, performance impact not known. Please, follow bug tracker to ask questions and report issues.

## Features

- scans resources in range of 32 blocks in the direction player is looking.
- can look for a specific resource or a list of resources
- presents the echoes of found resources
- supports configuration via JSON editing or Mod Menu plugin

## Usage

![Press Mouse Button 5](docs/img/mouse_buttons.png)

|Mouse button|Action|
|---|---|
|Button 4|Open Resource Scanner settings screen|
|Button 5|Scan nearby resources|

Pressing the scanner button will reveal the resouces, if any, in player's vicinity. It starts easy, with coal and deepslate coal ores, but later can be set up to look for anything you want (as long as you want to locate an ore). Just press **mouse button 5** and enjoy.

The configuration is done through `config/resource-scanner.json` file. It will be created after first run. Alternatively, if you have a Mod Menu plugin installed, you can go into its screen from Minecraft menu and change the options there. Since version 0.5.2, you can also use **mouse button 4** to get to the settings, even without Mod Menu plugin.

You can tweak the distance the scanner will try to reach, as well as width of the scaner cone and who long echoes are kept on screen. Finally, you can select the kinds of ore the scanner should look for.

![Minimum values are: 4 meters for distance, single block for width and 10 visible echoes](docs/img/scanner_options_min.png) ![Maximum values are: 64 meters for distance, 21 by 21 meters for width and 200 visible echoes](docs/img/scanner_options_max.png)

## Screenshots

### Scanner in action

Running a scan will unveil chosen resources in player's vicinity. For instance, looking for iron might look something like this:

![Scanner about to be used](docs/img/scanner_before.png)

![Scanner activated](docs/img/scanner_after.png)

At the same time, putting a wider net may reveal much more iron, coal, dimonds and maybe some gold or redstone.

![Scanner activated](docs/img/scanner_wide_scan.png)

### Settings GUI

While it is possible to modify the scanner reach in the main menu you can, a runnig session is needed to to also select kinds of ore to scan.

![Main menu of scanner options](docs/img/scanner_options_main_menu.png)

![Main menu of scanner options, in game](docs/img/scanner_options_game_menu.png)

This inventory can contain up to 18 ore kinds. It is possible to add more directly through the JSON file, but as shown on the screenshot, there no much more ores to choose from.

![Ore kind selection menu](docs/img/scanner_options_ore_selection.png)

[fabric]: https://fabricmc.net
[Mod loader: Fabric]: https://img.shields.io/badge/modloader-Fabric-dbd0b4?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAFHGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDIgNzkuMTYwOTI0LCAyMDE3LzA3LzEzLTAxOjA2OjM5ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ0MgMjAxOCAoV2luZG93cykiIHhtcDpDcmVhdGVEYXRlPSIyMDE4LTEyLTE2VDE2OjU0OjE3LTA4OjAwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAxOS0wNy0yOFQyMToxNzo0OC0wNzowMCIgeG1wOk1ldGFkYXRhRGF0ZT0iMjAxOS0wNy0yOFQyMToxNzo0OC0wNzowMCIgZGM6Zm9ybWF0PSJpbWFnZS9wbmciIHBob3Rvc2hvcDpDb2xvck1vZGU9IjMiIHBob3Rvc2hvcDpJQ0NQcm9maWxlPSJzUkdCIElFQzYxOTY2LTIuMSIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDowZWRiMWMyYy1mZjhjLWU0NDEtOTMxZi00OTVkNGYxNGM3NjAiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MGVkYjFjMmMtZmY4Yy1lNDQxLTkzMWYtNDk1ZDRmMTRjNzYwIiB4bXBNTTpPcmlnaW5hbERvY3VtZW50SUQ9InhtcC5kaWQ6MGVkYjFjMmMtZmY4Yy1lNDQxLTkzMWYtNDk1ZDRmMTRjNzYwIj4gPHhtcE1NOkhpc3Rvcnk+IDxyZGY6U2VxPiA8cmRmOmxpIHN0RXZ0OmFjdGlvbj0iY3JlYXRlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDowZWRiMWMyYy1mZjhjLWU0NDEtOTMxZi00OTVkNGYxNGM3NjAiIHN0RXZ0OndoZW49IjIwMTgtMTItMTZUMTY6NTQ6MTctMDg6MDAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIFBob3Rvc2hvcCBDQyAyMDE4IChXaW5kb3dzKSIvPiA8L3JkZjpTZXE+IDwveG1wTU06SGlzdG9yeT4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4/HiGMAAAAtUlEQVRYw+XXrQqAMBQF4D2P2eBL+QIG8RnEJFaNBjEum+0+zMQLtwwv+wV3ZzhhMDgfJ0wUSinxZUQWgKos1JP/AbD4OneIDyQPwCFniA+EJ4CaXm4TxAXCC0BNHgLhAdAnx9hC8PwGSRtAFVMQjF7cNTWED8B1cgwW20yfJgAvrssAsZ1cB3g/xckAxr6FmCDU5N6f488BrpCQ4rQBJkiMYh4ACmLzwOQF0CExinkCsvw7vgGikl+OotaKRwAAAABJRU5ErkJggg==
