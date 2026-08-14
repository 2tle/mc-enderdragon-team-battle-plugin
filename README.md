# EnderTeamBattle

EnderTeamBattle is a Paper plugin for running team-based Ender Dragon battles. The first team to defeat the Ender Dragon wins.

## Features

- Create color-coded teams and manage their members.
- Show team colors and membership through the server scoreboard.
- Start, pause, resume, or stop a battle.
- Freeze participating team members while a battle is paused.
- Persist teams and game state in the plugin configuration.
- Announce game events and the winning team to the server.

## Requirements

- Paper 26.2
- Java 25 or newer to run the server
- Java 26 to build with the configured Gradle toolchain

## Building

A system installation of Gradle is currently required because this repository does not include the Gradle wrapper scripts.

```bash
gradle build
```

The shaded plugin JAR is generated in `build/libs/`.

To start a local Paper development server:

```bash
gradle runServer
```

## Installation

1. Build the plugin or download a release JAR.
2. Copy the JAR into your Paper server's `plugins/` directory.
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
| `/etb start` | Start a new game or resume a paused game. |
| `/etb pause` | Pause the running game and freeze participating players. |
| `/etb stop` | Stop the game and declare a draw. |

Team names must be 1–32 characters long and may contain letters, numbers, underscores, and hyphens. Commands support tab completion for team names, players, and colors.

Available colors are:

`black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, and `white`.

## Game Flow

1. Create teams and add players.
2. Run `/etb start` to begin the battle.
3. The first participating team to defeat the Ender Dragon wins, and the game returns to the idle state.
4. Use `/etb pause` to temporarily freeze team members. Run `/etb start` to resume.
5. Use `/etb stop` to end the current game as a draw.

Plugin data is stored in `plugins/EnderTeamBattle/config.yml`.
