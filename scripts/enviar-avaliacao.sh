#!/usr/bin/env bash
# Uso: ./scripts/enviar-avaliacao.sh <URL_API> <API_KEY> "<descricao>" <nota>
# Exemplo: ./scripts/enviar-avaliacao.sh https://xxxx.execute-api.us-east-1.amazonaws.com/dev/avaliacao abc123 "Aula confusa" 2
set -euo pipefail
URL="$1"; CHAVE="$2"; DESCRICAO="$3"; NOTA="$4"

curl -sS -i -X POST "$URL" \
  -H "Content-Type: application/json" \
  -H "x-api-key: $CHAVE" \
  -d "{\"descricao\": \"$DESCRICAO\", \"nota\": $NOTA}"
echo
