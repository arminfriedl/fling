import * as fc from '@fling/flingclient';

let clientConfig = (token) => {
  let config = new fc.ApiClient();
  config.basePath = process.env.REACT_APP_API.replace(/\/+$/, '');

  token = token || sessionStorage.getItem('token');
  if(token) { config.authentications['bearer'].accessToken = token; }

  return config;
};

function FlingClient(token) {
  return new fc.FlingApi(clientConfig(token));
}

function ArtifactClient(token) {
  return new fc.ArtifactApi(clientConfig(token));
}

function AuthClient() {
  return new fc.AuthApi(clientConfig());
}

export {FlingClient, ArtifactClient, AuthClient, fc};
