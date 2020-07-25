/*
 * Shim for the fling API which sets a bearer token for every request
 */
import * as fc from '@fling/flingclient';

/*
 * Construct a client configuration with either the given token, or, if token is
 * undefined or null, token retrieved from the session storage.
 */
let clientConfig = (token) => {
  let config = new fc.ApiClient();
  config.basePath = process.env.REACT_APP_API.replace(/\/+$/, '');

  token = token || sessionStorage.getItem('token');
  if (token) { config.authentications['bearer'].accessToken = token; }

  return config;
};

function FlingClient(token) {
  return new fc.FlingApi(clientConfig(token));
}

function ArtifactClient(token) {
  return new fc.ArtifactApi(clientConfig(token));
}

function AuthClient(token) {
  return new fc.AuthApi(clientConfig(token));
}

export { FlingClient, ArtifactClient, AuthClient, fc };
