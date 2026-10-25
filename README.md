# Vibe Dev app template

Make your own Android app by describing it to Claude. No programming needed.

You tell Claude what you want, in plain words. Claude writes the app, and GitHub builds it. A few minutes later you can install the new version on your phone from a link that never changes.

> **Early test version.** This is a prototype of Vibe Dev. Expect rough edges.

## What you need

- An Android phone.
- A **Claude Pro or Max** plan. Free plans can't use Claude Code.
- A free **GitHub** account. You don't need to learn GitHub; it just stores and builds your app.

## Set up (once, about 20 minutes)

1. **Create a GitHub account** at [github.com/signup](https://github.com/signup), if you don't have one.
2. **Copy this template:** tap **Use this template → Create a new repository** at the top of this page. Give it a short name (for example, `plant-diary`), leave it **Public**, and tap **Create repository**.
3. **Connect GitHub to Claude:**
   1. Go to [claude.ai/code](https://claude.ai/code), and connect your GitHub account when asked.
   2. Install the Claude GitHub app and choose **Only select repositories**, picking your new one.
4. **Start your app:** in the Claude app's **Code** tab (or at claude.ai/code), pick your new repository and say what you want to make, for example "a diary for my houseplants, with a reminder to water them."
5. **Install it:** when Claude says it's ready, open the link Claude gives you on your phone. Android will ask to allow the install the first time. That's normal for apps not from the Play Store.

After that, just tell Claude what to change. Each time, Claude gives you the same link, and the new version installs over the old one.

## If something goes wrong

- **The app crashed:** open it again. It shows a **Copy for Claude** button. Tap it and paste the result to Claude.
- **"App not installed" when updating:** tell Claude. Don't uninstall unless Claude says to, because uninstalling deletes the app's data.

## Optional: faster checks

If you add `dl.google.com` to your Claude environment's network settings (claude.ai/code → environment → **Network access: Custom**, with "include defaults" checked), Claude can check each change before sending it to GitHub. That catches mistakes sooner. Everything works without it.

---

<details>
<summary>For developers: how this works</summary>

- **`.github/workflows/build.yml`:** builds a debug APK on every push to any branch and republishes it as the `test-build` prerelease, at `releases/download/test-build/app.apk`.
- **Signing:** `keystore/debug.keystore` is committed, so every build has the same signer. `versionCode` is the Actions run number.
- **`app.properties`:** holds `applicationId` and `appName`, so the source namespace (`com.vibedev.app`) never changes.
- **`CLAUDE.md` and `.claude/skills/`:** tell Claude to work for a non-programmer: no git jargon, push and watch CI, and hand over the link.
- **`.claude/hooks/session-start.sh`:** in cloud sessions, installs the Android SDK if `dl.google.com` is reachable. Builds there use the preinstalled `gradle`, because `./gradlew` can't download Gradle through the session proxy.
- **Versions:** AGP 8.13 and Gradle 8.14.3 to match Claude Code cloud sessions, with AndroidX versions held back to match. See `gradle/libs.versions.toml`.
- **Crash capture:** debug builds save uncaught exceptions and show them on the next launch (`CrashCatcher.kt`).
- **Untested assumptions:**
  - `gh run watch` works through the cloud session's GitHub proxy.
  - Repo `SessionStart` hooks run in cloud sessions.
  - Catching up via the `test-build` tag works across session branches.

</details>
