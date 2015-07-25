# EithonFixes

A EithonFixes plugin for Minecraft.

## Release history

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

