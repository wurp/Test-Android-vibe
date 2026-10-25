# Test signing key

`debug.keystore` signs **test builds only**. It's committed on purpose. Every build has to be signed with the same key, or Android won't install a new version over the old one.

- **It isn't secret.** The passwords are the Android defaults (`android` / `android`). The Vibe Store signs published apps with its own keys.
- **When the app is first set up,** Claude replaces the template's shared key with one unique to the app (see `.claude/skills/start-app`).
- **After the first build,** never replace or regenerate it. Phones that have the app installed would refuse updates until it's uninstalled, which loses its data.
