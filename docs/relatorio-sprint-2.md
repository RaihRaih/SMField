# Relatório do Projeto — Sprint 2

**Projeto:** SMField — Sistema de Monitoramento Inteligente e Análise de Riscos Agrícolas
**Disciplina:** Gestão e Qualidade de Software — Squad Ânima HUB
**Professora:** Rafaela
**Integrantes (conforme documentação do repositório):** Arthur (Arthur Henrique), Brener (Brenner), João, Raissa e Tayná

---

## 1. Introdução

O acompanhamento de lavouras depende de vários indicadores ambientais — umidade do solo, temperatura, umidade do ar e chuva — e de ocorrências observadas em campo. Quando esses dados ficam espalhados em anotações ou planilhas, é difícil perceber rapidamente quais áreas estão em situação desfavorável.

O **SMField** é uma aplicação desktop que centraliza propriedades, talhões e indicadores ambientais e transforma esses dados em um **índice de risco de 0 a 100**, com classificação por cores e uma justificativa em linguagem simples.

## 2. Objetivo do projeto

Ajudar produtores e gestores a **priorizar os talhões que precisam de atenção**. Para isso o sistema:

- organiza propriedades e talhões em um só lugar;
- registra indicadores ambientais com validação, preservando o histórico;
- calcula e classifica o risco de forma determinística e explicável;
- apresenta um painel ordenado do maior para o menor risco, com alertas internos.

## 3. Tecnologias utilizadas

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem de todo o sistema |
| Maven | Build, dependências, execução (`exec:java`) e empacotamento (`smfield.jar`) |
| Java Swing | Interface gráfica desktop |
| Gson 2.10.1 | Persistência em arquivos JSON locais |
| JUnit 5 (5.10.2) | Testes unitários |
| Cucumber 7.18.0 + Gherkin | Testes de comportamento (cenários escritos em português) |
| JaCoCo 0.8.11 | Medição de cobertura e verificação da meta de 80% |
| Git / GitHub | Versionamento |

## 4. Arquitetura

O código está organizado em quatro camadas (pacotes Java):

- **`domain`** — entidades e regras invariantes: `Propriedade`, `Talhao`, `IndicadorAmbiental`, `NivelRisco`, `ResultadoRisco`, `FatorRisco`, `OcorrenciaCampo`, `Alerta` etc. Nenhuma dependência de outras camadas.
- **`service`** — regras de negócio: cadastros, motor de risco (`RiscoService`), consolidação do painel, histórico e alertas (`MonitoramentoService`), ocorrências, dados de demonstração e o módulo demonstrativo de compatibilidade.
- **`infra`** — persistência: `JsonRepository<T>` genérico e repositórios por entidade, gravando arquivos JSON na pasta `dados/`.
- **`ui`** — interface Swing: janela principal com abas e painéis que apenas chamam os serviços e exibem os resultados.

A interface não contém cálculo de risco nem regras de validação: tudo isso fica em `domain`/`service`, o que permitiu testar as regras sem abrir janelas. O sistema da Sprint 2 possui **45 tipos Java autorais** (15 em `domain`, 10 em `service`, 7 em `infra` e 13 em `ui`).

O módulo da Sprint 1 (recomendador de plantio por região, linha de comando) foi mantido sem alterações no pacote `smfield`, com seus dados em `src/main/resources/dados/`. Ele compartilha o mesmo `pom.xml` e o mesmo build. Detalhes em `docs/arquitetura/arquitetura-inicial.md`.

## 5. Funcionalidades desenvolvidas

| Requisito | Classes principais | Tela |
|---|---|---|
| RF-01 Cadastrar propriedade | `Propriedade`, `PropriedadeService`, `PropriedadeRepository` | Propriedades |
| RF-02 Cadastrar talhão | `Talhao`, `TalhaoService`, `TalhaoRepository` | Talhões |
| RF-03 Listar propriedades e talhões | `PropriedadeService`, `TalhaoService` | Propriedades, Talhões |
| RF-04 Registrar indicadores | `IndicadorAmbiental`, `IndicadorService`, `ConversorEntrada` | Indicadores ambientais |
| RF-05 Calcular índice | `RiscoService` | Indicadores, Consulta, Dashboard |
| RF-06 Classificar risco | `NivelRisco` | Todas as telas de risco |
| RF-07 Painel de riscos | `MonitoramentoService` | Dashboard |
| RF-08 Consulta e justificativa | `MonitoramentoService`, `RiscoService`, `FatorRisco` | Consulta de risco |
| RF-09 Editar/excluir talhões | `TalhaoService` | Talhões |
| RF-10 Ocorrências de campo | `OcorrenciaCampo`, `OcorrenciaService` | Ocorrências |
| RF-11 Histórico | `IndicadorService`, `MonitoramentoService`, `RegistroHistorico` | Histórico e alertas |
| RF-12 Alertas internos | `MonitoramentoService`, `Alerta`, `TipoAlerta` | Histórico e alertas |
| Demonstrativo: compatibilidade de culturas | `CompatibilidadeService`, `RequisitoCultura` | Compatibilidade (demo) |
| Sprint 1: recomendação de plantio, "o que plantar agora" e calendário de colheita | `smfield.Recomendador`, `smfield.Calendario`, `smfield.DadosLoader`, `smfield.Main` | Linha de comando |

