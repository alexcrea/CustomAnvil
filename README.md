# Custom Anvil

**Custom Anvil** is a plugin that allows server administrators to customise every aspect of the anvil's mechanics. 
It is expected to work on 1.18 to 1.21 minecraft servers running spigot or paper.
(the plugin support of 1.16.5 to 1.17.1 is experimental an may encounter issues)

**Custom Anvil** was previously named **Unsafe Enchants+**.
It was renamed because it now affects every anvil aspect and not only unsafe enchants\
**Custom Anvil** is based on [Unsafe Enchants](https://github.com/DelilahEve/UnsafeEnchants) by  DelilahEve.

### Download Locations:

the plugin can be downloaded on the
[Spigot site](https://www.spigotmc.org/resources/custom-anvil.114884)
or [on GitHub](https://github.com/alexcrea/CustomAnvil/releases/latest)

---
**Custom Anvil** have the following features:
- Vanilla like default configuration.
- Custom enchantment level limit.
- Custom anvil recipes.
- Custom enchant restrictions (allow unsafe enchantment only for a group of item or create new restriction).
- Custom items of unit repairs (repair damaged with unit of "material", for example the repair of diamond sword by diamonds).
- Custom XP cost for every aspect of the anvil.
- Permissions to bypass level limit or enchantment restriction.
- Display xp cost instead of "to expensive" when above lv 40. (need ProtocoLib)
- Can handle some custom enchantment plugins (see bellow for more information)
- Gui to configure the plugin in game.
---
### Permissions:
```yml
ca.affected: Player with this permission will be affected by the plugin
ca.bypass.fuse: Allow player to combine every enchantments to every item (no custom limit)
ca.bypass.level: Allow player to bypass every level limit (no custom limit)
ca.command.reload: Allow administrator to reload the plugin's configs
ca.config.edit: Allow administrator to edit the plugin's config in game
```
/!\ version under 1.3.1 use other permission. from 1.2.0 to 1.3.1-A1 use ua.unsafe instead of ca.affected
under 1.2.0 replace ca prefix by ue and use ue.unsafe. some permission/features may not exist before the last version.

### Commands
```yml
anvilconfigreload or carl: Reload every config of this plugin
customanvilconfig: open a menu for administrator to edit plugin's config in game
```
### Custom Enchantment Plugins
Custom Anvil can be compatible with some custom enchant plugins. \
Currently, there is not a lot of compatible plugin

Here is a list of supported plugins with support status:
- [Enchantment²](https://www.spigotmc.org/resources/enchants-squared-the-enchantsplus-rewrite-custom-enchantments-that-act-like-vanilla-ones.86747/):
Officially supported but still experimental. Automatic configuration translation.

If you like Custom Anvil to support a specific custom enchantment plugin. 
You can ask, but please note implementing compatibility will be considered
as low priority as I work for the plugin on my free time for free. \
For custom enchantment plugin developers: 
If you like to make your plugin compatible with Custom Anvil
I recommend reaching out to me as there is currently no documentation.

---


### Default Plugin's Configurations
For 1.18 to 1.20.6 use the [1.18 configurations](https://github.com/alexcrea/CustomAnvil/tree/master/defaultconfigs/1.18)\
For 1.21 use the [1.21 configurations](https://github.com/alexcrea/CustomAnvil/tree/master/defaultconfigs/1.21)

---
### Known issue:
There is non known issue, if you find one please report the issue.

### Planned:
- Semi manual config update on plugin or minecraft update
- Check unknown registered enchantment & warn
- Warn admin on unsupported minecraft version 


