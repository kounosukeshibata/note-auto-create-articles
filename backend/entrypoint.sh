#!/bin/sh
# If the JSON secret is mounted as a file, export each key-value as env var
SECRET_FILE="/run/secrets/config"
if [ -f "$SECRET_FILE" ]; then
  eval "$(jq -r 'to_entries[] | "export \(.key)=\(.value | @sh)"' "$SECRET_FILE")"
fi
exec java -jar app.jar
