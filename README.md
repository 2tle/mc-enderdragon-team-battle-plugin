# EnderTeamBattle

EnderTeamBattle is a Paper plugin for running team-based Ender Dragon battles. The first team to defeat the Ender Dragon wins.

Current release: **1.3**

## Features

- Create color-coded teams and assign members even before their first server connection.
- Give each team a safe default spawn, arranged evenly around the world-border center about 550 blocks apart.
- Show team colors and membership through the server scoreboard.
- Start, pause, resume, or stop a battle.
- Freeze participating team members while a battle is paused.
- Persist teams and game state in the plugin configuration.
- Announce game events and the winning team to the server.
- Announce to the whole server whenever a player enters the End.
- Enable or disable the vanilla player locator bar for every world.
- Allow or block phantom spawning in every world.
- Set a persistent world-border radius centered at X/Z zero.
- Track team kills and deaths and determine the winner when a game is stopped.

## Requirements

- Paper 26.2
- Java 25 or newer to run the server
- Java 26 to build with the configured Gradle toolchain

## Building

A system installation of Gradle is currently required because this repository does not include the Gradle wrapper scripts.

```bash
gradle build
```

The deployable shaded plugin JAR is generated at `build/libs/EnderTeamBattle-1.3-all.jar`.

To start a local Paper development server:

```bash
gradle runServer
```

## Installation

1. Build the plugin or download a release JAR.
2. Copy `EnderTeamBattle-1.3-all.jar` into your Paper server's `plugins/` directory.
3. Start or restart the server.
4. Grant `enderteambattle.admin` to administrators, or use the commands as a server operator.

## Commands

All commands require the `enderteambattle.admin` permission.

| Command | Description |
| --- | --- |
| `/etb init` | Reset all EnderTeamBattle configuration and team data. |
| `/etb team create <teamname> <color>` | Create a team with the selected color. |
| `/etb team join <teamname> <username>` | Add a player to a team. |
| `/etb team kick <teamname> <username>` | Remove a player from a team. |
| `/etb team delete <teamname>` | Delete a team and unassign all of its members. |
| `/etb team list` | List all teams, colors, and member counts. |
| `/etb option teamAttack <allow\|deny>` | Allow or block teammates from damaging each other. |
| `/etb option locatorBar <enable\|disable>` | Enable or disable the player locator bar in every world. |
| `/etb option phantomSpawn <allow\|deny>` | Allow or block phantom spawning in every world. |
| `/etb option worldBorder <radius>` | Set every world's border to `-radius` through `+radius` on X and Z. |
| `/etb start` | Start a new game or resume a paused game. |
| `/etb pause` | Pause the running game and freeze participating players. |
| `/etb stop` | Stop the game and award the highest-scoring team; tied highest scores are a draw. |

Team names must be 1–32 characters long and may contain letters, numbers, underscores, and hyphens. Commands support tab completion for team names, players, and colors.

Available colors are:

`black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, and `white`.

## Game Flow

1. Create teams and add players.
2. Run `/etb start` to begin the battle.
3. The first participating team to defeat the Ender Dragon wins, and the game returns to the idle state.
4. Use `/etb pause` to temporarily freeze team members. Run `/etb start` to resume.
5. Use `/etb stop` to end the current game and award the team with the highest score. A team's score is its kills against opposing team members minus all deaths by its members. A tie for the highest score is a draw.

New players assigned to a team spawn at that team's default location. On death, the team spawn is used only when the player has no valid personal bed, respawn anchor, or other assigned respawn location.

Team spawns are distributed evenly around the border center. Three teams form an equilateral triangle with roughly 550-block sides; other team counts form the corresponding regular polygon. A border cannot be reduced below the radius required to contain the current teams, and creating a team is rejected if its spawn would fall outside the configured border.

Plugin data is stored in `plugins/EnderTeamBattle/config.yml`.
