# SMField
Projeto de Gestão e Qualidade de Software - Squad Ânima HUB | Alunos: Arthur, Brener, João, Raissa e Tayná | Professora: Rafaela

**SMField — Sistema de Monitoramento Inteligente e Análise de Riscos Agrícolas**

Aplicação desktop em Java (Swing) que centraliza propriedades rurais, talhões e indicadores ambientais, calcula um índice de risco agrícola de 0 a 100 para cada talhão, classifica o risco (baixo, médio ou alto), explica os fatores que levaram ao resultado e organiza um painel para priorizar as áreas que precisam de atenção.

---

## 1. Objetivo

Apoiar produtores e gestores agrícolas na identificação e priorização de talhões em condição desfavorável, a partir de uma análise de risco **determinística, documentada e explicável**, sem depender de sensores ou serviços externos na primeira versão.

## 2. Funcionalidades

| ID | Funcionalidade | Onde usar |
|----|----------------|-----------|
| RF-01 | Cadastrar, editar e excluir propriedades (nome, município, identificador único) | Seção **Propriedades** |
| RF-02 | Cadastrar talhões vinculados a uma propriedade (nome, área em ha, cultura) | Seção **Talhões** |
| RF-03 | Listar propriedades e consultar os talhões de cada uma | Seções **Propriedades** e **Talhões** |
| RF-04 | Registrar indicadores ambientais (umidade do solo, temperatura, umidade do ar, precipitação 24 h, data e hora) | Seção **Indicadores ambientais** |
| RF-05 | Calcular o índice de risco (0–100) | Automático ao salvar indicadores |
| RF-06 | Classificar o risco: BAIXO, MÉDIO, ALTO ou NÃO CALCULADO | Todas as telas de risco |
| RF-07 | Painel de riscos ordenado por criticidade, com cores | Seção **Dashboard** |
| RF-08 | Consultar indicadores, índice, classificação e justificativa de um talhão | Seção **Consulta de risco** (ou duplo clique no Dashboard) |
| RF-09* | Editar e excluir talhões (com confirmação e remoção dos registros vinculados) | Seção **Talhões** |
| RF-10* | Registrar ocorrências de campo (praga, doença, falta de irrigação, excesso de água, outra) | Seção **Ocorrências** |
| RF-11* | Histórico de indicadores e riscos (registros anteriores preservados) | Seção **Histórico e alertas** |
| RF-12* | Alertas internos (risco alto, mudança de classificação, talhão sem indicadores) | Seção **Histórico e alertas** |
| RF-13* | Importar clima **real do INMET** (temperatura, umidade do ar e chuva) para um talhão, com umidade do solo **simulada** | Seção **Dados reais (INMET)** |
| Extra (P3) | Compatibilidade de culturas por município — **módulo demonstrativo com base simulada** | Seção **Compatibilidade de culturas** |
| Sprint 1 | Recomendação de plantio, "o que plantar agora" e calendário de colheita por região (linha de comando) | `java -cp target/smfield.jar smfield.Main listar` — ver `docs/sprint-1-cli-java.md` |

\* Requisitos complementares derivados da visão do produto (`docs/requisitos/visao-do-produto.md`).

Os dados são gravados em arquivos JSON locais e continuam disponíveis ao fechar e abrir o programa.

### Interface

A interface segue a identidade visual da logo do SMField: verde floresta, verde agrícola e verde-lima, com a fonte Inter.

- **Menu lateral** com a logo e o slogan, as nove seções em quatro grupos (Visão geral, Cadastros, Monitoramento, Dados e análises), destaque da seção ativa e botão para recolher/expandir (também `Ctrl+B`).
- **Atalhos:** `Ctrl+1` a `Ctrl+9` abrem as seções (menu **Seções**) e `F5` atualiza todas as telas.
- **Cartões e gráficos** feitos só com os dados já calculados pelo sistema:
  - Dashboard: indicadores por nível de risco, rosca de distribuição e barras do índice por talhão;
  - Consulta: medidor do índice e composição do índice com pesos e penalidades;
  - Indicadores: evolução de cada indicador do talhão;
  - Histórico: linha do tempo do índice, com as faixas Baixo/Médio/Alto e as mudanças de classificação destacadas;
  - INMET: índice dia a dia da prévia.
- **Classificações em selos** com cor, ícone e texto, para não depender só da cor.
- **Layout adaptável:** em janelas estreitas o Dashboard empilha os gráficos sob a tabela e as páginas ganham rolagem.

