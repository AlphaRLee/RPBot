# RPBot
By @AlphaRLee

A Discord bot that provides simple role-playing features, like dice rolling, character sheets and maps.
This bot is designed to be versatile and unopionated - it allows for total customization of your characters, your rolling, your maps, etc.

**Important notes**:
* This bot should only be ran on small servers where all members are trusted
* This bot is created by somebody who has never played D&D or any other tabletop game

## Commands
Please see the [Commands.md](Commands.md) file for a full list of commands

## Simple Dice Example
We can roll our dice using the `&roll` command, aliased to `&r`. Rolling without any arguments just defaults to rolling a d20
```
&r
```
We can add  together any combination of rolls to make our game more interesting
```
&roll d20
&r 2d6
&r d8 + d4 + 3
&r 2d4 - 2
```

## Example With Characters
Adding a character sheet automates the painful parts of needing to memorize your character.

First create a character using the `&addchar` command. Let's make Lance the spearman.
```
&addchar
Name: Lance
strength: 5
defense: 4
agility: 3
intelligence: 1
charisma: 2
health: 100/100
experience: 0/10
```
> Character **Lance** added!

For convenience, the user who wants to control Lance should type the following command:
```
&claim lance
```
> @DiscordUser has claimed the character profile **Lance**.

We can now look at Lance at a glance
```
&char lance
```
The user who claimed Lance can look at a glance - most commands default to their character if none is specified
```
&char 
```
> Name: Lance  
> Player: @DiscordUser#1234  
>
> strength:       5  
> defense:        4  
> agility:        3  
> intelligence:   1  
> charisma:       2  
> health:   100/100  
> experience:  0/10  

Also for convenience, let's make some aliases for each of Lance's attributes. We'll abbreviate
* strength -> str
* defense -> def
* agility -> agi
* intelligence -> int
* charisma -> cha
* health -> hp
* experience -> exp
We can run the following command to set up these aliases:
```
&alias str strength def defense agi agility int intelligence cha charisma hp health exp experience
```
> Alias(es) set:
>   str **->** strength    def **->** defense    agi **->** agility    int **->** intelligence    cha **->** charisma    hp **->** health    exp **->** experience

Suppose Lance meets a Slime monster along the way. Let's add it to the game
```
&addchar
Name: Slime
strength: 2
defense: 1
agility: 1
health: 10/10
```
> Character **Slime** added!

Let's fight! When you want to roll a d20 and add in Lance's strength, this bot factors that into one tidy command:
```
&r str
```
This command reads "roll a d20 and add a die with the 5 faces on it based on Lance's strength".
> @DiscordUser rolled **18**. (15 + 3)

The slime defends itself! The Dungeon Master can roll for the slime's defense
```
&r def slime
```
> Slime rolled **11**. (10 + 1)

Let's say the slime takes 7 damage (18 - 11 = 7). We quickly remove that from the slime's health.
```
&-7 hp slime
```
> Slime lost **7** health. (10/10 -> 3/10)

Lance decides to drink a potion to boost his defense by +2 for 3 turns
```
&+2 def 3
```
> Lance got a +2 buff on def for 3 rolls.

He quickly checks his defense attribute
```
&attr def
```
> Lance's def: 4  +2 (3 rolls left)

The slime attacks! It unexpectedly jumps at Lance, so it gets +3 for surprise!
```
&r str + 3
```
> Slime rolled **14**. (9 + 2 + 3)

Lance rolls defense. His +2 boost from his potion is automatically added
```
&r def
```
> @DiscordUser rolled **10**. (5 + 3 + 2)

Lance takes 4 damage (14 - 10 = 4)
```
&-4 hp lance
```
> Lance lost **4** health. (100/100 -> 96/100)

He rolls his strength one last time, and the slime rolls its defense
```
&r str
```
> Lance rolled **16**. (14 + 2)
```
&r def slime
```
> Slime rolled **13**. (12 + 1)
```
&-3 hp slime
```
> Slime lost **3** health. (3/10 -> 0/10)

Lance wins! We'll delete the slime now
```
&delchar slime
```
> Character **Slime** has been deleted!

Let's also give +15 experience for this slime. But Lance only needs +10 to level up
```
&+15 exp lance 
```
> Lance gained **15** experience. (0/10 -> 10/10)

Let's reset the exp needed for his next level to 20, and give him that 5 leftover exp
```
&set exp 5/20 lance
```
> Lances's experience: 5/20

And to finish it off, Lance gets +1 point to improve any attribute he wants. He spends it on strength
```
&+1 str
```
> Lance gained **1** strength. (5 -> 6)

## Map Example
The map tool offers 2D text-based maps
Full example TBD

```
&map show demomap
```

```
DemoMap:
 +———+———+———+———+———+———+———+———+
8| X | W |   |   | █ |   |   | U |
 +———+———+———+———+———+———+———+———+
7|   |   |   |   | █ | █ |   |   |
 +———+———+———+———+———+———+———+———+
6|   |   |   |   |   |   |   |   |
 +———+———+———+———+———+———+———+———+
5|   |   |   |   |   |   |   |   |
 +———+———+———+———+———+———+———+———+
4|   |   |   |   |   |   |   |   |
 +———+———+———+———+———+———+———+———+
3|   |   |   | D |   |   |   |   |
 +———+———+———+———+———+———+———+———+
2|   |   | C |   | E | F |   |   |
 +———+———+———+———+———+———+———+———+
1| Z | / |   |   |   |   |   | Y |
 +———+———+———+———+———+———+———+———+
   A   B   C   D   E   F   G   H
```  

```
&map legend
```
> DemoMap Legend:
> `/` - Wall [**B1**]  
> `B` - Brontosaurus [**-B-1**]  
> `C` - Camel [**C2**]  
> `D` - Dino [**D3**]  
> `E` - Elephant [**E2**]  
> `F` - Fish [**F2**]  
> `U` - Unicorn [**H8**]  
> `W` - Walrus [**B8**]  
> `X` - Xerus [**A8**]  
> `Y` - Yak [**H1**]  
> `Z` - Zebra [**A1**]  
> `█` - Wall-1 [**E8**], Wall-2 [**E7**], Wall-3 [**F7**]  
