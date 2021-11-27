# Flickr Fetcher

[![CircleCI](https://circleci.com/gh/cwchriswilliams/flickr-thing/tree/main.svg?style=svg)](https://circleci.com/gh/cwchriswilliams/flickr-thing/tree/main)

Fetches data from Flickr Feed (https://www.flickr.com/services/feeds/docs/photos_public/) and transforms the returned images.

## Installation

Clone the repository

## Usage

**Important**: The default configuration will save images to `<current-directory>/tmp/`. (See options for details about configuration)

### Starting the server

#### Command Line

Start the Web Service with `clj -M:run-m`

e.g. `clj -M:run-m --PORT 5557 --config ./resources/appsettings.json`

#### Via REPL

Start the service with `(start-service <port>)`

e.g. `(start-service 5557)`

Stop the service with `(stop-service)`

Restart the service with `(restart-service)`

Reload the configuration file with `(reload-config)`

#### Via Docker

Build the docker image as normal with <container-name>

```bsh
docker run -it -p 127.0.0.1:<port>:5050/tcp -v <output-path>:/usr/src/app/tmp --rm --name <image-name> <container-name>
```

**Note**: Currently this uses the default path to config of ./resources/appsettings.json (TODO: make configurable)

### Options

- `--PORT` `-p` Port for the server to listen on
- `--config` `-c` Path to the configuration file to use. A sample is provided in ./resources/appsettings.json
- `--help` `-h` Display usage text

### Endpoints

A single endpoint is provided.

| Verb   | Path      | Parameters         |
| ------ | --------- |--------------------|
| POST   | /photos   | <ul><li>take: Number of images to download</li><li>resize-spec: (see below)</li></ul> |

#### resize-spec

A resize-spec can be provided containing the following fields

| Name            | Type |
| --------------- | ---- |
| maintain-ratio? | bool |
| height          | int  |
| width           | int  |

- If maintain-ratio? is not provided assumed to be false
- height-only, width-only and height and wdth are all valid


curl example:

```bsh
curl --header "Accept: text/plain" --header "Content-Type: application/json" -XPOST http://localhost:5556/photos --data '{"take":7}'
```

json example
```json
{
    "take": 3,
    "resize-spec": {
        "maintain-ratio?": false,
        "height": 800,
        "width": 50
    }
}
```

### Run Tests

Run tests with clj -X:test


# Disclaimer

This grabs data from a public feed that anyone can access. No responsibility is taken for any images which are downloaded.