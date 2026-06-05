# VisibilityToggle Modernization Plan (1.16 - 1.21.x)

## Objective
Refactor VisibilityToggle from its native 1.16 version to work optimally on 1.21.x while maintaining maximum backward compatibility. Restructure the project under the new package `net.redm1ne.visibilitytogglered`.

---

## Step 1: Package Refactoring

- **Old base package:** `dev.cleusgamer201.visibilitytoggle`
- **New base package:** `net.redm1ne.visibilitytogglered`
- Create new directory structure: `src/net/redm1ne/visibilitytogglered/`
- Update `package` declarations in every Java file
- Update all internal `import` statements referencing the old package
- Update `plugin.yml` `main:` field to `net.redm1ne.visibilitytogglered.Main`
- Delete old `src/dev/` directory tree when done

## Step 2: Build System -- pom.xml

- Create `pom.xml` with:
  - `groupId`: `net.redm1ne`, `artifactId`: `visibilitytogglered`, `version`: `4.0.0`
  - Java 21 (`source`/`target`/`release` = 21)
  - Spigot API `1.21.4-R0.1-SNAPSHOT` (compile against latest, runtime compatible with 1.16+)
  - MySQL Connector/J 8.x as `provided` scope
  - `maven-compiler-plugin` (Java 21)
  - `maven-shade-plugin` for fat JAR
  - Spigot repository at `https://hub.spigotmc.org/nexus/content/repositories/snapshots/`

## Step 3: Eliminate NMS -- TitleAPI

- **Delete** `TitleAPI.java` entirely (uses `net.minecraft.server.*` reflection -- broken since 1.17)
- Replace all `TitleAPI.sendTitle(player, title)` / `TitleAPI.sendSubTitle(player, subtitle)` calls in `Main.java` with Bukkit's standard API:
  ```java
  player.sendTitle(title, subtitle, 10, 70, 20);
  ```
  Available since 1.11, works on 1.16+.
- Remove `TitleAPI` imports from `Main.java`

## Step 4: Eliminate UMaterial

- **Delete** `UMaterial.java` entirely (1692 lines):
  - Uses `org.bukkit.material.Leaves`, `Sapling`, `WoodenStep`, `Banner` -- deprecated in 1.13, **removed** in 1.20.5+
  - Uses `org.bukkit.potion.Potion` -- **removed** in 1.20.5+
  - The plugin only uses UMaterial through `ItemBuilder` for 3 toggle items
- **Refactor `ItemBuilder.java`:**
  - Remove UMaterial dependency
  - Accept `Material` enum directly or `String` resolved via `Material.matchMaterial()`
  - Add legacy material name mapping for `INK_SACK` with data values (pre-1.13)
- **Update `Config.yml`:**
  - Replace `Item: INK_SACK` with modern material names: `LIME_DYE`, `PINK_DYE`, `GRAY_DYE`
  - Remove `Data:` field (data values eliminated in 1.13+)
- **Add legacy material resolver in `loadConfigValues()`:**
  - If `Material.matchMaterial()` returns null, attempt fallback mapping for common legacy names

## Step 5: Fix showPlayer/hidePlayer Compatibility

- In 1.16.2+, `showPlayer(Player)` and `hidePlayer(Player)` were deprecated in favor of `showPlayer(Plugin, Player)` and `hidePlayer(Plugin, Player)`
- Update all calls in `Main.java`:
  - `p.showPlayer(a)` → `p.showPlayer(Main.getInstance(), a)`
  - `p.hidePlayer(a)` → `p.hidePlayer(Main.getInstance(), a)`
- The old signature still compiles against 1.16, so this is backward compatible

## Step 6: Fix MySQL Driver

- `DBSettings.java`: Change `com.mysql.jdbc.Driver` → `com.mysql.cj.jdbc.Driver`
  - Old driver class was deprecated in MySQL Connector/J 5.1 and removed in 8.x
  - Add fallback: try new driver first, then old driver for ancient MySQL Connector versions

## Step 7: Bug Fixes

| Location | Bug | Fix |
|---|---|---|
| `Cache.java:31` | Method typo `setVisbility` | Rename to `setVisibility` and update all call sites |
| `VisibilityToggleAPI.java` | `forceToggleVisibility()` delegates to `plugin.toggleVisibility()` instead of `plugin.forceToggleVisibility()` | Delegate to correct method |
| `Commands.java` (Reload) | Malformed parentheses: `item.isSimilar(plugin.getOnItem()) \|\| item.isSimilar(plugin.getRankItem())) \|\| item.isSimilar(plugin.getOffItem())` | Fix grouping: `(item.isSimilar(on) \|\| item.isSimilar(rank) \|\| item.isSimilar(off))` |
| `Config.yml:1` | Typo `Visbility` in Prefix | Fix to `Visibility` |
| `Utils.java` | Duplicate line `Text = Text.replaceAll("<u>", "ú");` | Remove duplicate |

## Step 8: Plugin Configuration Updates

- **plugin.yml:**
  - `main`: `net.redm1ne.visibilitytogglered.Main`
  - `version`: `4.0`
  - `api-version`: `1.16` (keeps backward compat, signals modern behavior)
- **Config.yml:**
  - Fix `Visbility` → `Visibility` in Prefix
  - Replace `INK_SACK` items with modern dye names
  - Remove `Data` fields
  - Add comments noting 1.13+ material names are supported

## Step 9: Cleanup

- Delete `src/dev/` entire directory tree after migration
- Delete `TitleAPI.java` (replaced by Bukkit API)
- Delete `UMaterial.java` (replaced by direct Material usage)
- Ensure no references to old package remain in any file