A modernização foi só de apresentação: regras, cálculos, validações, dados e mensagens continuam os mesmos.

### Dados reais do INMET

O projeto inclui a base real das estações automáticas do **INMET** (634 estações, de 01/01/2026 a 31/10/2026) em `src/main/resources/inmet/`. Na seção **Dados reais (INMET)**:

1. escolha o talhão;
2. busque a estação pela cidade, UF ou código (se o município da propriedade tiver estação, ela é sugerida automaticamente);
3. informe o período (até 92 dias) e clique em **Visualizar prévia** para ver dia a dia os valores e o risco que seria calculado;
4. clique em **Importar para o talhão**.

Cada dia vira um registro de indicadores, com a fonte identificada ("INMET A521 … · umidade do solo SIMULADA"). Importar o mesmo período de novo não duplica registros.

- **Reais (INMET):** temperatura média, umidade relativa do ar e chuva do dia.
- **Simulada:** umidade do solo, porque o INMET não a mede. A fórmula acadêmica, em que a chuva recente pesa mais, ar úmido aumenta e calor reduz, é:

  `20 + 0,8 × Σ chuva(d−i) × 0,75^i (7 dias) + 0,3 × (umidade do ar média de 7 dias − 60) − max(0, temperatura − 25)`, limitada a 5–95%.
- **Acadêmico:** o cálculo do índice de risco continua o mesmo.

Origem e atualização da base: `ferramentas/inmet/README.md`.

## 3. Tecnologias

| Componente | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Build e dependências | Maven |
| Interface gráfica | Java Swing com FlatLaf 3.5.4 (tema próprio do SMField) e fonte Inter (licença SIL OFL 1.1, em `src/main/resources/ui/fontes/`) |
| Persistência | Arquivos JSON locais com Gson 2.10.1 |
| Testes unitários | JUnit 5 (5.10.2) |
| Testes BDD | Cucumber 7.18.0 + Gherkin (em português) |
| Cobertura | JaCoCo 0.8.11 |
| Versionamento | Git e GitHub |

## 4. Requisitos para execução

- **JDK 21** (Temurin, Oracle ou o JDK que acompanha o NetBeans/IntelliJ). Verifique com `java -version`.
- **Maven 3.9+** (`mvn -version`). O NetBeans e o IntelliJ já trazem um Maven embutido.
- Acesso à internet **apenas no primeiro build**, para o Maven baixar as dependências do Maven Central. Depois disso o sistema funciona totalmente offline.

## 5. Instalação

```bash
git clone https://github.com/RaihRaih/SMField.git
cd SMField
mvn clean verify
```

O `mvn clean verify` compila, executa todos os testes (JUnit + Cucumber), gera o relatório de cobertura, verifica a meta de 80% e cria o executável `target/smfield.jar`.

## 6. Comandos Maven

| Comando | O que faz |
|---|---|
| `mvn clean verify` | Build completo: compila, testa, mede cobertura (falha se `service`/`domain` ficarem abaixo de 80% de linhas) e gera `target/smfield.jar` |
| `mvn test` | Executa testes unitários e cenários Cucumber |
| `mvn compile exec:java` | Abre a interface gráfica |
| `mvn package -DskipTests` | Gera apenas o `.jar` executável |

## 7. Como executar o programa

**Linha de comando da Sprint 1 (recomendação de plantio por região):**

```bash
java -cp target/smfield.jar smfield.Main listar
java -cp target/smfield.jar smfield.Main agora --regiao "Belo Horizonte" --mes 10
```

**Interface gráfica — pelo terminal (na pasta do projeto):**

```bash
mvn compile exec:java
```

ou, depois de um `mvn clean verify`:

```bash
java -jar target/smfield.jar
```

**No NetBeans:**

1. `Arquivo > Abrir Projeto...` e selecione a pasta `SMField` (o NetBeans reconhece o `pom.xml`).
2. Clique com o botão direito no projeto > `Limpar e Construir` (equivale a `mvn clean install`).
3. Clique em `Executar Projeto` (F6). A classe principal `ui.AppMain` já está configurada no `pom.xml` (`exec.mainClass`).

**No IntelliJ / VS Code:** abra a pasta do projeto como projeto Maven e execute a classe `ui.AppMain`.

