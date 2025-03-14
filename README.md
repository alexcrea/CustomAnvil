# Custom Anvil

**Custom Anvil** is a plugin that allows server administrators to customize every aspect of the anvil's mechanics. 
It is expected to work on 1.18 to 1.21.3 minecraft servers running spigot or paper.
(the plugin support of 1.16.5 to 1.17.1 is experimental and may encounter issues)

**Custom Anvil** was previously named **Unsafe Enchants+**.
It was renamed because it now affects every anvil aspect and not only unsafe enchants\
**Custom Anvil** is based on [Unsafe Enchants](https://github.com/DelilahEve/UnsafeEnchants) by  DelilahEve.

### Download Locations:

the plugin can be downloaded on
[Spigot](https://www.spigotmc.org/resources/custom-anvil.114884),
 [modrinth](https://modrinth.com/plugin/customanvil),
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
- Support of color code and hexadecimal color
- (Experimental) Folia support (gui do not work)
---
### Permissions:
```yml
ca.affected: Player with this permission will be affected by the plugin
ca.bypass.fuse: Allow player to combine every enchantments to every item (no custom limit)
ca.bypass.level: Allow player to bypass every level limit (no custom limit)
ca.command.reload: Allow administrator to reload the plugin's configs
ca.config.edit: Allow administrator to edit the plugin's config in game
# Related to use of color (usage of permission for color is toggleable in basic config gui or config.yml)
ca.color.code: Allow player to use color code if permission is required (toggleable)
ca.color.hex: Allow player to use hexadecimal color if permission is required (toggleable)
```

### Commands
```yml
anvilconfigreload or carl: Reload every config of this plugin
customanvilconfig or configanvil: open a menu for administrator to edit plugin's config in game
```
### Supported Plugins
Custom Anvil can be compatible with some custom enchantments and anvil mechanics plugins.

Here is a list of supported custom enchantment plugins with support status:
- [Enchantment²](https://www.spigotmc.org/resources/enchants-squared-the-enchantsplus-rewrite-custom-enchantments-that-act-like-vanilla-ones.86747/):
support by Custom Anvil but still experimental. Automatic configuration.

- [EcoEnchant](https://www.spigotmc.org/resources/ecoenchants-%E2%AD%95-250-enchantments-%E2%9C%85-create-custom-enchants-%E2%9C%A8-essentials-cmi-support.79573/):
support by Custom Anvil but still experimental. Need to use /anvilconfigreload or a server restart to add newly added enchantment. 
Use EcoEnchant restriction system by default.

- [ExcellentEnchants](https://www.spigotmc.org/resources/excellentenchants-%E2%AD%90-75-vanilla-like-enchantments.61693/):
support by Custom Anvil but still experimental. use ExcellentEnchants item type.

- [Superenchants](https://modrinth.com/plugin/superenchants)
support by Superenchants. used CustomAnvil to combine enchantment in anvil in survival.

Here is a list of supported anvil mechanic plugins with support status:
- [Disenchantment](https://www.spigotmc.org/resources/disenchantment-1-21-1-1-20-6-new-book-splitting-mechanics.110741/)
support by Custom Anvil but still experimental. Mostly use Custom Anvil basic XP settings. (version >= 6.1.5)

- [HavenBags](https://www.spigotmc.org/resources/havenbags-shulker-like-player-bound-bags-1-17-1-21-4.110420/)
support by Custom Anvil. Not really enchantment related but CustomAnvil should not impact bag upgrade and skin via anvil. (version >= 1.31.0)

If you like Custom Anvil to support a specific plugin (custom enchant or anvil mechanic). 
You can ask, but please note implementing compatibility will be considered
as low priority as I work for the plugin on my free time for free.

### Overriding Too Expensive

One of the configurations allow displaying price about 40 and removing Too Expensive. \
By how the minecraft client work: price above 40 can only be displayed green, even if the player does not own enough experience level. 
Minecraft version 1.17 to 1.21.1 do not need any dependency. Other version need ProtocoLib enabled on your server for this feature. \
You can also wait for an update of the plugin to support a newer version.

Please note that 1.16.5 to 1.17.1 are not officially supported. Run at your own risk.

### For custom enchantment plugin developers
For information about the API, please refer to [the Wiki](https://github.com/alexcrea/CustomAnvil/wiki) \
(Please note that the wiki is currently incomplete)​

---

### Default Plugin's Configurations
For 1.18 to 1.20.6 use the [1.18 configurations](https://github.com/alexcrea/CustomAnvil/tree/master/defaultconfigs/1.18)\
For 1.21 to 1.21.1 use the [1.21 configurations](https://github.com/alexcrea/CustomAnvil/tree/master/defaultconfigs/1.21)

---
Custom anvil [use bstat](https://bstats.org/plugin/bukkit/Unsafe%20Enchants%20Plus/20923) for metric. You can [disable it](https://bstats.org/getting-started) if you like.

### Planned:
- Better Folia support (make gui work. fix some dirty handled parts)
- Get restriction on unknown enchantments
- More features for custom anvil craft

### Known issue:
Most unknown registered enchantments (by unsupported custom enchantment plugin & datapacks) will not have restriction by default. Planned but no eta.
