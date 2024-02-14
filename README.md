# Custom Anvil

**Custom Anvil** is a plugin that allows server administrators to customise every aspect of the anvil's mechanics. 
It is expected to work on 1.18 to 1.20.4 minecraft servers running spigot or paper.

**Custom Anvil** was previously named **Unsafe Enchants+**.
It was renamed because it now affects every anvil aspect and not only unsafe enchants

**Custom Anvil** is based on [Unsafe Enchants](https://github.com/DelilahEve/UnsafeEnchants) by  DelilahEve. You can find it on 
[GitHub](https://github.com/DelilahEve/UnsafeEnchants/releases/latest), 
[Spigot](https://www.spigotmc.org/resources/unsafe-enchants.104708/) or
[CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/unsafe-enchants/files/all)
### Download Locations:

the plugin can be downloaded on the
[Spigot site](https://www.spigotmc.org/resources/custom-anvil.114884)
or [on GitHub](https://github.com/alexcrea/CustomAnvil/releases/latest)
---
**Custom Anvil** have the following features:
- Vanilla like default configuration
- Custom enchantment level limit
- Custom enchant restrictions (allow unsafe enchantment only for a group of item or create new restriction)
- Custom items of unit repairs (repair damaged with unit of "material", for example the repair of diamond sword by diamonds)
- Custom XP cost for every aspect of the anvil
- Permissions to bypass level limit or enchantment restriction.
---
### Permissions:
```yml
ca.affected: Player with this permission will be affected by the plugin
ca.bypass.fuse: Allow player to combine every enchantments to every item (no custom limit)
ca.bypass.level: Allow player to bypass every level limit (no custom limit)
ca.command.reload: Allow administrator to reload the plugin's configs
```
### Commands
```yml
anvilconfigreload or carl: Reload every config of this plugin
```
---
### Default Configuration:

Default configuration can be found on following links:
- [config.yml](https://github.com/alexcrea/CustomAnvil/blob/master/src/main/resources/config.yml)
- [enchant_conflict.yml](https://github.com/alexcrea/CustomAnvil/blob/master/src/main/resources/enchant_conflict.yml)
- [item_groups.yml](https://github.com/alexcrea/CustomAnvil/blob/master/src/main/resources/item_groups.yml)
- [unit_repair_item.yml](https://github.com/alexcrea/CustomAnvil/blob/master/src/main/resources/unit_repair_item.yml)
---
### Know issue:
There is non known issue, if you find one please report the issue.
