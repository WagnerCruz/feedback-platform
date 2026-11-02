# Roteiro do vídeo de demonstração (8 a 12 min)

Grave com a infraestrutura já implantada e **destrua a stack logo depois** (créditos limitados).

1. **Introdução (1 min)**: problema, objetivo e modelo escolhido (serverless/FaaS na AWS) e por quê.
2. **Arquitetura (2 min)**: mostrar o diagrama do README e explicar cada componente e a separação por
   responsabilidade (3 funções).
3. **Repositório e código (1 min)**: estrutura multi-módulo, `common`, handlers, testes e `template.yaml`.
4. **Ambiente na nuvem (2 min)** no console AWS:
   - Lambda: as 3 funções ativas (runtime Java 21, ARM64, SnapStart, variáveis de ambiente);
   - IAM: role de cada função (privilégio mínimo);
   - DynamoDB: criptografia, PITR e índice; SNS, SQS (DLQ) e EventBridge (regra semanal);
   - API Gateway: API key, usage plan e throttling.
5. **Pipeline (1 min)**: GitHub Actions executando CI/deploy (ou execução recente concluída).
6. **Demonstração funcional (3 min)**:
   - `POST /avaliacao` com nota alta (201, só grava) e nota baixa (201, grava e dispara e-mail);
   - mostrar o **e-mail de urgência** (descrição, urgência, data de envio);
   - requisição inválida (400) e sem API key (403);
   - itens gravados no DynamoDB;
   - invocar manualmente a função de relatório e mostrar o **e-mail do relatório** (média, quantidade por dia e por urgência).
7. **Monitoramento (1 min)**: dashboard, logs estruturados, alarmes; provocar um erro (ex.: remover variável de
   ambiente de uma função) e mostrar o **e-mail do alarme** chegando ao administrador.
8. **Encerramento (30 s)**: destruir a stack (`aws cloudformation delete-stack`) e mencionar melhorias futuras.
