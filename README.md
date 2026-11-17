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
| RF-01 | Cadastrar, editar e excluir propriedades (nome, município, identificador único) | Aba **2. Propriedades** |
| RF-02 | Cadastrar talhões vinculados a uma propriedade (nome, área em ha, cultura) | Aba **3. Talhões** |
| RF-03 | Listar propriedades e consultar os talhões de cada uma | Abas **2. Propriedades** e **3. Talhões** |
| RF-04 | Registrar indicadores ambientais (umidade do solo, temperatura, umidade do ar, precipitação 24 h, data e hora) | Aba **4. Indicadores ambientais** |
| RF-05 | Calcular o índice de risco (0–100) | Automático ao salvar indicadores |
| RF-06 | Classificar o risco: BAIXO, MÉDIO, ALTO ou NÃO CALCULADO | Todas as telas de risco |
| RF-07 | Painel de riscos ordenado por criticidade, com cores | Aba **1. Dashboard** |
| RF-08 | Consultar indicadores, índice, classificação e justificativa de um talhão | Aba **Consulta de risco** (ou duplo clique no Dashboard) |
| RF-09* | Editar e excluir talhões (com confirmação e remoção dos registros vinculados) | Aba **3. Talhões** |
| RF-10* | Registrar ocorrências de campo (praga, doença, falta de irrigação, excesso de água, outra) | Aba **5. Ocorrências** |
| RF-11* | Histórico de indicadores e riscos (registros anteriores preservados) | Aba **6. Histórico e alertas** |
| RF-12* | Alertas internos (risco alto, mudança de classificação, talhão sem indicadores) | Aba **6. Histórico e alertas** |
| Extra (P3) | Compatibilidade de culturas por município — **módulo demonstrativo com base simulada** | Aba **Compatibilidade (demo)** |

\* Requisitos complementares derivados da visão do produto (`docs/requisitos/visao-do-produto.md`).

Os dados são gravados em arquivos JSON locais e continuam disponíveis ao fechar e abrir o programa.

## 3. Tecnologias

| Componente | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Build e dependências | Maven |
| Interface gráfica | Java Swing |
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

**Pelo terminal (na pasta do projeto):**

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

Resultado medido nesta entrega (`mvn clean verify`): **148 testes executados, 0 falhas, 0 erros** — 133 execuções JUnit (83 métodos de teste, sendo 9 parametrizados) e **15 cenários Cucumber / 77 passos**.

Relatórios gerados:

- JUnit: `target/surefire-reports/`
- Cucumber: `target/cucumber-report.html`

## 9. Como verificar a cobertura

Após `mvn clean verify`, abra `target/site/jacoco/index.html` no navegador.

Cobertura medida nesta entrega (linhas):

| Pacote | Linhas | Ramos |
|---|---|---|
| `service` | 99,2% | 94,8% |
| `domain` | 98,9% | 95,3% |
| `infra` | 94,9% | 86,2% |
| `ui` | 0% (interface Swing não tem testes automatizados no build) | 0% |
| **Total do projeto** | 36,5% | 48,1% |

A regra do JaCoCo no `pom.xml` exige no mínimo **80% de linhas** nos pacotes `service` e `domain` (RNF-06). A interface foi validada por um roteiro executado clicando nos componentes reais (ver `docs/evidencias/`).

## 10. Estrutura de pacotes

```
SMField/
├── pom.xml
├── src/main/java/
│   ├── domain/   entidades e regras invariantes (Propriedade, Talhao, IndicadorAmbiental, NivelRisco, ResultadoRisco...)
│   ├── service/  regras de negócio (cadastros, motor de risco, monitoramento, alertas, demonstração)
│   ├── infra/    persistência JSON (JsonRepository e repositórios por entidade)
│   └── ui/       interface Swing (AppMain, MainFrame e painéis)
├── src/test/java/          testes JUnit (domain, service, infra) e passos Cucumber (bdd)
├── src/test/resources/features/   cenários Gherkin (.feature)
└── docs/                   requisitos, arquitetura, relatório, pitch e evidências
```

Detalhes em `docs/arquitetura/arquitetura-inicial.md`.

## 11. Dados de demonstração

No menu `Arquivo > Carregar dados de demonstração (SIMULADOS)` — ou no botão do Dashboard, quando ainda não carregados — o sistema cria:

- 3 propriedades e 6 talhões (milho, soja e café) em municípios fictícios;
- indicadores que geram talhões de risco **alto**, **médio** e **baixo**, um talhão **sem indicadores** e históricos com mudança de classificação;
- 3 ocorrências de campo.

Todos os registros têm o prefixo **[SIMULADO]** e servem apenas para uso acadêmico. A carga não é duplicada: se já tiver sido feita, o sistema avisa e não cria registros novamente.

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
- Os dados são informados manualmente (ou simulados); não há integração com sensores, estações meteorológicas ou APIs climáticas.
- As ocorrências de campo são informativas e não alteram o índice numérico.
- O módulo de compatibilidade de culturas usa uma **base de requisitos simulada** e só considera os indicadores registrados (temperatura, umidades e precipitação); pH, textura e fertilidade do solo não fazem parte desta versão.
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
- Relatório da Sprint 2: `docs/relatorio-sprint-2.md`
- Pitch: `docs/pitch-smfield.md`
- Evidências (telas e roteiro de teste): `docs/evidencias/`

## Licença

MIT — ver `LICENSE`.
