# EithonFixes

A EithonFixes plugin for Minecraft.

## Release history

### 1.34.5 (2016-04-09)

* BUG: Wrong command syntax.
* BUG: rc goto ran rc delete by mistake.

### 1.34.4 (2016-01-24)

* BUG: Compiled with the wrong version of EithonLibrary

### 1.34.3 (2016-01-24)

* BUG: "/eithonfixes restart" was actually executing "/eithonfixes cancel".

### 1.34.2 (2016-01-23)

* BUG: Commands /eithonfixes server and /eithonfixes restart did not work.

### 1.34.1 (2016-01-23)

* BUG: amount was not optional in the buy command.

### 1.34 (2016-01-20)

* CHANGE: Moved commands freeze on/off/restore/list to copbot.

### 1.33 (2016-01-20)

* NEW: Added commands freeze on/off/restore/list
* CHANGE: Command balance now accepts offline players. 

### 1.32 (2016-01-19)

* CHANGE: Now uses the new command syntax interpretator from EithonLibrary 

### 1.31.2 (2016-01-14)

* BUG: When the last region command (or spawn point) was deleted, the save file was not updated. 

### 1.31.1 (2016-01-13)

* BUG: Balance expected no arguments

### 1.31 (2016-01-12)

* CHANGE: Balance now gets an optional player

### 1.30.1 (2015-11-28)

* BUG: Did not save the list of RegionCommands after deleting a RegionCommand.

### 1.30 (2015-11-14)

* CHANGE: Now has max amount for Consecutive Days reward.

### 1.29 (2015-11-13)

* CHANGE: Optimization of PlayerMoveEvent

### 1.28 (2015-10-18)

* CHANGE: Refactoring EithonLibrary.

### 1.27 (2015-10-17)

* CHANGE: Added command /eithonfixes debug

### 1.26 (2015-10-13)

* CHANGE: New EithonLibrary

### 1.25.2 (2015-10-12)

* BUG: The bungee server name was empty for quit messages.

### 1.25.1 (2015-10-12)

* BUG: The bungee server name was empty for join messages.

### 1.25 (2015-10-11)

* NEW: Added EithonBungeeQuitEvent
* BUG: There were double join and quit messages.

### 1.24.1 (2015-10-11)

* BUG: Minor fixes for EithonBungeeJoinEvent.

### 1.24 (2015-10-11)

* NEW: Added EithonBungeeJoinEvent.

### 1.23 (2015-10-11)

* NEW: Bungee cord test

### 1.22.2 (2015-10-04)

* BUG: Wrong server name.

### 1.22.1 (2015-10-04)

* BUG: Quit message was announced as join.

### 1.22 (2015-10-04)

* NEW: Added bungee login message.
* NEW: Added bungee command.

### 1.21 (2015-09-23)

* NEW: Added spawn points.

### 1.20 (2015-09-23)

* NEW: Region commands now has "/rcset" and an option for ignoring enter/leave from/to other world.

### 1.19 (2015-09-22)

* NEW: Added "/eithonfixes server" command.

### 1.18.1 (2015-09-17)

* BUG: Could not read scheduler configuration

### 1.18 (2015-09-10)

* NEW: Added scheduler for weekly commands.
* BUG: The reward formula for consecutive days award was off by 1.

### 1.17 (2015-08-30)

* NEW: Adds money to user that has consecutive days on the server.
* BUG: Could not restart server from the console.

### 1.16 (2015-08-29)

* NEW: When a flying player teleports to a world where flying is not allowed, we try to stop the flying.

### 1.15.1 (2015-08-23)

* BUG: Cooldown for teleporting to worlds tried to find the world name among the cool down commands.

### 1.15 (2015-08-19)

* NEW: Added cooldown for teleporting to worlds.
* BUG: "/eithonfixes restart" throws exception

### 1.14.1 (2015-08-16)

* BUG: "/eithonfixes restart" throws exception
* BUG: Could not read PermissionBasedMultiplier

### 1.14 (2015-08-16)

* NEW: Permission based multipliers for killing mobs
* CHANGE: Now uses TimeSpan for restart countdown.

### 1.13 (2015-08-10)

* CHANGE: All time span configuration values are now in the general TimeSpan format instead of hard coded to seconds or minutes or hours.

### 1.12 ()

* CHANGE: The restart command now takes the time in the general time format (10 or 10s means 10 seconds, 5:00 or 5m means 5 minutes, 1:00:00 or 1h means 1 hour).

### 1.11 (2015-08-03)

* NEW: Flying is now only allowed in specific worlds
* BUG: Command settings where not read from the config file.

### 1.10 (2015-07-25)

* NEW: Added restart command.

### 1.9 (2015-07-25)

* NEW: CoolDown for commands can now specify how many incidents that are allowed during the cooldown period.

### 1.8.1 (2015-07-24)

* CHANGE: If a command starts with * it will be executed with the user in Op mode.
* CHANGE: If a command starts with # it will be executed with by the console.
* BUG: RegionCommand was sending all commands at once.

### 1.8 (2015-07-20)

* NEW: RegionCommand can now be a serie of commands, separated by ';'
* BUG: After a restart, a superuser command was no longer run as a superuser.

### 1.7 (2015-07-19)

* NEW: RegionCommand can now be run as console user by adding a '*' at the beginning of the command (but only if your are op).

### 1.6.1 (2015-07-18)

* BUG: Wrong time for first login.

### 1.6 (2015-07-17)

* NEW: Detects new players.

### 1.5 (2015-07-16)

* NEW: Added a test command to test different things. Currently for testing CountDown.

### 1.4.2 (2015-07-15)

* CHANGE: Added printout when not finding a material.

### 1.4.1 (2015-07-14)

* CHANGE: Money is rounded to closest integer.

### 1.4 (2015-07-13)

* CHANGE: Money is rounded to closest two decimals.

### 1.3 (2015-07-11)

* NEW: RegionCommands

### 1.2 (2015-07-09)

* NEW: Added cooldown for KillerMoney rewards.
* CHANGE: Now verifies that the player inventory has room for what to buy.
* BUG: Now shows subcommands if no subcommand was given.

### 1.0 (2015-04-18)

* First proper Eithon release

