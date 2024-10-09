# Roadmap

## Next steps

- [x] only one setting at a time is saved (bug: #20, pr: #24)
- [x] `NullPointerException` when a scanner is active and world disappears (bug: #21, pr: #25)
- [ ] clumps of blocks (bug for internal walls: #22, issue: #23, pr: ?)
  - [ ] bounds for frustum
  - [x] walls no longer flicker
  - [x] focused clump has an outline (at minimum, `DEBUG_LINES`) &mdash; ...
  - [ ] ... so we have external vertices for camera distance calculation

## Bugs

- [x] Settings
  - [x] only one setting at a time is saved

- [x] Scanner
  - [x] internal walls tend to flicker in and out of existence
  - [x] closing app / disconnecting from server ends with a `NullPointerException` if the scanner is active

## Features

- [ ] Settings
  - [ ] per-world saves on top of common defaults
    - [ ] _end of minimal viable code_
- [ ] Scanner
  - [ ] clumps of blocks
  - [ ] label the clumps
    - [ ] _end of minimal viable code_
  - [ ] custom shader
  - [ ] better sorting for triangles
  - [ ] remove moiré effect from shimmers
  - [ ] consider adding alpha modifier to far-away blocks
    - [ ] _out of beta-quality_
  - [ ] consider removing directionality from sonar
  - [ ] game-play friendly nerfing approaches (iron and coal for free, other things &mdash; how?)
- [ ] Scanned items
  - [ ] _end of minimal viable code_
  - [ ] Investigate other blocks outside of `c:ores`
  - [ ] Investigate possibility of mob scanning
  - [ ] Investigate possibility of locating bigger structures (e.g. strongholds)
- [ ] InventoryWidget
  - [ ] picking up item stack from upper slots should leave the stack in the inventory
  - [ ] shift-click should put item in / remove from the lower part
  - [ ] dropping item stack _anywhere_ should merge with preexisting stack (count still 1)
    - [ ] _end of minimal viable code_
  - [ ] _nice to have_: dropping item to on-hand inventory should re-sort that inventory
