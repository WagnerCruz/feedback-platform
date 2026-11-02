# Segurança e governança de acesso

## 1. Contas e identidades humanas
- Ative **MFA** no usuário root e **não o utilize** no dia a dia.
- Acesse via **IAM Identity Center** (SSO) com permission sets distintos, por exemplo:
  - `Administrador` (provisiona infraestrutura),
  - `SomenteLeitura` (consulta logs, métricas e dashboard).
- Configure um **AWS Budget** com alerta (ex.: 50%, 80% e 100% dos créditos) enviado por e-mail.

## 2. Identidades de máquina (privilégio mínimo)
Cada função possui sua **própria role**, gerada pelo SAM a partir das políticas do `template.yaml`:

| Função | Permissões |
|---|---|
| `receber-avaliacao` | `dynamodb:PutItem` (tabela), `sns:Publish` (tópico crítico), logs e X-Ray |
| `notificar-critico` | `ses:SendEmail` (condição `ses:FromAddress`), `sqs:SendMessage` (DLQ), logs e X-Ray |
| `relatorio-semanal` | `dynamodb:Query` (somente o índice `indice-dia-dataEnvio`), `ses:SendEmail`, logs e X-Ray |

Nenhuma função tem `Scan`, `DeleteItem`, `*:*` ou acesso a outros recursos.

## 3. Pipeline de deploy sem chaves (OIDC)
O GitHub Actions assume uma role via OpenID Connect; não há `AWS_ACCESS_KEY_ID` salvo no repositório.

1. No IAM, crie o provedor de identidade `token.actions.githubusercontent.com` (audience `sts.amazonaws.com`).
2. Crie a role `feedback-github-deploy` com a política de confiança abaixo (ajuste conta, organização e repositório):

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "Federated": "arn:aws:iam::<CONTA>:oidc-provider/token.actions.githubusercontent.com" },
    "Action": "sts:AssumeRoleWithWebIdentity",
    "Condition": {
      "StringEquals": { "token.actions.githubusercontent.com:aud": "sts.amazonaws.com" },
      "StringLike":   { "token.actions.githubusercontent.com:sub": "repo:<ORG>/<REPO>:ref:refs/heads/main" }
    }
  }]
}
```
3. Anexe permissões para CloudFormation, S3 (bucket do SAM), Lambda, API Gateway, DynamoDB, SNS, SQS, EventBridge,
   CloudWatch, Logs, SES e `iam:PassRole`/`iam:CreateRole` restritos aos recursos `feedback-*`.
   (Em produção, restrinja ainda mais com *permissions boundary*.)
4. Salve o ARN da role no secret `AWS_DEPLOY_ROLE_ARN` do repositório.

## 4. Proteção dos dados dos clientes
| Controle | Implementação |
|---|---|
| Criptografia em trânsito | HTTPS obrigatório no API Gateway; SDK AWS usa TLS |
| Criptografia em repouso | DynamoDB (KMS), SNS (`alias/aws/sns`), SQS (SSE gerenciado) |
| Minimização em logs | Logs não contêm a `descricao`; apenas id, nota e urgência |
| Validação de entrada | Tipo, faixa (0-10) e tamanho (2000 caracteres) da descrição |
| Prevenção de injeção | Escape de HTML na descrição exibida no relatório |
| Proteção contra abuso | API key, throttling e cota diária no usage plan |
| Recuperação | Point-in-Time Recovery no DynamoDB |
| Retenção | Logs com retenção de 30 dias |

## 5. Melhorias para produção
- Substituir API key por **Cognito/JWT** ou Lambda authorizer para identificar o estudante.
- Associar **AWS WAF** ao API Gateway.
- Usar CMK própria (KMS) com rotação e política de chave explícita.
- Separar contas AWS por ambiente (dev/prod) via AWS Organizations.
- Habilitar CloudTrail e AWS Config para auditoria de acesso e configuração.
