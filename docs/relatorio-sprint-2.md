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
| Base do INMET (CSV) | Clima real de 634 estações automáticas, jan–out/2026, tratado previamente por script Python |
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

A interface não contém cálculo de risco nem regras de validação: tudo isso fica em `domain`/`service`, o que permitiu testar as regras sem abrir janelas. O sistema da Sprint 2 possui **50 tipos Java autorais** (17 em `domain`, 11 em `service`, 8 em `infra` e 14 em `ui`).

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
| RF-13 Importar clima real do INMET | `InmetService`, `InmetRepository`, `EstacaoMeteorologica`, `LeituraClimaticaDiaria` | Dados reais (INMET) |
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
- Carga de **dados de demonstração** em três polos reais do agronegócio (Sorriso/MT, Rio Verde/GO e Patrocínio/MG), com clima real do INMET e sem duplicação.
- Módulo demonstrativo de compatibilidade de culturas por município (ver seção 11).

### Incremento complementar — dados reais do INMET (RF-13)

Para aproximar o sistema de dados reais, foi integrada uma base das estações automáticas do **INMET**: 634 estações, de 01/01/2026 a 31/10/2026, com mais de 128 mil dias de registro. A base foi tratada previamente por um script Python (`ferramentas/inmet/tratar_inmet.py`), recebido de um colaborador, que resume os registros horários em valores diários.

- **Aba "Dados reais (INMET)":**
  - busca de estação por cidade, UF ou código, com sugestão automática pelo município da propriedade;
  - prévia dia a dia com o risco que seria calculado;
  - importação de até 92 dias para um talhão.
- **Origem dos dados:** temperatura média, umidade do ar e chuva do dia são reais. A **umidade do solo é simulada**, porque o INMET não a mede, por uma fórmula acadêmica documentada em que a chuva recente pesa mais, ar úmido aumenta e calor reduz. O índice de risco segue o mesmo modelo acadêmico.
- **Rastreabilidade:** cada registro guarda a fonte (ex.: "INMET A521 (Belo Horizonte - Pampulha/MG) · umidade do solo SIMULADA"), exibida no histórico, na lista de indicadores e na consulta de risco. Registros manuais aparecem como "Registro manual".
- **Sem duplicação:** importar o mesmo período de novo não cria registros repetidos. Dias sem dados completos no INMET são ignorados e informados.
- **Demonstração em municípios reais do agronegócio**, substituindo os municípios fictícios, com o clima real do ciclo de cada cultura:
  - **Sorriso/MT** (estação A904): soja no plantio de outubro e milho safrinha de abril a junho;
  - **Rio Verde/GO** (A025): soja de agosto a outubro e milho safrinha de junho a agosto. No fim da seca de agosto (solo seco, cerca de 30 °C e umidade do ar abaixo de 30%), esse talhão chega a risco **alto (68)** e o sistema gera o alerta;
  - **Patrocínio/MG** (A523): café Catuaí de agosto a outubro, da seca à florada, e um talhão de café novo ainda sem medições.

  Os municípios e o clima são reais; propriedades, talhões e ocorrências são fictícios (prefixo [DEMO]).
- **Compatibilidade com clima real:** quando o município tem estação, a aba "Compatibilidade (demo)" passa a usar o clima real da estação (jan–out/2026) em vez das médias de poucos registros. Como resultado:
  - Sorriso aparece compatível com milho, soja e feijão;
  - Patrocínio inclui o café.

  A umidade do solo fica de fora dessa comparação, porque o INMET não a mede.

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
| Cucumber em português leu "27.54" como 2754, porque no padrão brasileiro a vírgula é o separador decimal | Cenário escrito com "27,54" |
| No Windows, ao carregar a demonstração (398 gravações seguidas de `indicadores.json`), aparecia "Não foi possível gravar o arquivo de dados". Antivírus e indexador do Windows seguram o arquivo por alguns milissegundos após cada gravação | A troca do arquivo passou a ser repetida até 8 vezes, com pausas curtas, antes de informar erro; o temporário é sempre apagado. Teste novo para falha persistente |
| A demonstração usava municípios fictícios | Substituídos por três polos reais do agronegócio (Sorriso/MT, Rio Verde/GO e Patrocínio/MG), com clima real do INMET |
| A compatibilidade apontava "nenhuma cultura compatível" em Sorriso/MT, porque usava só as poucas medições de períodos secos e a umidade do solo simulada | Passou a usar o clima real da estação do município, sem a umidade do solo |
| Município sem estação (ex.: Contagem) fazia a tela pré-selecionar uma estação sem dados, e empates de cobertura escolhiam a estação secundária | Orientação para buscar a cidade mais próxima e desempate pela estação principal (A521) |

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
| Métodos de teste JUnit 5 — Sprint 2 | 102 (93 `@Test` e 9 `@ParameterizedTest`), 152 execuções |
| Testes JUnit 5 — Sprint 1 (preservados) | 23 |
| Cenários Cucumber executados | 17 (11 cenários + 6 exemplos de esquema), 89 passos |
| Total de testes no Surefire | **192 executados, 0 falhas, 0 erros, 0 ignorados** |
| Cobertura de linhas `service` | 99,4% (ramos 92,9%) |
| Cobertura de linhas `domain` | 99,0% (ramos 95,0%) |
| Cobertura de linhas `infra` | 94,7% (ramos 89,1%) |
| Cobertura de linhas `ui` | 0% — sem testes automatizados de interface |
| Cobertura de linhas `smfield` (Sprint 1) | 76,9% (`smfield.domain`: 0%) |
| Cobertura total do projeto | 45,1% de linhas |
| Verificação JaCoCo (≥ 80% em `service` e `domain`) | Aprovada ("All coverage checks have been met") |

