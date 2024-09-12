# Zconf

Zepben application configuration tool

## Usage

### Generating a config

```text
zconf.kexe generate <--source "SOURCE"> <--output "OUTPUT">

# example
zconf.kexe generate --source "env-blob://TEST_ENV" --output ./config.json
```

The generate command will take one or more sources and generate an output file.

- `--source` - A source string in the form of `source-type://params`. See [supported source types](#supported-source-types). Can be repeated more than once, subsequent uses will override the first config source.
- `--output` - Absolute path to file the final config JSON will be written. Full path must exist.

#### Supported Source Types

The follow are sources in the form of `source-type://params`

- `env-blob://ENV` - Represents an environment variable (the param) which contains a Base64 encoded JSON
- `env-blob-gz://ENV` - Represent an environment variable (the param) which contains a Gzip'd Base64 encoded JSON
- `null://` - Represent a null source. No parameter. Used as fallback if the source is unparsable.

## Development

You can open this in Intellij as a `gradle` project or run the following in a Terminal

```shell
# Linux
./gradlew build

# Windows
./gradlew.bat build
```

After this completes, you coulld find debug/test/release executable in the`build/bin/native` folder.
