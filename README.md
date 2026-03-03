# Custom Anvil

**Custom Anvil** is a plugin that allows server administrators to customize every aspect of the anvil's mechanics. 
It is expected to work on 1.18 to 1.21.7 minecraft servers running spigot or paper.
(the plugin support of 1.16.5 to 1.17.1 is experimental and may encounter issues)

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
- Custom enchant restrictions (allow unsafe enchantment only for a group of item or create new restriction).
- Custom items of unit repairs (repair damaged with unit of "material", for example the repair of diamond sword by diamonds).
- Custom XP cost for every aspect of the anvil.
- Permissions to bypass level limit or enchantment restriction.
- Display XP cost instead of "too expensive" when above level 40. (see below for more information)
- Can handle some custom enchantment plugins (see below for more information)
- Gui to configure the plugin in game.
- Support use of color code, hexadecimal color and minimessage for color/decoration
- (Experimental) Folia support (gui do not work)
---
### Permissions:
```yml
ca.affected: Player with this permission will be affected by the plugin
ca.bypass.fuse: Allow player to combine every enchantments to every item (no custom limit)
ca.bypass.level: Allow player to bypass every level limit (no custom limit)
ca.command.reload: Allow administrator to reload the plugin's configs
ca.config.edit: Allow administrator to edit the plugin's config in game
# Bellow permissions also require some config change to allow usage of features
# usage of these permission is toggleable in basic config gui or config.yml

# Permissions related to use of color and minimessage
ca.color.code: Allow player to use color code on rename if enabled (toggleable)
ca.color.hex: Allow player to use hexadecimal color on rename if enabled (toggleable)
ca.rename.minimessage: Allow player to use minimessage formating on rename if enabled (toggleable) (only legacy compatible at the time)

# Permissions related to edition of the lore
ca.lore_edit.book: Allow player to edit lore via book and quil if enabled (toggleable)
ca.lore_edit.paper: Allow player to edit lore via paper if enabled (toggleable)
```

### Commands
```yml
anvilconfigreload or carl: Reload every config of this plugin
customanvilconfig or configanvil: open a menu for administrator to edit plugin's config in game
```
### Supported Plugins
See the [Compatibility list](https://github.com/alexcrea/CustomAnvil/blob/v1.x.x/COMPATIBILITY.md)

### Overriding Too Expensive

One of the configurations allow displaying price about 40 and removing Too Expensive. \
By how the minecraft client work: price above 40 can only be displayed green, even if the player does not own enough experience level. 
Minecraft version 1.18 to latest marked as supported do not need any ProtocoLib dependency. \
Any recent paper version also are supported for this feature. 
But you should wait for update for new version containing new enchantable item or new enchantments.
Other version need ProtocoLib enabled on your server for this feature. \
You can also wait for an update of the plugin to support a newer version.

Please note that 1.16.5 to 1.17.1 are not officially supported. Run at your own risk.

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
