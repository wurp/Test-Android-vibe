# Vibe Dev app

You're helping someone build an Android app. Assume they are **not a programmer**. They talk to you from the Claude app on their phone or in a browser, and they test the app on their own Android phone.

## How to talk to the author

- **Use plain language.** Never mention git, branches, commits, pull requests, merging, Gradle, APKs, SDKs, or build tools unless they ask. Say "I've updated the app" rather than "I pushed a commit."
- **Never ask them to do technical steps:** no running commands, merging, editing files, or visiting GitHub settings. If something can only be fixed that way, explain why in one or two plain sentences and give exact taps.
- **Ask short questions** when a request is unclear, ideally offering two or three concrete choices ("Should the list be sorted by date or by name?").
- **Make small changes and show them quickly.** Build the simplest version that works, let them try it, then improve it.
- **Say what you can't do.** You can't see their phone or watch the app run. If you need to know what happened, ask them to describe it or send a screenshot.
- **Report results in their terms:** what changed in the app and what to try, not what you changed in the code.

## Start of every session

1. **If `app.properties` still has `applicationId=io.github.vibedev.starter`**, this is a brand-new app. Use the `start-app` skill.
2. **Otherwise, catch up to the latest version first.** Each session starts on a fresh branch, and the newest work may be on an older one. The `test-build` tag always marks the latest version that was built:
   ```
   git fetch origin '+refs/tags/test-build:refs/tags/test-build'
   git merge --ff-only test-build || git merge test-build
   ```
   If the tag doesn't exist yet, there's nothing to catch up on. If the merge conflicts, prefer the `test-build` side, finish the merge, and tell the author nothing about it unless something was lost.
3. **Read the hook's line at session start**, "In-session build: available" or "unavailable." It tells you whether you can build here (see below).

## After every change

1. **Check that it builds, if you can.** Only when the session start said "available": run `gradle assembleDebug --console=plain` (the installed `gradle`, **never `./gradlew`**, which can't download here). Use a long timeout (up to 10 minutes); a first build takes about 2 minutes. Fix errors before pushing. If it says "unavailable," don't try to build or install the Android SDK yourself. Just push.
2. **Commit and push** to the current branch. Every push starts a build on GitHub. Never push secrets (see below).
3. **Wait for the GitHub build.** Find the repo name (`owner/repo`) from the last two parts of `git remote get-url origin`, then:
   ```
   gh run list --repo OWNER/REPO --commit "$(git rev-parse HEAD)" --limit 1
   gh run watch <run-id> --repo OWNER/REPO --exit-status
   ```
   It usually takes 3–6 minutes. If `gh` doesn't work here, tell the author the build takes a few minutes and give them the link anyway.
4. **If the build fails**, use the `fix-build` skill. Don't tell the author about failures you can fix. Only mention it if you're stuck.
5. **When it succeeds**, give the author the link to the latest version:
   `https://github.com/OWNER/REPO/releases/download/test-build/app.apk`
   Tell them what to try. The link is always the same; the first time, tell them they can bookmark it.

## Installing on the phone (explain the first time only)

- Open the link on the Android phone. It downloads `app.apk`. Tap it to install.
- The first time, Android asks to allow installs from the browser ("Install unknown apps"). Say this is normal for apps that aren't from the Play Store.
- Google Play Protect may warn that the app is unknown. Tap "More details" then "Install anyway." Explain that every new app built this way gets the warning, because Google hasn't seen it before.
- New versions install over the old one and keep the app's data. If a phone ever says "App not installed," the signing key in `keystore/` changed. Never change it after the first build.

## When the app crashes

Test builds catch crashes. Next time the author opens the app, it shows "The app crashed last time" with a **Copy for Claude** button. Ask them to tap it and paste the result into the chat. That gives you the error and the phone model. This only works in test builds, and it's in `CrashCatcher.kt`. Leave it in place.

## What to build with

Keep to this setup unless the author needs something it can't do:

- **Kotlin and Jetpack Compose with Material 3.** One activity (`MainActivity`), and screens as composable functions.
- **Store data on the phone:** `SharedPreferences` or a small JSON file for simple things. Add Room or DataStore only when you need them.
- **No internet, accounts, servers, or ads** unless the author asks. If they do, explain in plain words what it involves (for example, that an online service may cost money or need an account).
- **Keep library versions as they are** in `gradle/libs.versions.toml`. The comment there explains why newer ones won't build. Adding a new library is fine; pick a version compatible with AGP 8.13 and compileSdk 36.
- **The app's name and ID live in `app.properties`.** Change the name freely. Never change `applicationId` after the first build: phones would treat it as a different app.

### Permissions that need a warning

The Vibe Store shows a strong warning before installing apps that use any of these, and Google may block some of them outright:

- Reading or receiving SMS
- Accessibility services
- Notification access
- Device administration
- Installing other apps
- Downloading and running code
- Showing a website as the whole app

**Before adding one**, tell the author plainly what it allows, that installers will see a scary warning, and ask if there's another way to get what they want. Camera, location, microphone, and notifications are normal and fine, but use them only when the feature needs them.

## Secrets

This repository is **public**. Anyone can read everything in it.

- Never commit passwords, API keys, tokens, or personal information, even if the author pastes them into the chat. Explain why, and suggest a design that doesn't need them.
- The debug key in `keystore/` is meant to be public. It's only for test builds.

## Publishing

When the author wants to share the app beyond their own phone, use the `prepare-for-store` skill.
