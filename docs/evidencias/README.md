# Evidências da Sprint 2

## 1. Build e testes

Comando: `mvn clean verify` (Java 21, Maven 3.9.11)

- `Tests run: 148, Failures: 0, Errors: 0, Skipped: 0`
- Cucumber: `15 Scenarios (15 passed)`, `77 Steps (77 passed)`
- JaCoCo: `All coverage checks have been met.` — linhas: `service` 99,2%, `domain` 98,9%, `infra` 94,9%, `ui` 0%, total 36,5%
- `BUILD SUCCESS`; executável gerado em `target/smfield.jar`

Após restaurar o módulo da Sprint 1 (pacote `smfield`), o build completo passou a executar **171 testes** (148 da Sprint 2 + 23 da Sprint 1), com 0 falhas, e a cobertura total ficou em 44,0% de linhas. Os pacotes `service` e `domain` mantiveram os percentuais acima. A linha de comando da Sprint 1 foi verificada com `java -cp target/smfield.jar smfield.Main listar` e `... agora --regiao "Belo Horizonte" --mes 10`.

Os relatórios são gerados a cada build em `target/surefire-reports/`, `target/cucumber-report.html` e `target/site/jacoco/index.html` (a pasta `target/` não é versionada).

## 2. Roteiro de teste manual na interface

O roteiro obrigatório foi executado na interface Swing real: os campos dos painéis foram preenchidos e os botões acionados (`doClick`) em um display virtual, com captura de tela de cada etapa e registro de cada mensagem exibida. A ferramenta usada está em `RoteiroGui.java.txt` (não faz parte do build) e o registro completo em `log-roteiro-interface.txt`.

| Etapa do roteiro | Resultado observado | Tela |
|---|---|---|
| 1. Abrir o SMField | Janela aberta, painel vazio com orientação | `01-tela-inicial-vazia.png` |
| Erro: município vazio | "O campo "Município" é obrigatório." | `02-erro-municipio-vazio.png` |
| 2–3. Fazenda Modelo / Contagem | "Propriedade cadastrada com sucesso." | `03-propriedade-cadastrada.png` |
| Erro: área −5 | "A área do talhão deve ser maior que zero." | `04-erro-area-negativa.png` |
| 4–6. Talhão A, 12 ha, Milho | "Talhão cadastrado com sucesso." | `05-talhao-cadastrado.png` |
| Erro: letras na umidade | "O campo "Umidade do solo" deve conter apenas números…" | `06-erro-letras-em-campo-numerico.png` |
| Erro: umidade 150 | "…deve estar entre 0 e 100 % (valor informado: 150)." | `07-erro-umidade-150.png` |
| Erro: temperatura em branco | "Dados insuficientes… Faltando: temperatura." | `08-erro-indicador-ausente.png` |
| 7–10. Indicadores 55 / 24 / 65 / 12 | Índice 0 — BAIXO | `09-indicadores-registro-1.png` |
| 11. Justificativa | "não foram identificados desvios…" | `10-consulta-justificativa-registro-1.png` |
| 12–13. Dashboard | Talhão A listado com classificação | `11-dashboard-registro-1.png` |
| 14. Segunda medição 22 / 36 / 35 / 0 | Índice 68 — ALTO | `12-indicadores-registro-2.png`, `13-consulta-justificativa-registro-2.png` |
| 15. Histórico | 2 registros; variação BAIXO → ALTO; 2 alertas | `14-historico-e-alertas.png` |
| 16–17. Fechar e reabrir | Dashboard mantém Fazenda Modelo / Talhão A / 68 / Alto | `15-dashboard-apos-reabrir.png` |
| Dados de demonstração | 7 talhões ordenados por criticidade (2 alto, 2 médio, 2 baixo, 1 não calculado) | `16-dashboard-com-dados-demo.png` |
| Alertas com demonstração | 6 alertas (risco alto, mudança, sem indicadores) | `17-historico-alertas-demo.png` |
| Consulta de outro talhão | Índice 90, fatores e ocorrência exibidos | `18-consulta-cafe-encosta.png` |
| RF-10 ocorrência | "Ocorrência registrada com sucesso." | `19-ocorrencias.png` |
| Compatibilidade (demo) | "2 cultura(s) potencialmente compatível(is) segundo a base simulada." | `20-compatibilidade-demo.png` |
| RF-09 edição | Área alterada para 14,5 ha | `22-talhoes-apos-edicao.png` |
| RF-09 exclusão | Confirmação informa 2 indicadores e 0 ocorrências vinculados; resposta "Não" mantém o talhão | `21-confirmacao-exclusao-talhao.png` |

Os valores usados com o município "Contagem" são fictícios e não representam condições reais. Os dados de demonstração são marcados como [SIMULADO].

Também foram verificados os comandos de execução: `mvn compile exec:java` e `java -jar target/smfield.jar` abriram a janela sem erros.
