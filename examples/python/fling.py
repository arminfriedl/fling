import flingclient as fc
from flingclient.rest import ApiException
from datetime import datetime

# Per default the dockerized fling service runs on localhost:3000 In case you
# run your own instance, change the base url
configuration = fc.Configuration(host="http://localhost:3000")

# Every call, with the exception of `/api/auth`, is has to be authorized by a
# bearer token. Get a token by authenticating as admin and set it into the
# configuration. All subsequent calls will send this token in the header as
# `Authorization: Bearer <token> header`
def authenticate(admin_user, admin_password):
  with fc.ApiClient(configuration) as api_client:
    auth_client = fc.AuthApi(api_client)
    admin_auth = fc.AdminAuth(admin_user, admin_password)
    configuration.access_token = auth_client.authenticate_owner(admin_auth=admin_auth)

admin_user = input("Username: ")
admin_password = input("Password: ")
authenticate(admin_user, admin_password)

with fc.ApiClient(configuration) as api_client:
  # Create a new fling
  fling_client = fc.FlingApi(api_client)
  fling = fc.Fling(name="A Fling from Python", auth_code="secret",
                   direct_download=False, allow_upload=True,
                   expiration_time=datetime(2099, 12, 12))
  fling = fling_client.post_fling()
  print(f"Created a new fling: {fling}")

  #
