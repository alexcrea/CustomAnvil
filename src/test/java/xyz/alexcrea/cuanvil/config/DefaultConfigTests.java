package xyz.alexcrea.cuanvil.config;

import io.delilaheve.util.ConfigOptions;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.alexcrea.cuanvil.DefaultCustomAnvilTest;
import xyz.alexcrea.cuanvil.enchant.CAEnchantment;
import xyz.alexcrea.cuanvil.enchant.CAEnchantmentRegistry;
import xyz.alexcrea.cuanvil.enchant.EnchantmentRarity;

import java.util.stream.Stream;

public class DefaultConfigTests extends DefaultCustomAnvilTest {

    @ParameterizedTest
    @MethodSource("provideStringsForIsConfiguredValueValid")
    public void isConfiguredValueValid(String path, Object value){
        FileConfiguration config = ConfigHolder.DEFAULT_CONFIG.getConfig();

        Assertions.assertEquals(value.toString(), config.getString(path), "Default value is not the same as in the config file");
    }

    @ParameterizedTest
    @MethodSource("provideForEnchantmentsTests")
    public void testDefaultEnchantLimit(NamespacedKey key){
        CAEnchantment enchantment = CAEnchantmentRegistry.getInstance().getByKey(key);
        Assertions.assertNotNull(enchantment, "Enchantment was somehow not found");

        int levelLimit = ConfigOptions.INSTANCE.enchantLimit(enchantment);
        int mergeLimit = ConfigOptions.INSTANCE.maxBeforeMergeDisabled(enchantment);

        Assertions.assertEquals(enchantment.defaultMaxLevel(), levelLimit,"Default enchantment limit is not the same as expected limit ");

        if(mergeLimit >= 0) { // negative mean default
            Assertions.assertEquals(enchantment.defaultMaxLevel(), mergeLimit,"Default enchantment merge limit is not the same as expected limit ");
        }

    }

    @ParameterizedTest
    @MethodSource("provideForEnchantmentsTests")
    public void testDefaultEnchantValues(NamespacedKey key){
        CAEnchantment enchantment = CAEnchantmentRegistry.getInstance().getByKey(key);
        Assertions.assertNotNull(enchantment, "Enchantment was somehow not found");

        int itemValue = ConfigOptions.INSTANCE.enchantmentValue(enchantment, false);
        int bookValue = ConfigOptions.INSTANCE.enchantmentValue(enchantment, true);

        EnchantmentRarity rarity = enchantment.defaultRarity();

        Assertions.assertEquals(rarity.getItemValue(), itemValue,"Default enchantment item value is not the same as expected value");
        Assertions.assertEquals(rarity.getBookValue(), bookValue,"Default enchantment book value is not the same as expected value");
    }


    private static Stream<Arguments> provideStringsForIsConfiguredValueValid() {
        return Stream.of(
                // Default options
                Arguments.of(ConfigOptions.CAP_ANVIL_COST, ConfigOptions.DEFAULT_CAP_ANVIL_COST),
                Arguments.of(ConfigOptions.CAP_ANVIL_COST, ConfigOptions.DEFAULT_CAP_ANVIL_COST),
                Arguments.of(ConfigOptions.MAX_ANVIL_COST, ConfigOptions.DEFAULT_MAX_ANVIL_COST),
                Arguments.of(ConfigOptions.REMOVE_ANVIL_COST_LIMIT, ConfigOptions.DEFAULT_REMOVE_ANVIL_COST_LIMIT),
                Arguments.of(ConfigOptions.REPLACE_TOO_EXPENSIVE, ConfigOptions.DEFAULT_REPLACE_TOO_EXPENSIVE),
                Arguments.of(ConfigOptions.ITEM_REPAIR_COST, ConfigOptions.DEFAULT_ITEM_REPAIR_COST),
                Arguments.of(ConfigOptions.UNIT_REPAIR_COST, ConfigOptions.DEFAULT_UNIT_REPAIR_COST),
                Arguments.of(ConfigOptions.ITEM_RENAME_COST, ConfigOptions.DEFAULT_ITEM_RENAME_COST),
                Arguments.of(ConfigOptions.SACRIFICE_ILLEGAL_COST, ConfigOptions.DEFAULT_SACRIFICE_ILLEGAL_COST),
                // Color options
                Arguments.of(ConfigOptions.ALLOW_COLOR_CODE, ConfigOptions.DEFAULT_ALLOW_COLOR_CODE),
                Arguments.of(ConfigOptions.ALLOW_HEXADECIMAL_COLOR, ConfigOptions.DEFAULT_ALLOW_HEXADECIMAL_COLOR),
                Arguments.of(ConfigOptions.PERMISSION_NEEDED_FOR_COLOR, ConfigOptions.DEFAULT_PERMISSION_NEEDED_FOR_COLOR),
                Arguments.of(ConfigOptions.USE_OF_COLOR_COST, ConfigOptions.DEFAULT_USE_OF_COLOR_COST)

        );
    }

    // Test every enchantment defaults
    private static Stream<Arguments> provideForEnchantmentsTests() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                .stream().map(enchantment -> Arguments.of(enchantment.getKey()));
    }

}
