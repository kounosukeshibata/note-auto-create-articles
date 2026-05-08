#!/bin/bash
set -e
SUPABASE_URL=$(gcloud secrets versions access latest \
  --secret=SUPABASE_URL \
  --project=project-384b1a9e-de04-4629-b84)
SUPABASE_KEY=$(gcloud secrets versions access latest \
  --secret=SUPABASE_SERVICE_ROLE_KEY \
  --project=project-384b1a9e-de04-4629-b84)
exec npx -y @supabase/mcp-server-supabase \
  --supabase-url "$SUPABASE_URL" \
  --supabase-key "$SUPABASE_KEY"