## 6. Sprint 1 — Base funcional

A Sprint 1 está na branch `dev` do repositório e foi preservada integralmente nesta entrega:

- **Documentação inicial:** visão do produto, requisitos funcionais e não funcionais e arquitetura inicial.
- **MVP em Java/Maven (linha de comando), pacote `smfield`:** ajuda o agricultor a decidir quando plantar e colher em uma região, alinhado à proposta do pré-projeto:
  - recomendação de plantio com nota de 0 a 100 para cada mês e colheita estimada (`recomendar`);
  - ranking do que plantar em um mês na região (`agora`);
  - calendário de colheita a partir da data de plantio (`colheita`);
  - listagem de culturas e regiões (`listar`).
- **Dados ilustrativos** em `culturas.json` e `regioes.json` (6 culturas e 3 regiões).
- **23 testes JUnit 5.**
- A classe `smfield.domain.Propriedade`, ponto de partida do cadastro de propriedades.

Documentação completa: `docs/sprint-1-cli-java.md`.

**Integração com a Sprint 2.** A Sprint 2 corrigiu a estrutura Maven (o pacote de trabalho tinha o código em `scr/main/java/src/main/java/domain`, com o pacote `src.main.java.domain`) e organizou o sistema de monitoramento de riscos nas quatro camadas exigidas. O módulo da Sprint 1 continua no pacote `smfield`, sem alterações de código, e é compilado e testado pelo mesmo `mvn clean verify`. Como o `.jar` passou a abrir a interface gráfica, a linha de comando da Sprint 1 é executada com `java -cp target/smfield.jar smfield.Main <comando>`.

## 7. Sprint 2

### Incremento 1 — Registro ambiental (RF-04)

- Formulário com talhão, umidade do solo, temperatura, umidade do ar, precipitação e data/hora (botão "Agora").
- Validações: umidades 0–100%; temperatura −20 a 60 °C; precipitação ≥ 0 (até 500 mm); letras, `NaN` e infinito rejeitados; os quatro indicadores obrigatórios; talhão precisa existir; data não pode ser futura.
- **Período de referência da precipitação:** acumulado das últimas 24 horas.
- Persistência em `indicadores.json` e lista dos registros do talhão.

### Incremento 2 — Motor de análise de risco (RF-05, RF-06 e lógica do RF-08)

- Cálculo ponderado com penalidades 0/50/100, feito com aritmética inteira (determinístico, sem números aleatórios).
- Classificação automática nos limites 0–33 / 34–66 / 67–100 e estado separado **NÃO CALCULADO**.
- Justificativa gerada a partir dos indicadores fora da faixa, com valor, faixa de referência e pontos somados ao índice, em ordem de contribuição.
- Testes de limites (cada fronteira das faixas) e de cenários extremos (14.256 combinações verificadas para o intervalo 0–100 e a coerência índice × nível).

### Incremento 3 — Monitoramento e visualização (RF-07, apresentação do RF-08, RF-11, RF-12)

- Dashboard com cartões de contagem por nível, tabela colorida ordenada por criticidade e acesso à justificativa por duplo clique.
- Tela de consulta com indicadores usados, pesos, penalidades, justificativa e ocorrências do talhão.
- Histórico cronológico com a variação de classificação entre registros.
- Alertas de risco alto, mudança de classificação e talhão sem indicadores.

### Complementos do MVP previstos na visão do produto

- RF-09: edição e exclusão de talhões, com confirmação que informa quantos registros vinculados serão removidos.
- RF-10: ocorrências de campo.
- Carga de **dados de demonstração SIMULADOS**, sem duplicação.
- Módulo demonstrativo de compatibilidade de culturas por município (ver seção 11).

### Refinamentos da Sprint 2

- Mensagens de validação escritas para o usuário (indicando o campo e o valor informado).
- Proibição de duplicidades (propriedade com mesmo nome e município; talhão com mesmo nome na mesma propriedade).
- Identificadores nunca reutilizados e gravação atômica dos arquivos.
- Avisos sobre o caráter acadêmico do modelo exibidos na interface.

### Problemas encontrados e corrigidos

