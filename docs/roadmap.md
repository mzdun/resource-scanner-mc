# Roadmap

## Next steps

- only one setting at a time is saved (bug: ?, pr: ?)
- `NullPointerException` what a scanner is active and world disappears (bug: ?, pr: ?)
- clumps of blocks (bug for internal walls: ?, issue: ?, pr: ?)
  - [ ] bounds for frustum
  - [ ] walls no longer flicker
  - [ ] focused clump has an outline (at minimum, `DEBUG_LINES`) &mdash; so we have external vertices for camera distance calculation

## Bugs

- [ ] Settings
  - [ ] only one setting at a time is saved

- [ ] Scanner
  - [ ] internal walls tend to flicker in and out of existence
  - [ ] closing app / disconnecting from server ends with a `NullPointerException` if the scanner is active

## Features

- [ ] Scanner
  - [ ] clumps of blocks
  - [ ] label the clumps
  - [ ] custom shader
  - [ ] remove moiré effect from shimmers
  - [ ] consider adding alpha modifier to far-away blocks
  - [ ] consider removing directionality from sonar
  - [ ] better sorting for triangles
  - [ ] game-play friendly nerfing approaches (iron and coal for free, other things &mdash; how?)
- [ ] Scanned items
  - [ ] Investigate other blocks outside of `c:ores`
  - [ ] Investigate possibility of mob scanning
  - [ ] Investigate possibility of locating bigger structures (e.g. strongholds)
- [ ] InventoryWidget
  - [ ] picking up item stack from upper slots should leave the stack in the inventory
  - [ ] shift-click should put item in / remove from the lower part
  - [ ] dropping item stack _anywhere_ should merge with preexisting stack (count still 1)
  - [ ] _nice to have_: dropping item to on-hand inventory should re-sort that inventory
