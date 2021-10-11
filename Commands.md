# RPBot Commands
The prefix for this bot is `&`

## General
### `&help`
Alias `&?`

Sends a link to this document

## Dice roll commands
### `&roll <expression> [character]`
Alias `&r`

The `&roll` command allows you to roll any combination of the following expressions:
* **Dice**, in the format `<n1>d<n2>`, where `n1` is the quantity and `n2` is the number of sides on the die. If `n1` is not included, it defaults to 1
    * Example: `2d6` rolls two 6-sided dice. `d20` rolls one 20-sided die.
* **Fixed modifiers**, in the format `<n>` where `n` is any integer (note that floating point numbers have undefined behaviour)
    * Example: `5` rolls a constant 5 every time
* **Character attributes**, in the format of `<attribute name>`, where `attribute name` is the number of sides on a single die.
  You can use aliases (if configured) instead of the full attribute name.  
  **Note**: If an attribute the first expression in the roll command, then a d20 will automatically be added.

All dice expressions can be added together with `+` and subtracted with `-`.

If you have no expression at all, then you will roll a default d20.

For character attributes, you will default to using the character you have claimed.
You can switch characters to use by adding the character's name as another argument after your dice expression.
If your character does not have the specified attribute or you don't have a claimed character, a +0 fixed modifier is used instead.

If the character has a temporary modifier on their attribute (e.g. +5 strength), then a fixed modifier will automatically be added when the attribute is used in an expression.
The temporary modifier will automatically decrement the number of uses left on it.
**Note**: Do not use the attribute multiple times in a single roll, this has undefined behaviour.

**Basic Examples**:
* `&roll` - Roll a standard d20
* `&r` - Roll a standard d20. Alias for the command above
* `&roll d20` - Roll a d20
* `&roll 2d10 + d4` - Roll 2 d10 and one d4
* `&roll d20 + 3` - Roll d20 + 3. Fixed bonus of 3
* `&roll d10 - 5` - Roll d10 - 5

**Examples With Character Profiles**

Assumptions:
* There are two characters named `Char1` and `Char2`
* The user running the command has claimed the character `Char1`
* `Char1` and `Char2` has an attribute called `strength` (alias `str`) set to 5 and 3 respectively

Examples:
* `&roll strength` - Rolls d20 + d5. The d5 is based on `Char1` strength attribute of 5
* `&r str` - Rolls d20 + d5. Alias for the command above
* `&r str + 2` - Rolls d20 + d5 + 2. Adds a fixed bonus of 2
* `&r strength char2` - Rolls d20 + d3. The d3 is based on `Char2` strength of 3. Note names are case-insensitive
* `&r str + 2d4 + 1 char2` - Rolls d20 + d3 + two d4 + 1
* `&r d10 + str + 2` - **Rolls d10 + d5 + 2.** When adding anything before the character's attribute, the default d20 is dropped

**Example With Temporary Modifier**