| Problema | Correção |
|---|---|
| Código fora do padrão Maven (`scr/main/java/src/main/java/domain`) e pacote `src.main.java.domain` | Movido para `src/main/java/domain` com `package domain;` |
| Ausência de `pom.xml` | Projeto Maven criado com compilação, testes, cobertura, execução e `.jar` executável |
| Arquitetura previa `MemoriaPropriedadeRepository`, que perderia os dados ao fechar | Substituído por persistência em JSON |
| Alerta de risco alto repetia o texto "Risco alto (índice X)" duas vezes | Mensagem passou a usar só a justificativa |
| Aviso do Swing ("row index is bigger than sorter's row count") ao atualizar a tabela de talhões | Ordenador da tabela recriado após recarregar as linhas; aviso não ocorre mais no roteiro |
| Selo do índice esticado e colunas truncadas em algumas telas | Ajustes de layout identificados pelas capturas de tela |
| O pacote de trabalho não continha o código da Sprint 1, e o primeiro envio da branch `sprint-2` o removia | Código, dados, testes e documentação da Sprint 1 restaurados a partir da branch `dev` e integrados ao build |
| A versão 2.22.3 do `maven-surefire-plugin` não existe no Maven Central | Atualizado para a versão 3.2.5 |

## 8. Regras de negócio

**Cálculo.** Cada indicador recebe penalidade conforme a distância da faixa de referência:

| Indicador | Peso | 0 pontos | 50 pontos | 100 pontos |
|---|---|---|---|---|
| Umidade do solo | 35% | 40–70% | 25–<40% ou >70–85% | <25% ou >85% |
| Temperatura | 30% | 18–30 °C | 10–<18 °C ou >30–38 °C | <10 °C ou >38 °C |
| Umidade do ar | 15% | 40–80% | 20–<40% ou >80–90% | <20% ou >90% |
| Precipitação 24 h | 20% | 5–30 mm | 0–<5 mm ou >30–100 mm | >100 mm |

`risco = solo × 0,35 + temperatura × 0,30 + umidade do ar × 0,15 + precipitação × 0,20`, arredondado para o inteiro mais próximo.

**Exemplo (roteiro de demonstração):** solo 22% (100 pts × 0,35 = 35), temperatura 36 °C (50 × 0,30 = 15), umidade do ar 35% (50 × 0,15 = 7,5), precipitação 0 mm (50 × 0,20 = 10) → 67,5 → **68, ALTO**.

**Classificações.** BAIXO 0–33, MÉDIO 34–66, ALTO 67–100. Sem os quatro indicadores: **NÃO CALCULADO** (cinza), nunca "baixo".

**Justificativas.** Listam apenas os indicadores fora da faixa. Exemplo real gerado pelo sistema:

> Risco alto (índice 68) identificado devido a: umidade do solo muito abaixo da faixa de referência (22 %; referência 40–70 %; +35 pontos); temperatura acima da faixa de referência (36 °C; referência 18–30 °C; +15 pontos); precipitação abaixo da faixa de referência, com pouca chuva nas últimas 24 h (0 mm; referência 5–30 mm; +10 pontos) e umidade do ar abaixo da faixa de referência (35 %; referência 40–80 %; +7,5 pontos).

Quando todos os indicadores estão na faixa ideal: "não foram identificados desvios nas faixas de referência acadêmicas adotadas".

## 9. Testes e qualidade

Comando executado: `mvn clean verify`

| Item | Resultado real |
|---|---|
| Métodos de teste JUnit 5 — Sprint 2 | 83 (74 `@Test` e 9 `@ParameterizedTest`), 133 execuções |
| Testes JUnit 5 — Sprint 1 (preservados) | 23 |
| Cenários Cucumber executados | 15 (9 cenários + 6 exemplos de esquema), 77 passos |
| Total de testes no Surefire | **171 executados, 0 falhas, 0 erros, 0 ignorados** |
| Cobertura de linhas `service` | 99,2% (ramos 94,8%) |
| Cobertura de linhas `domain` | 98,9% (ramos 95,3%) |
| Cobertura de linhas `infra` | 94,9% (ramos 86,2%) |
| Cobertura de linhas `ui` | 0% — sem testes automatizados de interface |
| Cobertura de linhas `smfield` (Sprint 1) | 76,9% (`smfield.domain`: 0%) |
| Cobertura total do projeto | 44,0% de linhas |
| Verificação JaCoCo (≥ 80% em `service` e `domain`) | Aprovada ("All coverage checks have been met") |

**Cenários BDD** (`src/test/resources/features`): cadastro de propriedade; cadastro de talhão vinculado; talhão sem propriedade rejeitado; risco baixo; risco alto; dados insuficientes; indicador inválido; classificação nos limites 0, 33, 34, 66, 67 e 100; dashboard com riscos diferentes e alertas; histórico preservado após fechar e reabrir.

