# slothbot

A Discord bot to manage slow mode on channels.

## Docker

### Cofiguration

  * `SB_TOKEN` [required] the Discord bot token to run under
  * `SB_PREFIX` [defaults to sb!] the prefix for the bot the listen to
  * `SB_LOG_LEVEL` [defaults to info] logging level

### Data

Data is stored within the container under the `/var/data` folder.
A volume or mount should be configured for this location to persist data between runs.
