[![Build Status](https://travis-ci.org/msprunck/cordula.svg?branch=master)](https://travis-ci.org/msprunck/cordula)

Cordula
==========

HTTP request adapter
<pre>
+-------------+
|  Webhook    |
+-------------+
   |      ^
   v      |
   HTTP POST
   |      ^
   v      |
+-------------+
|  Cordula    |
+-------------+
   |      ^
   v      |
   HTTP GET
   |      ^
   v      |
+-------------+
| Webservice  |
+-------------+
</pre>

## Usage

### Run the application locally

`lein run -- [options]`

### Run the tests

`lein test`

### Packaging and running as standalone jar

```
lein do clean, uberjar
java -jar target/server.jar [options]
```

### Docker

The latest image is available directly from the [Docker Hub](https://hub.docker.com/r/msprunck/cordula/)

You can use `docker-compose.yml` to run the development version with all dependencies.

```
lein do clean, uberjar
docker-compose -f docker/docker-compose.yml build
export CLIENT_SECRET=<YOUR_CLIENT_SECRET>
docker-compose -f docker/docker-compose.yml up
```

## Configuration

`Cordula` can be configured via [command line options](#command-line-options), [system properties](#system-properties) or [environment variables](#environment-variables).

### Command line options

```
Usage of Cordula:
      --help               Show help
      --server-host        which IP to bind
      --server-port        which port listens for incoming requests
      --db-uri             MongoDB URI
      --client-secret      The Client Secret is used to sign the access token
      --client-id          The application's client ID (for Swagger auth)
      --token-name         The access token parameter name returned by the authorization endpoint (for Swagger auth)
      --authorization-url  the API authorization endpoint (for Swagger auth)
      --scope              Specifies the level of access that the application is requesting (for Swagger auth)
```

### System properties

System properties are usually separated by . (periods). `Cordula` will convert these periods to - (dashes). For instance, properties `-Dserver.host` is equivalent to command line option `--server-host`.

### Environment variables

The _ is converted to - by `Cordula`. For instance, ENV variable `SERVER_HOST` is equivalent to command line option `--server-host`.

## Authentication

[Auth0](https://auth0.com/) has been integrated. You will need to register a new application on `Auth0` and configure it with Redirect URI `http://<SERVER_HOST>:<SERVER_PORT>/o2c.html`. You will be able to login using Swagger UI.

## Development
You can use `docker-compose-dev.yml` which will bring up the dependencies you need in containers.

You can then bring up a development environemnt:

```
docker-compose -f docker/docker-compose-dev.yml up
```

## License

Copyright ©  2016 Matthieu Sprunck

Eclipse Public License v1.0