**Teste manual / de interface.** O roteiro obrigatório (Fazenda Modelo, Contagem, Talhão A, 12 ha, milho, duas medições, histórico, fechar e reabrir) foi executado na interface real, acionando os botões dos painéis Swing em um display virtual, com captura de tela de cada etapa. Também foram verificados os cenários de erro (município vazio, área negativa, letras em campo numérico, umidade 150, indicador ausente). O registro está em `docs/evidencias/log-roteiro-interface.txt` e as telas em `docs/evidencias/telas/`. O mesmo roteiro também existe como teste automatizado de serviço (`RoteiroDemonstracaoTest`).

**Observação sobre o ambiente de build desta entrega.** O ambiente usado para desenvolver não tinha acesso ao Maven Central; as bibliotecas foram obtidas de um cache Maven local e de pacotes do sistema, e o plugin do JaCoCo 0.8.11 foi compilado a partir do código-fonte oficial. Por isso o `pom.xml` fixa versões estáveis já validadas (JUnit 5.10.2, Cucumber 7.18.0, Gson 2.10.1, JaCoCo 0.8.11, Surefire 3.2.5). Em uma máquina com internet, o Maven baixa essas mesmas versões do Maven Central.

## 10. Resultados

Funcionalidades comprovadamente operacionais (testes automatizados + roteiro na interface):

- cadastro, edição, exclusão e listagem de propriedades e talhões, com vínculo obrigatório;
- registro de indicadores com rejeição de dados inválidos ou incompletos;
- cálculo, classificação e justificativa do risco;
- dashboard ordenado por criticidade com cores;
- consulta individual com fatores e ocorrências;
- histórico preservado e alertas internos;
- dados mantidos após fechar e reabrir a aplicação;
- carga de dados de demonstração sem duplicação;
- módulo demonstrativo de compatibilidade de culturas.

## 11. Limitações

- **Modelo de risco acadêmico:** faixas e pesos são hipóteses para demonstrar a lógica do software. Não são limites agronômicos validados e não variam por cultura, fase da lavoura ou tipo de solo.
- **Dados da Sprint 1 ilustrativos:** as normais climatológicas e os parâmetros das culturas do recomendador são aproximados e devem ser substituídos por dados do INMET e do ZARC/Embrapa antes de uso real.
- **Dados manuais ou simulados:** não há sensores, estações meteorológicas nem APIs. Os dados de demonstração são fictícios (municípios fictícios, prefixo [SIMULADO]) e não representam condições reais. Os valores usados no roteiro com "Contagem" também são fictícios.
- **Ocorrências** não alteram o índice numérico nesta versão.
- **Relação com o pré-projeto:** o pré-projeto apresentado no Ânima HUB tratava da comparação entre o clima de uma região e os requisitos das culturas. Essa proposta foi atendida na **Sprint 1** pelo recomendador de plantio por região. A **Sprint 2** seguiu os requisitos do repositório e implementou o monitoramento de riscos por talhão. O módulo "Compatibilidade (demo)" da interface faz uma ligação simples entre as duas sprints: compara as médias dos indicadores registrados nos talhões de um município com uma **base simulada** de requisitos por cultura. Nenhuma das duas sprints considera características de solo como pH, textura e fertilidade, e nenhuma deve ser apresentada como recomendação agronômica.
- **Interface** sem testes automatizados no build (validada pelo roteiro com capturas de tela).
- **Persistência JSON** pensada para uso local por um usuário.

## 12. Evoluções futuras

Possibilidades não implementadas nesta versão:

- integração com APIs meteorológicas e estações para preencher indicadores automaticamente;
- sensores IoT de umidade do solo;
- faixas de referência por cultura e por fase, definidas com apoio agronômico;
- uso das ocorrências no cálculo, com regra validada;
- gráficos de tendência, exportação de relatórios e notificações externas;
- banco de dados e acesso multiusuário;
- testes automatizados de interface.

## 13. Conclusão

O SMField chega ao fim da Sprint 2 com duas frentes integradas no mesmo projeto: o recomendador de plantio por região da Sprint 1 e o monitoramento de riscos por talhão da Sprint 2. Este cobre os oito requisitos funcionais do repositório e os quatro complementares da visão do produto, com arquitetura em camadas e persistência local. O projeto tem 171 testes automatizados aprovados e cobertura acima da meta nas camadas de regras de negócio. O cálculo é transparente: o usuário vê não apenas o nível de risco, mas também por que ele foi atribuído — o que torna o painel útil para decidir quais talhões visitar primeiro.

Para a equipe, o projeto consolidou práticas de qualidade de software: requisitos rastreáveis até classes e testes, regras de negócio isoladas da interface, cenários BDD legíveis, medição real de cobertura e documentação honesta das limitações.

---

### Anexo — publicação no GitHub

A Sprint 2 foi publicada na branch `sprint-2`, criada a partir da `dev`, para integração por Pull Request (`sprint-2` → `dev`), preservando o histórico da Sprint 1.
