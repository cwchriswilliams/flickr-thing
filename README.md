# Flickr Fetcher

Fetches data from Flickr Feed (https://www.flickr.com/services/feeds/docs/photos_public/) and transforms the returned images.

## Installation

Clone the repository

## Usage

### Command Line

Start the Web Service with `clj run-m`

e.g. `clj --PORT 5557`

### Via REPL

Start the service with `(start-service <port>)`

e.g. `(start-service 5557)`

### Run Tests

Run tests with clj -X:test

## Options

- `--PORT` `-p` Port for the server to listen on