**Cenários BDD** (`src/test/resources/features`):
- cadastro de propriedade;
- cadastro de talhão vinculado;
- talhão sem propriedade rejeitado;
- risco baixo, risco alto, dados insuficientes e indicador inválido;
- classificação nos limites 0, 33, 34, 66, 67 e 100;
- dashboard com riscos diferentes e alertas;
- histórico preservado após fechar e reabrir;
- importação de uma semana de dados reais do INMET (Belo Horizonte, 01–07/10/2026);
- reimportação sem duplicar registros.

A integração com o INMET também foi validada na interface: busca de estação, prévia, importação, erros de período, reimportação e demonstração com clima real. As telas estão em `docs/evidencias/inmet/`.

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
- **Dados do INMET:** cobrem só jan–out/2026, vêm de um arquivo embutido (sem consulta online) e representam a estação, não a propriedade. A umidade do solo dos registros importados é **simulada**.
- **Dados de demonstração:** os municípios e o clima são reais (INMET), mas as propriedades, os talhões e as ocorrências são fictícios (prefixo [DEMO]), e a umidade do solo é simulada. Não há sensores nem APIs em tempo real. Os valores digitados no roteiro com "Contagem" também são fictícios.
- **Ocorrências** não alteram o índice numérico nesta versão.
- **Relação com o pré-projeto:** o pré-projeto apresentado no Ânima HUB tratava da comparação entre o clima de uma região e os requisitos das culturas. Essa proposta foi atendida na **Sprint 1** pelo recomendador de plantio por região. A **Sprint 2** seguiu os requisitos do repositório e implementou o monitoramento de riscos por talhão. O módulo "Compatibilidade (demo)" da interface faz uma ligação simples entre as duas sprints: compara o clima real do município (estação do INMET) com uma **base simulada** de requisitos por cultura. Nenhuma das duas sprints considera características de solo como pH, textura e fertilidade, e nenhuma deve ser apresentada como recomendação agronômica.
- **Interface** sem testes automatizados no build (validada pelo roteiro com capturas de tela).
- **Persistência JSON** pensada para uso local por um usuário.

## 12. Evoluções futuras

Possibilidades não implementadas nesta versão:

- consulta online ao INMET (ou outra API) para manter a base atualizada, além da importação do arquivo já incluído;
- sensores de umidade do solo, que substituiriam a estimativa simulada;
- sensores IoT de umidade do solo;
- faixas de referência por cultura e por fase, definidas com apoio agronômico;
- uso das ocorrências no cálculo, com regra validada;
- gráficos de tendência, exportação de relatórios e notificações externas;
- banco de dados e acesso multiusuário;
- testes automatizados de interface.

## 13. Conclusão

O SMField chega ao fim da Sprint 2 com duas frentes integradas no mesmo projeto: o recomendador de plantio por região da Sprint 1 e o monitoramento de riscos por talhão da Sprint 2. Este cobre os oito requisitos funcionais do repositório e os quatro complementares da visão do produto, e passou a usar clima real do INMET. O projeto tem arquitetura em camadas, persistência local, 192 testes automatizados aprovados e cobertura acima da meta nas camadas de regras de negócio. O cálculo é transparente: o usuário vê não apenas o nível de risco, mas também por que ele foi atribuído — o que torna o painel útil para decidir quais talhões visitar primeiro.

Para a equipe, o projeto consolidou práticas de qualidade de software: requisitos rastreáveis até classes e testes, regras de negócio isoladas da interface, cenários BDD legíveis, medição real de cobertura e documentação honesta das limitações.

---

### Anexo — publicação no GitHub

A Sprint 2 foi publicada na branch `sprint-2`, criada a partir da `dev`, para integração por Pull Request (`sprint-2` → `dev`), preservando o histórico da Sprint 1.
