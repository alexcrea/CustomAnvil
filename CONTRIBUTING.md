Contributions are welcome ! but need to follow the following guidelines

It is recommended to use IntelliJ for this project, if you do not please
be more careful when it comes to formating.

### Formating
Code format must in general follow project style, but can sometimes locally differ.

Project style mostly follow the default java and kotlin style provided by IntelliJ 
with some additional change.
It is recommended to use the IntelliJ reformater with default keybind `CTRL + ALT + L`.

It's also recommended to use block guard when possible over tabulation.

### Nullability
This project use JetBrains nullability annotations to avoid NPE.

All new Java files must be annotated by `@NotNullByDefault`. \
And nullable values and function parameter should be annotated by `@Nullable`.

This allows IntelliJ to warn about nullability issues, all of these must get addressed.

`@UnknownNullability` must be used rarely,
most of the time for not-null elements which you cannot assert are not null.

### Translation
See the [Translation doc](doc/TRANSLATE.md) and the [AI guidelines](#in-translation).

### License
Just like paper and spigot, this project use the GPL-3 licence. \
If you wish for all of your code to ALSO be licensed under MIT please tell me so in a PR, 
a dedicated section will be created.

## AI Usage
AI is a tool, that must be disclosed and not be the only creator of contributions unless specified in one of the following sections.

AI agents are NOT allowed to create PR, issue or comment by themselves
or by a meat proxy, as in a human that just copy and paste the AI.

It's the job of the managing human to remove useless information provided by the AI,
like implementation details visible in the code, and keep the architectural decisions
(keep the "why" not the "how" as it should be in the PR).

It is also the responsibility of the human in charge to ensure that the AI generation is compatible with the GPL-3.0 licence.

Disclosure of what the AI did must be included in the PR discussion. 

### In code
AI is allowed without restriction for some trivial stuff
For example, simple explanations
- Fetching documentation/creating small code
(example: "what is the equivalent of 'dict()' from python in java").
- "What does this part of the code do"

Things like very small "trivial" functions are also allowed.

AI generated code should NOT have overly descriptive comments. \
Function and class Javadoc should be provided only on "complex" and API facing code.

Comments are still welcomed to explain the "why" of the code,
when intention cannot be clear enough with just the code by itself.

### In translation

AI translations must be specified in the translation file name by suffixing the language code with `_ai`. \
More precisely if there is a translation, for example french (ISO 639 'fr') using AI,  
then it must be placed inside `fr_ai.yml`.
`fr.yml`, the non AI translation file is reserved for human contributions.

Obviously this only applies if you wish to commit the translation.
You are free to do whatever you want on your local server.

More instruction about how to translate is present [here](doc/TRANSLATE.md).