**Onde ficam os dados:** na pasta `dados/` dentro do diretório em que o programa foi iniciado (arquivos `propriedades.json`, `talhoes.json`, `indicadores.json` e `ocorrencias.json`). Para usar outra pasta: `java -Dsmfield.dados=C:\caminho\da\pasta -jar target/smfield.jar`. Para começar do zero, feche o programa e apague a pasta `dados/`.

## 8. Como executar os testes

```bash
mvn test
```

Resultado medido (`mvn clean verify`): **192 testes executados, 0 falhas, 0 erros**:

- Sprint 2: 152 execuções JUnit (102 métodos de teste, sendo 9 parametrizados) e **17 cenários Cucumber / 89 passos**;
- Sprint 1: 23 testes JUnit (pacote `smfield`).

Relatórios gerados:

- JUnit: `target/surefire-reports/`
- Cucumber: `target/cucumber-report.html`

## 9. Como verificar a cobertura

Após `mvn clean verify`, abra `target/site/jacoco/index.html` no navegador.

Cobertura medida nesta entrega (linhas):

| Pacote | Linhas | Ramos |
|---|---|---|
| `service` | 99,4% | 92,9% |
| `domain` | 99,0% | 95,0% |
| `infra` | 94,7% | 89,1% |
| `smfield` (Sprint 1) | 76,9% | 71,5% |
| `smfield.domain` (Sprint 1) | 0% | 0% |
| `ui` | 0% (interface Swing não tem testes automatizados no build) | 0% |
| **Total do projeto** | 26,3% | 37,4% |

A regra do JaCoCo no `pom.xml` exige no mínimo **80% de linhas** nos pacotes `service` e `domain` (RNF-06). A interface foi validada por roteiros que clicam nos componentes reais (ver `docs/evidencias/`).

O total do projeto caiu de 45,1% para 26,3% porque a modernização visual acrescentou código ao pacote `ui`, que continua sem testes automatizados. A cobertura de `service` e `domain` não mudou.

## 10. Estrutura de pacotes

```
SMField/
├── pom.xml
├── src/main/java/
│   ├── domain/   entidades e regras invariantes (Propriedade, Talhao, IndicadorAmbiental, NivelRisco, ResultadoRisco...)
│   ├── service/  regras de negócio (cadastros, motor de risco, monitoramento, alertas, demonstração)
│   ├── infra/    persistência JSON (JsonRepository e repositórios por entidade)
│   ├── ui/       interface Swing: AppMain, MainFrame, BarraLateral, painéis e design system (Estilo, Cartao, Graficos, Icone)
│   └── smfield/  Sprint 1: recomendador de plantio por região (linha de comando)
├── src/main/resources/dados/   Sprint 1: culturas.json e regioes.json (valores ilustrativos)
├── src/main/resources/inmet/   base real do INMET (estacoes.csv e diario_consolidado.csv)
├── src/main/resources/ui/      logo e ícones do SMField (marca/) e fonte Inter com a licença OFL (fontes/)
├── ferramentas/inmet/          script que gera a base do INMET e instruções de atualização
├── src/test/java/          testes JUnit (domain, service, infra) e passos Cucumber (bdd)
├── src/test/resources/features/   cenários Gherkin (.feature)
└── docs/                   requisitos, arquitetura, relatório, pitch e evidências
```

Detalhes em `docs/arquitetura/arquitetura-inicial.md`.

## 11. Dados de demonstração

No menu `Arquivo > Carregar dados de demonstração (clima real do INMET)`, ou no botão do Dashboard quando ainda não carregados, o sistema monta a demonstração em **três polos reais do agronegócio**, com o clima **real** da estação do INMET de cada município:

| Município (estação) | Propriedade [DEMO] | Talhões | Período importado | Situação no último dia |
|---|---|---|---|---|
| Sorriso/MT (A904) | Fazenda Boa Esperança | Soja 26/27 (120 ha) | out/2026: plantio com a volta das chuvas | Baixo |
| | | Milho Safrinha (95 ha) | abr–jun/2026 | Médio |
| Rio Verde/GO (A025) | Fazenda Santa Helena | Soja (80 ha) | ago–out/2026, com 67,8 mm de chuva em 31/10 | Médio |
| | | Milho Safrinha (60 ha) | jun–ago/2026: fim de ciclo na seca | **Alto (68)** |
| Patrocínio/MG (A523) | Sítio Cerrado Mineiro | Café Catuaí (15 ha) | ago–out/2026: seca até a florada | Baixo |
| | | Café Novo (5 ha) | sem medições | Não calculado |

