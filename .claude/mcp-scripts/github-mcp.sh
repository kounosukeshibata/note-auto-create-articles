#!/bin/bash
set -e
export GITHUB_PERSONAL_ACCESS_TOKEN=$(gcloud secrets versions access latest \
  --secret=GITHUB_TOKEN \
  --project=project-384b1a9e-de04-4629-b84)
exec npx -y @modelcontextprotocol/server-github
