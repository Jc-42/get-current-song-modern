# Get Current Song

This is a simple FabricMC mod that allows you to get the current playing song in Minecraft via a command. Updated for 1.19+ [Original Version](https://modrinth.com/mod/getcurrentsong)

## Commands

This mod adds two commands to the game:

+ `/getsong` &mdash; Gets information about the currently playing song, such as the name and composer.
+ `/getsongid` &mdash; Gets the [namespaced ID](https://minecraft.fandom.com/wiki/Namespaced_ID) of the currently playing song.

However, instead of using these commands, you can see the same information on the [F3 screen](https://minecraft.fandom.com/wiki/Debug_screen).

## API

This mod provides a resource-based method of getting information about the songs in Minecraft, such as name, composer, album, and track number.

### Resource pack developers

If your resource pack adds custom music to the game, consider adding your song(s) to the `getcurrentsong/song_names.json` file in your resource pack. This JSON file has one root object, with keys being the song IDs and values being an object whose form is described below:

```json
{
    "name": "song name (string, required)",
    "composer": "composer's name (string, required)",
    "soundtrack": "album name (string, optional)",
    "trackNumber": "track number in album (integer, optional)"
}
```

If the soundtrack is specified, the track number must also be specified, and vice-versa.

## Mod develepers

To include this mod's API with Gradle, add the following to your `build.gradle` (code adapted from the [Modrinth Maven docs](https://docs.modrinth.com/docs/tutorials/maven/)):

```gradle
repositories {
    // Other Maven repositories can go above (or below) Modrinth's. We don't need priority :)
    // Do remember, though, this is the `repositories` block below where plugins are declared, not in `pluginManagement`!
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
        content {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    modApi "maven.modrinth:getcurrentsong:1.0.3"
}
```

Once you have added the API to your `build.gradle`, you can load song information from resource packs using the following API (the `SongNameDatabase` class is in the `io.github.gaming32.getcurrentsong` package):

+ `SongNameDatabase#isInitialized` &mdash; Return `true` if the song info is currently in a loaded state. Attempting to read from the database when this returns `false` may not return correct data. You have been warned!
+ `SongNameDatabase#getSong` &mdash; Gets the current song by name or `net.minecraft.util.Identifier` and returns a `SongNameDatabase.SongInfo` object or `null` if the song wasn't found in the database.
