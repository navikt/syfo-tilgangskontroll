export LDAP_USERNAME=$(cat /secrets/ldap/ldap/username)
export LDAP_PASSWORD=$(cat /secrets/ldap/ldap/password)
export AAD_CLIENT_ID=$(cat /var/run/secrets/nais.io/azuread/client_id)
export AAD_CLIENT_SECRET=$(cat /var/run/secrets/nais.io/azuread/client_secret)