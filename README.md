This fork aim to add functionality to the base plugin.
I will rework the readme later.

# UnsafeEnchants

UnsafeEnchants is a fully configurable plugin for bukkit, spigot, and paper minecraft servers allowing custom enchantment limits and removing combination restrictions.

---

### Download Locations:

Direct: https://github.com/DelilahEve/UnsafeEnchants/releases/latest

Spigot: https://www.spigotmc.org/resources/unsafe-enchants.104708/

CurseForge: https://www.curseforge.com/minecraft/bukkit-plugins/unsafe-enchants/files/all

### Permissions:
```yml
ue.unsafe: Allows combining of unsafe enchantments
```

### Default Configuration:
```yml
# Default limit to apply to any enchants missing from override_limits
#
# Valid range of 1 - 255
default_limit: 10

# Whether enchants should be combined without regard for conflicts by default
#
# This setting will override permissions, if a player has ue.unsafe but this is false
# they will be unable to combine conflicting enchantments
#
# i.e. Protection and Blast Protection can be on the same piece of armour
allow_unsafe: true

# Whether all anvil actions should be capped to the vanilla repair limit (40 levels)
limit_repair_cost: true

# Whether the anvil's repair limit should be removed entirely
#
# The anvil will still visually display "too expensive" however the action will be completable
remove_repair_limit: false

# Override limits for specific enchants
#
# Enchantments not listed here will use the value of default_limit
#
# Overrides provided default to 1 in vanilla and won't change effect with extra levels
# with exceptions to this rule having their own comment
#
# Valid range of 1 - 255 for each enchantment
enchant_limits:
  aqua_affinity: 1
  binding_curse: 1
  channeling: 1
  flame: 1
  infinity: 1
  mending: 1
  multishot: 1
  silk_touch: 1
  vanishing_curse: 1
  depth_strider: 3 # anything more than 3 is treated as 3 by the game
#  bane_of_arthropods: 1
#  blast_protection: 1
#  efficiency: 1
#  feather_falling: 1
#  fire_aspect: 1
#  fire_protection: 1
#  fortune: 1
#  frost_walker: 1
#  impaling: 1
#  knockback: 1
#  looting: 1
#  loyalty: 1
#  luck_of_the_sea: 1
#  lure: 1
#  piercing: 1
#  power: 1
#  projectile_protection: 1
#  protection: 1
#  punch: 1
#  quick_charge: 1
#  respiration: 1
#  riptide: 1
#  sharpness: 1
#  smite: 1
#  soul_speed: 1
#  sweeping: 1
#  swift_sneak: 1
#  thorns: 1
#  unbreaking: 1

# Multipliers used to calculate the enchantment's value in repair/combining
#
# Values here are pulled from the fandom wiki:
# https://minecraft.fandom.com/wiki/Anvil_mechanics#Costs_for_combining_enchantments
#
# If an enchantment is missing values here, or is less than 0, it will default to 0
#
# Calculated as: [Enchantment lvl] * [multiplier]
#
# With default values protection 4 would have a value of 4 when
# coming from either a book (4 * 1) or an item (4 * 1)
enchant_values:
  aqua_affinity:
    item: 4
    book: 2
  bane_of_arthropods:
    item: 2
    book: 1
  binding_curse:
    item: 8
    book: 4
  blast_protection:
    item: 4
    book: 2
  channeling:
    item: 8
    book: 4
  depth_strider:
    item: 4
    book: 2
  efficiency:
    item: 1
    book: 1
  flame:
    item: 4
    book: 2
  feather_falling:
    item: 2
    book: 1
  fire_aspect:
    item: 4
    book: 2
  fire_protection:
    item: 2
    book: 1
  fortune:
    item: 4
    book: 2
  frost_walker:
    item: 4
    book: 2
  impaling:
    item: 4
    book: 2
  infinity:
    item: 8
    book: 4
  knockback:
    item: 2
    book: 1
  looting:
    item: 4
    book: 2
  loyalty:
    item: 1
    book: 1
  luck_of_the_sea:
    item: 4
    book: 2
  lure:
    item: 4
    book: 2
  mending:
    item: 4
    book: 2
  multishot:
    item: 4
    book: 2
  piercing:
    item: 1
    book: 1
  power:
    item: 1
    book: 1
  projectile_protection:
    item: 2
    book: 1
  protection:
    item: 1
    book: 1
  punch:
    item: 4
    book: 2
  quick_charge:
    item: 2
    book: 1
  respiration:
    item: 4
    book: 2
  riptide:
    item: 4
    book: 2
  silk_touch:
    item: 8
    book: 4
  sharpness:
    item: 1
    book: 1
  smite:
    item: 2
    book: 1
  soul_speed:
    item: 8
    book: 4
  swift_sneak:
    item: 8
    book: 4
  sweeping:
    item: 4
    book: 2
  thorns:
    item: 8
    book: 4
  unbreaking:
    item: 2
    book: 1
  vanishing_curse:
    item: 8
    book: 4

# Whether to show debug logging
debug_log: false
```
