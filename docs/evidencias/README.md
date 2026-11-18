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
| Dados de demonstração (Sorriso/MT, Rio Verde/GO, Patrocínio/MG, clima real do INMET) | 7 talhões ordenados por criticidade: 2 alto (milho safrinha de Rio Verde e o Talhão A do roteiro), 2 médio, 2 baixo, 1 não calculado | `16-dashboard-com-dados-demo.png` |
| Alertas com demonstração | 7 alertas (risco alto, mudança de classificação, sem indicadores) | `17-historico-alertas-demo.png` |
| Consulta de outro talhão | Café Catuaí (Patrocínio/MG): índice 8 (Baixo), fonte INMET + solo simulado, ocorrência de bicho-mineiro | `18-consulta-cafe-catuai.png` |
| RF-10 ocorrência | "Ocorrência registrada com sucesso." | `19-ocorrencias.png` |
| Compatibilidade de culturas | Sorriso/MT com clima real da estação A904: "3 cultura(s) potencialmente compatível(is)" (milho, soja, feijão) | `20-compatibilidade-demo.png` |
| RF-09 edição | Área alterada para 14,5 ha | `22-talhoes-apos-edicao.png` |
| RF-09 exclusão | Confirmação informa 2 indicadores e 0 ocorrências vinculados; resposta "Não" mantém o talhão | `21-confirmacao-exclusao-talhao.png` |

Os valores usados com o município "Contagem" são fictícios e não representam condições reais. Na demonstração, municípios e clima são reais (INMET); propriedades, talhões e ocorrências são fictícios ([DEMO]) e a umidade do solo é simulada.

Também foram verificados os comandos de execução: `mvn compile exec:java` e `java -jar target/smfield.jar` abriram a janela sem erros.

## 3. Dados reais do INMET (RF-13)

Roteiro executado na interface real (`inmet/RoteiroInmet.java.txt`); registro completo em `inmet/log-roteiro-inmet.txt`.

| Verificação | Resultado observado | Tela |
|---|---|---|
| Propriedade em Contagem (sem estação própria) | Mensagem orientando a buscar a cidade mais próxima | `inmet/01-aba-inmet-municipio-sem-estacao.png` |
| Busca "Belo Horizonte" | 3 estações; selecionada A521 (Pampulha), dados de 01/01/2026 a 31/10/2026 | `inmet/02-previa-a521-outubro.png` |
| Prévia 01–31/10/2026 | 31 dias prontos; ex.: 01/10 = 27,5 °C, 48,8%, 0,4 mm, solo simulado 15,5% → índice 45 (Médio) | `inmet/02-previa-a521-outubro.png` |
| Período invertido | "A data inicial deve ser anterior ou igual à data final." | `inmet/03-erro-periodo-invertido.png` |
| Período de 304 dias | "Escolha um período de até 92 dias (informado: 304)." | `inmet/04-erro-periodo-longo.png` |
| Importação | 31 dias importados | `inmet/05-importacao-concluida.png` |
| Reimportação | 0 importados, 31 já existiam (sem duplicar) | `inmet/06-importacao-repetida-sem-duplicar.png` |
| Consulta e histórico | Fonte "INMET A521 (Belo Horizonte - Pampulha/MG) · umidade do solo SIMULADA" | `inmet/07-...`, `inmet/08-...`, `inmet/11-...` |
| Demonstração em polos do agronegócio | Milho safrinha de Rio Verde/GO em 31/08/2026: índice 68, **ALTO**, com alerta; Sorriso/MT e Patrocínio/MG com clima real | `inmet/09-dashboard-demo-polos-agro.png`, `inmet/10-historico-rio-verde-milho-safrinha.png` |

Build após a integração: **192 testes, 0 falhas** (17 cenários Cucumber), `All coverage checks have been met`.

## 4. Interface modernizada

Capturas de todas as seções na interface nova, com os dados de demonstração (janela 1440 × 900), geradas por `interface/Vitrine.java.txt` (não faz parte do build). As capturas das seções 2 e 3 acima também foram refeitas com a interface nova pelos mesmos roteiros, que terminaram com os mesmos resultados de antes da modernização (`FIM OK`).

| Seção | Tela |
|---|---|
| Dashboard (indicadores por nível, tabela, rosca e barras do índice) | `interface/01-dashboard.png` |
| Propriedades | `interface/02-propriedades.png` |
| Talhões | `interface/03-talhoes.png` |
| Indicadores ambientais (evolução de cada indicador) | `interface/04-indicadores-ambientais.png` |
| Consulta de risco (medidor, justificativa e composição do índice) | `interface/05-consulta-de-risco.png` |
| Ocorrências | `interface/06-ocorrencias.png` |
| Histórico e alertas (linha do tempo do índice) | `interface/07-historico-e-alertas.png` |
| Dados reais (INMET) (estação e índice da prévia) | `interface/08-dados-reais-inmet.png` |
| Compatibilidade de culturas (cartões por cultura) | `interface/09-compatibilidade-de-culturas.png` |
| Menu recolhido na janela mínima (1120 × 720), com o Dashboard reorganizado | `interface/10-menu-recolhido-janela-minima.png` |
| Sistema sem dados (estado vazio) | `interface/11-dashboard-sem-dados.png` |
| Janela "Sobre" com a logo oficial e o slogan | `interface/12-sobre-com-logo.png` |
| Janela "Como o risco é calculado" | `interface/13-modelo-de-risco.png` |

Build após a modernização: **192 testes, 0 falhas** (17 cenários Cucumber), `All coverage checks have been met`.
