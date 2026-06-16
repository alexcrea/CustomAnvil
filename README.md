# Custom Anvil

**Custom Anvil** is a plugin that allows server administrators to customize every aspect of the anvil's mechanics. 

### Download Locations:

the plugin can be downloaded on
 [Modrinth](https://modrinth.com/plugin/customanvil),
 [Hangar](https://hangar.papermc.io/alexcrea/CustomAnvil)
 or here [on GitHub](https://github.com/alexcrea/CustomAnvil/releases/latest)

---
**Custom Anvil** have the following features:
- Vanilla like default configuration.
- Custom enchantment level limit.
- Custom anvil recipes.
- Custom enchant restrictions (allows unsafe enchantment only for a group of item or create new restriction).
- Custom items of unit repairs (repair damaged with unit of "material", for example the repair of diamond sword by diamonds).
- Custom XP cost for every aspect of the anvil.
- Permissions to bypass level limit or enchantment restriction.
- Display XP cost instead of "too expensive" when above level 40. (see below for more information)
- Can handle some custom enchantment plugins (see below for more information)
- Gui to configure the plugin in game.
- Support use of color code, hexadecimal color and minimessage for color/decoration
- (Experimental) Folia support (gui do not work)
- (Experimental) Dialog rename (allows longer rename)
- (Experimental) Anvil with monetary cost (using vault) (require dialog rename)
And others !
---
### Permissions:
Note that for most of them you also need to enable feature and in most case enable use of permission for the specfic feature (indicated with `(toggleable)`)
```yml
# Generic and bypass permissions
ca.affected: Player with this permission will be affected by the plugin
ca.bypass.fuse: Allow player to combine every enchantments to every item (no custom limit)
ca.bypass.level: Allow player to bypass every level limit (no custom limit)

# Command permissions
ca.command.reload: Allow administrator to reload the plugin's configs
ca.command.diagnostic: Allow adminastator to diagnistic some simple problem with the plugin
ca.config.edit: Allow administrator to edit the plugin's config in game

# -----------------------------------------------------------------------------
# Bellow permissions also require some config change to allow usage of features
# Usage of these permission is toggleable in basic config gui or config.yml
# -----------------------------------------------------------------------------
# Permissions related to use of color and minimessage
ca.color.code: Allow player to use color code on rename if enabled (toggleable)
ca.color.code.[thecode] (for example ca.color.code.a): Allows usage of only certain color code (toggleable)
ca.color.hex: Allow player to use hexadecimal color on rename if enabled (toggleable)
ca.rename.minimessage: Allow player to use minimessage formating on rename if enabled (toggleable)

# Permissions related to edition of the lore
ca.lore_edit.book: Allow player to edit lore via book and quil if enabled (toggleable)
ca.lore_edit.paper: Allow player to edit lore via paper if enabled (toggleable)

# Others
ca.rename.dialog: Allow player to use the rename dialog (toggleable)
```

### Commands

run `/customanvil help` to get information about available commands  \
this only show subcommands you have permission for

### Supported Plugins
See the [Compatibility list](https://github.com/alexcrea/CustomAnvil/blob/v1.x.x/COMPATIBILITY.md)

### Overriding Too Expensive

One of the configurations allow displaying price about 40 and removing Too Expensive. \
By how the minecraft client work: price above 40 can only be displayed green, even if the player does not own enough experience level. 
spigot version 1.18 to 1.21.11 do not need any ProtocoLib dependency. (26.1.0 or above requires it) \
Any recent paper version also are supported for this feature. 
But you should wait for update for new version containing new enchantable item or new enchantments if any of this got added.
Else it is, likely, fine to use the current version you are ussing on a new paper version

### For custom enchantment plugin developers
For information about the API, please refer to [the Wiki](https://github.com/alexcrea/CustomAnvil/wiki) \
(Please note that the wiki is currently incomplete)

---

### Default Plugin's Configurations
see [Here](https://github.com/alexcrea/CustomAnvil/tree/master/defaultconfigs)

---
### Metric And Telemetry
Custom anvil [use bstat](https://bstats.org/plugin/bukkit/Unsafe%20Enchants%20Plus/20923) 
and [faststats](https://faststats.dev/project/customanvil/minecraft-plugin) for metric and error reporting.

You can select specific telemetry or disable all in config.yml. \
You can also [disable bstat](https://bstats.org/getting-started) and [faststats](https://faststats.dev/info) in their /plugin folder if you like too.

faststats is in beta testing please report me or them any error you encounter

### Credits and Thanks
Credits and thanks can be seen [here](https://github.com/alexcrea/CustomAnvil/blob/v1.x.x/CREDITS.md)

### Planned:
- Better Folia support (make gui work. fix some dirty handled parts)
- Get restriction on unknown enchantments (planned for V2)
- More features for custom anvil craft

### Known issue:
Most unknown registered enchantments (by unsupported custom enchantment plugin & datapacks) will not have restriction by default. Planned but no eta.

### Do you need help with the plugin, or have any issue or suggestion?
You can ask on the discussion page, create a [GitHub issue](https://github.com/alexcrea/CustomAnvil/issues) or join my [discord](https://discord.gg/KHUNsUfRYJ)​
