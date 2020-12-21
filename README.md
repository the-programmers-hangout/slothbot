# slothbot

A Discord bot to manage slow mode on channels.

## Development

Running locally:
```console
$ SB_TOKEN=<your discord token> ./gradlew run
```

`.env` files are supported and an option for running locally.
For example if your `.env` file looks as below, running can simply be `./gradlew run`.

```
SB_TOKEN=<your discord token>
SB_LOG_LEVEL=debug
SB_PREFIX=sbtest!
```

Code formatting:

```console
$ ./gradlew spotlessApply
```

[Emoji log](https://github.com/ahmadawais/Emoji-Log) or
[tsktsk](https://github.com/princesslana/tsktsk) should be used for commit messages.

## Docker

To build and run the Docker container locally:

```console
$ docker build . -t slothbot:latest
$ docker run -e SB_TOKEN=<your discord token> slothbot:latest
```

### Cofiguration

  * `SB_TOKEN` [required] the Discord bot token to run under
  * `SB_PREFIX` [defaults to sb!] the prefix for the bot the listen to
  * `SB_LOG_LEVEL` [defaults to info] logging level

### Data

Data is stored within the container under the `/var/data` folder.
A volume or mount should be configured for this location to persist data between runs.
