[![Build Status](https://drone.friedl.net/api/badges/incubator/fling/status.svg)](https://drone.friedl.net/incubator/fling)

# Fling
Fling is a self-hosted file share. It is simple like USB without missing out on
the good parts of a web app:
- Just drop files on a Fling and share the URL
- Choose your own name for your share-URL
- Share a direct download link
- Let others upload files
- Protect your fling by a password - no registration required
- Let a fling expire after a date or a number of clicks

# API
Fling is distributed as both, a backend service and a web interface. You can use
the backend on its own with any [HTTP client](examples).

Per default Fling publishes a Swagger UI page and an OpenAPI spec. You can find
them here:

``` http
http://<host>:<port>/swagger-ui.html
http://<host>:<port>/v3/api-docs
```

If starting the fling container locally, the default `<host>:<port>` is
http://localhost:3000. You can also find a recent version of it via
https://fling.friedl.net/swagger-ui.html and
https://fling.friedl.net/v3/api-docs.

# Starting Fling from Docker
A Fling container is provided at https://hub.docker.com/repository/docker/arminfriedl/fling. 

1. Run `docker run --rm -p3000:3000 arminfriedl/fling` 
2. Go to the default http://localhost:3000
3. Log in with `adminName:adminPassword`.

## Configuring Fling
The Fling container can be configured by environment variables.

The web interface configuration ([config.js](web/fling/public/config.js)/[config.js.template](container/var/www/fling/config.js.template)) will be
filled by `envsubst` when the container starts up.
``` sh
# The base URL of the Fling API service
FLING_API_BASE=http://localhost:3000
# Log level of the application
FLING_LOG_LEVEL=warn
# Max. upload size in bytes. Checked on client side.
FLING_FILESIZE=209715200
```

The Fling service configuration is a standard spring configuration. It can be
set by environement variables or any other [configuration externalization supported by spring boot]
(https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)).
Refer to the [application-prod.yml](service/fling/src/main/resources/application-prod.yml) for configuration options.
