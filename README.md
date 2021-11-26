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

### Options

- `--PORT` `-p` Port for the server to listen on
- `--config` `-c` Path to the configuration file to use. A sample is provided in ./resources/appsettings.json

### Endpoints

A single endpoint is provided.

| Verb   | Path      | Parameters         |
| ------ | --------- |--------------------|
| POST   | /photos   | *none*             |


### Run Tests

Run tests with clj -X:test


# Disclaimer

This grabs data from a public feed that anyone can access. No responsibility is taken for any images which are downloaded.