# RPBot
By @AlphaRLee

A Discord bot that provides simple role-playing features, like dice rolling, character sheets and maps.
**Disclaimer**: This bot is created by somebody who has never played D&D or any other tabletop game

## Commands
The prefix for this bot is `&`

### Dice roll commands
#### `&roll <expression>`
Alias `&r`

The `&roll` command

**Basic Examples**:
* `&roll` - Roll a standard d20
* `&r` - Roll a standard d20. Alias for the command above
* `&roll d20` - Roll a d20
* `&roll 2d10 + d4` - Roll 2 d10 and one d4
* `&roll d20 + 3` - Roll d20 + 3. Fixed bonus of 3
* `&roll d10 - 5` - Roll d10 - 5

**Examples With Character Profiles**

Assumptions:
* There are two players named `Player1` and `Player2`  
* The user running the command has claimed the profile `Player1`
* `Player1` and `Player2` has `strength` (alias `str`) set to 5 and 3 respectively

Examples:
* `&roll strength` - Rolls d20 + d5. The d5 is based on `Player1` strength of 5
* `&r str` - Rolls d20 + d5. Alias for the command above
* `&r str + 2` - Rolls d20 + d5 + 2. Adds a fixed bonus of 2
* `&r strength player2` - Rolls d20 + d3. The d3 is based on `Player2` strength of 3. Note names are case-insensitive
* `&r str + 2d4 + 1 player2` - Rolls d20 + d3 + two d4 + 1