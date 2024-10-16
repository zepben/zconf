# Zconf

Zepben application configuration tool

## Usage

### CLI

```text
zconf.kexe generate <--source "SOURCE"> <--output "OUTPUT">

# example
zconf.kexe generate --source "env-blob://TEST_ENV" --output ./config.json
```

The generate command will take one or more sources and generate an output file.

- `--source <source>` - A source string in the form of `source-type://params`. See [supported source types](#supported-source-types). Can be repeated more than once, subsequent uses will override the first config source.
- `--source-env <env>` - The name of the environment variable that contains a comma seprate list of sources.
- `--output <path/to/file>` - Absolute path to file the final config JSON will be written. Full path must exist.

### Integrating with Docker images

In order to integrate with the Docker container of a downstream Zepben application, reference the Zconf container in a multi stage build and copy the executable across.

```dockerfile
# Reference the images
FROM ghcr.io/zepben/conf:latest AS zconf

# Final dockerfile
FROM docker.io/library/amazoncorretto:11-al2023

# Required dependency in Amazon Linux 2023
RUN yum update && yum install -y libxcrypt-compat


# In the final image, copy the executable accross
COPY --from=zconf /zconf /bin/zconf
```

Once the executable is present, you can call it in the `entrypoint.sh` script to generate the configuration and output it to the appropriate directory for your application.

#### Supported Source Types

The follow are sources in the form of `source-type://params`

- `env-blob://ENV` - Represents an environment variable (the param) which contains a Base64 encoded JSON
- `env-blob-gz://ENV` - Represent an environment variable (the param) which contains a Gzip'd Base64 encoded JSON
- `env-prefix://PREFIX` - Fetches all environment variables that start with a prefix (the param). The prefix must be followed by two underscores. Any config key that has an underscore can be separated by double underscores. For example: "PREFIX__auth_client__secret=123" whould become "auth.client_secret=123", while  "PREFIX__auth_clientSecret=123" whould become "auth.clientSecret=123". Any casing after the prefix is preserved.
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

## About

### Motivation

Zepben tools run in a variety of environments, from the local environment, containers, bare metal machines and more. Each of these runtimes have different preferred ways of being configured, with each configuration item having different secuirty requirements, ie. secrets. Additionally, the configuration required for these applications can be very complex and large and each application also seems to implement its configuration in different ways. Finally, applications that are part of the platform can be written in any language, which makes it difficult to simply have a Zepben configuration library.

Given the many problems, it was decided that we would require that all applications implement their config as single JSON file (this JSON file may reference other JSON files). Then it would be up to an external tool (Zconf!) to somehow derive the JSON file from any platform specific storage.

### Architecture

Zconf's role is to unite configuration stored in multiple sources, merge them together and emit them as a single config document (JSON). Therefore it is broken down into those three distinct stages

#### Sources

Zconfig can take one more sources, known as a `SourceType`. Each source has a processor that allows zconf to take the parameters provided that source and extract all of the configuration values. Each processor creates an intermediate representation of the config for that source. Supporting more config sources is as simple as adding a new source type and a corresponding processor.

#### Intermediate Representation

The intermediate representation is a recursive data structure that can be indexed using a language that is similar to JSON path. There are three elements

- `ConfigObject` - similar to a JSON object, a map of configuration
- `ConfigArray` - similar to a JSON array, implemented as a ConfigObject with numerical keys
- `ConfigValue` - similar to a JSON primitive, a leaf node in the structure. Contains a string or null

Given a root config, we are able to index through it:

```kotlin
val root = ConfigObject()

root["foo.bar.0"] = "1" // Json document is now { "foo" : { "bar": ["1"] }}
```

Once all sources are in this intermediate form, it is trivial to merge configuration values together.

#### Output

The intermediate representation can be converted into KotlinX JSON and written to a file. More storage formats can be supported in the future if required.