Assumptions:
* `Char1` and `Char2` an attribute called `agility` (alias `agi) set to 2 and 4 respectively
* `Char1` has a +3 modifier for 2 more rolls on their `agility` attribute

Example:  
The following commands are ran sequentially. Note how commands 2 and 3 are added but have no effect on the sequence. 
1. `&r agi` - Rolls d20 + d2 + 3. The +3 modifier from agility is added on top of the usual d20 and d2. The temporary modifier is decremented from 2 more rolls to just 1 more roll.
2. `&r` - _Rolls a d20, as usual. Has no effect on the temporary modifier because command does not include an `agi` expression_
3. `&r agi char2` - _Rolls a d20 + d4, as usual. Has no effect on the temporary modifier because the temporary modifier belongs to `Char1` not `Char2`_
4. `&r agi` - Rolls d20 + d2 + 3. Same as command 1, now the temporary modifier is decremented from 1 to 0 (i.e. it is removed)
5. `&r agi` - Rolls d20 + d2. The +3 modifier is now removed so it has no effect.


## Character Profile Commands
### `&list`
List all characters and their attributes on your server. Command may take a few seconds to complete, depending on the number of characters.
**Note**: If you have a lot of characters on your server, this will list all of them.

### `&listchar [character]`
Alias `&char`

List the attributes for a character. Defaults to your claimed character if `character` argument is unspecified.

**Examples**:
* `&listchar` - Show the attributes for the user's character
* `&char` - Alias for the command above
* `&listchar Char1` - Show the attributes for `Char1`

### `&listattribute <attribute> [character]`
Aliases `&attribute`, `&listattr`, `&attr`

Display the character's attribute. Defaults to your claimed character if `character` argument is unspecified.
Can use aliased attributes (if configured)

**Examples**:
* `&listattribute strength` - Show the user's strength attribute for their character
* `&attr str` - Alias for command above, assuming alias `str` is for `strength`
* `&attr str char2` - Show the strength attribute for `Char2`. Character names are case-insensitive

### `addcharacter <LINE BREAK> Name: <Character Name> [<LINE BREAK> <attribute>: <value> ]...`
Alias `&addchar`

Create a character from your message. The name must be unique (names are treated as case-insensitive in commands but displays exactly as you typed them).
The following lines are for the character's name and their attributes (and respective values)
You must add line breaks after the command and between each attribute.
Remember to claim your character with `&claim <character name>` when you are done!

Currently, renaming characters is not supported. To change your character's name, delete the character and create a new one.

**Note**: To get a line break on Discord using your computer, press **shift + enter**.
To get a line break on your phone, simply press the return button at the bottom right.

**Warning**: Character names must start with a letter (i.e. they cannot start with a number)

**Example**:  
The following example is _all one message_ using line breaks
```
&addchar
Name: Char1
strength: 5
agility: 3
intelligence: 1
charisma: 2
hp: 20/20
stamina: 100/100
```

### `addcharacterfromid #<channel-name> <numeric_message_id>`
Alias `&addcharfromid`, `&addcharid`

**Note**: To find your message ID, do one of following:

Create a character from an existing Discord message sent.
The `#<channel-name>` is the name of the channel that the command was sent from (use Discord's `#` feature to write the channel's name).
The `<numeric_message_id>` is the ID of the message with your character's information. See below for how to get the ID.
The required arguments (i.e. the `Name` and attributes) are identical to the command `&addchar`.

_Option 1: Message ID from message link_
1. Right-click the message/open the **More** option on your message and press **Copy message link**.
2. Paste the message link in chat. It should be in the format `https://discord.com/channels/111111111111111111/222222222222222222/333333333333333333`
3. Copy just the sequence of numbers after the last `/` (e.g. the `333333333333333333` in the example above)

_Option 2: Message ID using Developer Mode_
1. Under your Discord **User settings**, go to **Advanced** under App settings then enable **Developer Mode**
2. Right-click the message/ open the **More** option on your message and select **Copy ID**

**Example**:  
The following example is made of _two_ messages. Use line breaks between each line.
Note that the first message has no command in it.

Assumptions:
* The message has ID `333333333333333333`
* The message was sent under the channel `#game`

```
Name: Char1
strength: 5
agility: 3
intelligence: 1
charisma: 2
hp: 20/20
stamina: 100/100
```
```
&addcharfromid #game 333333333333333333
```

### `&deletecharacter <character>`
Alias `&delchar`

Delete the specified character.
**WARNING**: This command cannot be undone! Be careful with this!

**Note**: Please have users run `&unclaim` on their characters before the characters are deleted

**Example**
`&delchar char1` - Delete `Char1`. Note that names are case-insensitive

### `&claimchar <character>`
Alias `&claim`

Claim a character as yours on this server.
This changes commands like `&roll` and `&attr` to default to your character if you do not specify a player.

If you already claimed a different character, you will automatically unclaim that character before claiming this one.
You cannot claim a character that has already been claimed by another user. Ask them to run `&unclaim <character>` first.

**Example**
`&claim char1` - Claim `Char1` for yourself. Note that names are case-insensitive

### `&unclaimchar`
Alias `&unclaim`

End your claim on a character and allow other players to claim it.

### `&setattr <value>[/<max_value>] <attribute> [character]`
Alias `&set`

Set an attribute for a character to the specified value. Defaults to the user's character if the character is not defined.
By adding `/<max_value>` to the end of the value, you can set a maximum value for that attribute.
Attributes and max values must be integers. For the `&roll` command using an attribute with a maximum value, only the current value is used.

If the attribute does not exist on the character, the attribute is added.

**Note**: Maximum values can be changed by running the `&set` command, but currently cannot be removed.
To remove a maximum value, use `&deleteattribute` and recreate the attribute

**Examples**
* `&setattr strength 6` - Set the strength attribute to 6 for the current character
* `&set str 6` - Alias for the command above, assuming alias `str` is for `strength`
* `&set str 4 char2` - Set the strength attribute to 4 for `Char2`. Character names are case-insensitive
* `&set hp 15/20` - Set the HP attribute to 15 out of 20 for the current player
* `&set hp 21/20` - Set the HP attribute to 21 out of 21 for the current player
* `&set hp 14` - Set the HP attribute to 14 out of 20 for the current player. 
  Note that the "out of 20" is only implied if the attribute was already defined with a maximum value (from either creating the character or running the `&set` command with `/20` in it earlier) 
  
### `&deleteattribute <attribute> [character]`
Alias `&delattr`

Deletes an attribute from a character. Defaults to the user's character if the character is not defined.

**Examples**
* `&delattr strength` - Delete the strength attribute from the current character
* `&delattr str` - Alias for the command above, assuming alias `str` is for `strength`
* `&delattr hp char2` - Delete the HP attribute from `Char2`. Character names are case-insensitive

## Quick attribute modifier commands
The following commands all look similar because they either start with `&+` or `&-`.
They come in two variants:
1. Directly add or subtract values to an attribute
2. Add a temporary modifier (buff/debuff) to an attribute

### `&+<number> <attribute> [character]` and `&-<number> <attribute> [character]` to Edit Attributes
Alternative forms: `&++` and `&--`

Add or subtract the amount specified to an attribute. Defaults to the user's character if the character is not defined.

If an attribute has a maximum value, then the `&+` command will cap the increase at the maximum value. 
This is useful for attributes like HP where you want to quickly give characters health but do not want to heal them past their maximum value.

If you want to exceed the maximum value anyway, you can use `&++` instead, and the max value will be ignored.
Currently, `&--` is also supported but functions identically to `&-`

**Basic Example**:
* `&+1 strength` - Add 1 to the strength attribute of the current user
* `&-2 str char2` - Subtract 2 from the strength of the current user

**Maximum Value Example**:  
Assumptions:
* `Char1` has an attribute `exp` set to `95/100`
* `Char1` and `Char2` have an attribute `hp` set to `18/20` and `25/20` respectively

Examples:
* `&+8 exp char1` - Sets the `exp` of `Char1` to `100/100` because the `exp` cannot exceed the maximum value
* `&++5 hp char1` - Sets the `hp` of `Char1` to `23/20`. Using `&++` ignores the maximum value
* `&+5 hp char2` - Does not change the `hp` of `Char2` because they are already above their maximum value
* `&-1 hp char2` - Sets the `hp` of `Char2` to `24/20`. Using `&-` still decrements the `hp` if it is above its maximum value

### `&+<number> <attribute> <duration> [character]` and `&-<number> <attribute> <duration> [character]` to Buff/Debuff Attributes

Adding a `duration` gives a temporary modifier to the attribute specified for the given character (i.e. a buff or debuff).
The duration represents the number of rolls that are modified when the character use that attribute in a roll.
The duration must be a positive integer.

Temporary modifiers can be viewed on attributes through the `&list`, `&listchar` and `&attr` commands
Only one temporary modifier can be applied at a time per attribute - any new temporary modifiers replace existing modifiers.

See the `&roll` command to see how rolls are affected by temporary modifiers.

Currently, `&++` and `&--` variants on the command function identically to `&+` and `&-` respectively when a duration is applied.

**Examples**:
* `&+1 str 3` - Add a temporary fixed buff of +1 to the next 3 rolls for `str` for this user
* `&-2 str 4 char2` - Add a temporary fixed debuff of -2 to the next 4 rolls for the `str` for `Char2`


## Administrative Commands
### `&alias <alias_name> <full_name> [<alias_name2> <full_name2>] [<alias_name3> <full_name3>]...`
Alias `&multialias`

Add alias (abbreviations) for attribute names. Can add multiple aliases at once by adding extra arguments.
You can add multiple aliases for the same attribute name. 
Using the same alias for a new attribute name will remove the old alias.

**Warning**: Currently there is no way to remove alias completely

**Examples**:
* `&alias str strength` - Add an alias for `strength` as `str`
* `&alias str strength st strength` - Add a second alias for `strength` as `st`
* `&alias str strength def defense agi agility` - Add aliases for `strength`, `defense` and `agility` respectively with `str`, `def` and `agi`

### `&savechar [character]`
Alias `&save`

Saves the attributes of the character the user ran the command for.

### `&read`

Reload all the character profiles from the files stored on disk.