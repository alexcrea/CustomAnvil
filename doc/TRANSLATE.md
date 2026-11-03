
At this time, only the English language `en` is available.

You can change the current language inside `plugins/CustomAnvil/config.yml`.

If you like to add a language you can add it inside `plugins/CustomAnvil/lang/langid.yml`
where `langid` is the language id put inside config.yml. `lang` folder do not exist by default.

English translation file containing all the translation key is available 
[Here](../src/main/resources/lang/en.yml)

In your translation file you should only keep translated key. no original English should be present. \
This is to preserve potential change in the English text if it does get updated

If you wish to use AI for your translation, you can, but don't expect good/perfect result.

You can share your translation: you can commit a pr with your file in `src/main/resources/lang/`. \
Please note that if you used AI you need to follow the [translation AI Guideline](../CONTRIBUTING.md#in-translation)

Some tools are available in game for easier:
- `/ca debug lang details` to get potential issues
- `/ca reload` to update the translation file