- São 398 dias importados e 3 ocorrências de campo.
- O painel mostra os quatro estados (alto, médio, baixo e não calculado), com alertas de risco alto, de mudança de classificação e de talhão sem dados.
- **O que é real:** os municípios e o clima (temperatura, umidade do ar e chuva).
- **O que é fictício:** propriedades, talhões e ocorrências, marcados com o prefixo **[DEMO]**. A umidade do solo é simulada.
- A carga não é duplicada: se já tiver sido feita, o sistema avisa e não cria registros novamente.
- Para recarregar do zero, feche o programa e apague a pasta `dados/`.

## 12. Modelo de risco

Cada indicador recebe uma penalidade de 0 (faixa ideal), 50 (faixa intermediária) ou 100 (faixa crítica):

| Indicador | Peso | Penalidade 0 | Penalidade 50 | Penalidade 100 |
|---|---|---|---|---|
| Umidade do solo | 35% | 40–70% | 25–<40% ou >70–85% | <25% ou >85% |
| Temperatura | 30% | 18–30 °C | 10–<18 °C ou >30–38 °C | <10 °C ou >38 °C |
| Umidade do ar | 15% | 40–80% | 20–<40% ou >80–90% | <20% ou >90% |
| Precipitação (acumulado das últimas 24 h) | 20% | 5–30 mm | 0–<5 mm ou >30–100 mm | >100 mm |

```
risco = solo × 0,35 + temperatura × 0,30 + umidade do ar × 0,15 + precipitação × 0,20
```

O resultado é arredondado para o inteiro mais próximo (0,5 arredonda para cima) e sempre fica entre 0 e 100. Classificação: **BAIXO 0–33**, **MÉDIO 34–66**, **ALTO 67–100**. Sem os quatro indicadores, o estado é **NÃO CALCULADO** (nunca é tratado como risco baixo). A justificativa lista os indicadores fora da faixa, o valor medido e quantos pontos cada um somou ao índice.

Faixas aceitas no registro: umidades 0–100%, temperatura −20 a 60 °C, precipitação 0 a 500 mm.

## 13. Limitações

- **As faixas de referência são hipóteses acadêmicas** para demonstrar a lógica do software. Elas **não são limites agronômicos validados** e não diferenciam culturas, fases de desenvolvimento ou tipos de solo. Esse aviso também aparece na interface.
- Os dados do INMET cobrem apenas jan–out/2026 e vêm de um arquivo embutido. Não há consulta online, e as estações podem estar longe da propriedade (Contagem, por exemplo, usa a estação de Belo Horizonte).
- A **umidade do solo dos registros importados é simulada**, porque o INMET não a mede. Medições reais exigiriam sensores de solo.
- Não há integração com sensores nem APIs climáticas em tempo real.
- As ocorrências de campo são informativas e não alteram o índice numérico.
- O recomendador da Sprint 1 usa dados climáticos e de culturas **ilustrativos** (3 regiões, 6 culturas), que precisam ser substituídos por fontes oficiais (INMET, ZARC/Embrapa) antes de uso real.
- O módulo de compatibilidade de culturas compara o clima real do município, vindo da estação do INMET, com uma **base de requisitos simulada**. Sem estação no município, usa os indicadores registrados nos talhões. Umidade do solo (não medida pelo INMET), pH, textura e fertilidade não entram na comparação com o INMET.
- A persistência em JSON é adequada para uso local por um usuário; não há controle de acesso nem uso simultâneo por várias pessoas.

## 14. Integrantes do projeto

Conforme a documentação original do repositório:

- README original: Arthur, Brener, João, Raissa e Tayná — Professora: Rafaela.
- Visão do produto: Raissa, Tayná, João, Arthur Henrique, Brenner.

## Documentação

- Requisitos funcionais: `docs/requisitos/requisitos_funcional.md`
- Requisitos não funcionais: `docs/requisitos/requisitos_nao_funcionais.md`
- Visão do produto: `docs/requisitos/visao-do-produto.md`
- Arquitetura: `docs/arquitetura/arquitetura-inicial.md`
- Sprint 1 (recomendador por região): `docs/sprint-1-cli-java.md`
- Relatório da Sprint 2: `docs/relatorio-sprint-2.md`
- Pitch: `docs/pitch-smfield.md`
- Evidências (telas e roteiro de teste): `docs/evidencias/`

## Licença

MIT — ver `LICENSE`.
