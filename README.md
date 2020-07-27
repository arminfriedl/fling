[![Build Status](https://drone.friedl.net/api/badges/incubator/fling/status.svg)](https://drone.friedl.net/incubator/fling)

# Fling
Have you ever missed the simplicity of an USB Stick when sharing data over the
net?

Zero-friction sharing is a surprisingly unsolved problem. There's no reasonably
ubiquitous solution installed on everybody's machine. Online providers are often
packed with features, but miss out on things like direct download urls, a space
for others to easily _upload_ to you, or require registration from all
participants. I don't remember any of these things being a problem with USB 🤔.

Fling is a self-hosted file share. It is simple like USB without missing out on
the good parts of the web:
- Drop files on a fling and share the URL. That's it. The fling way of life.

Other features include:
- Choose your own, meaningful name for your sharing URL
- Share a direct download link
- Choose to let others upload files just as simple
- Protect your fling by a password - no registration required
- Let a fling expire after a date or a number of clicks (or keep it forever)

# Fling is a API
It gets even better!

Fling is a backend service and a web interface. But you can use anything else
that speaks HTTP if you prefer. In fact, we generate and publish a javascript
and python client for the Fling API on every build. If you like it bare-bones
there is also a querysheet in the examples folder with raw HTTP calls.

Fling also has a code-first OpenAPI compliant spec. O mon Dieu, it just checks
_all_ the boxes!

# Fling as a container
It gets even even better better!

Fling is self-hosted. But it is packaged up in a container for easy deployment.
Run `docker run --rm -p3000:3000 arminfriedl/fling` and go to
http://localhost:3000. The default admin user is `adminName:adminPassword`.

## Configuring Fling
The Fling container can be configured by environment variables.

Web interface configuration (`config.js`/`config.js.template`; will be replaced
by `envsubst` when the container starts up):

``` sh
# The base URL of the Fling API service
FLING_API_BASE=http://localhost:3000
FLING_LOG_LEVEL=warn
# Max. upload size in bytes
FLING_FILESIZE=209715200
```

Fling service configuration (`application-prod.yml`). Standard spring
configuration, can be set by environement variables or any other
[externalization facilities of spring boot](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)).
Please refer to `application-prod.yml` for configuration possibilites.
