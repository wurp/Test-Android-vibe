---
name: prepare-for-store
description: Prepare the app's Vibe Store listing (name, descriptions, tags). Use when the author wants to publish or share the app with other people.
---

# Prepare the app for the Vibe Store

**The Vibe Store isn't accepting submissions yet.** Say so plainly up front. Explain that you can prepare the listing now so the app is ready, and that until then they can share the test link with friends.

## 1. Write the listing

Draft the listing from what you know about the app, then show it to the author in plain text and ask for changes. Write it in the format F-Droid-compatible stores read:

- `fastlane/metadata/android/en-US/title.txt`: the app name (max 50 characters).
- `fastlane/metadata/android/en-US/short_description.txt`: one sentence (max 80 characters).
- `fastlane/metadata/android/en-US/full_description.txt`: a few short paragraphs: what it does, who it's for, and what it doesn't do. Be honest. Don't promise features it lacks.

## 2. Store details

Create `vibe-store.yml`:

```yaml
category: <one of: Games, Productivity, Health, Education, Lifestyle, Tools, Social, Media, Other>
tags: [<3 to 6 lowercase tags>]
contains_ads: false
uses_internet: <true or false: does the app go online?>
high_risk_permissions: [<any listed under "Permissions that need a warning" in CLAUDE.md, or empty>]
```

Fill `uses_internet` and `high_risk_permissions` by checking `app/src/main/AndroidManifest.xml`, not from memory. The store's review checks them.

## 3. Final check

Before reporting that it's ready:
- Confirm nothing secret is in the repository (see "Secrets" in `CLAUDE.md`).
- Confirm the app opens without crashing on the author's phone. Ask them.

Then push, so the listing is saved.
