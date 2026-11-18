# Requisitos Não Funcionais — SMField

## 1. Objetivo

Este documento define os requisitos de qualidade, organização e desenvolvimento do SMField e registra **como cada um foi atendido e verificado** ao final da Sprint 2.

## 2. Requisitos não funcionais

| ID | Requisito | Descrição | Status | Evidência |
|---|---|---|---|---|
| RNF-01 | Tecnologia | Java e Maven. | Atendido | `pom.xml` (Java 21, `maven.compiler.release` = 21); `mvn clean verify` executado com sucesso. |
| RNF-02 | Organização do código | Camadas `domain`, `service`, `infra` e `ui`. | Atendido (Sprint 2) | `src/main/java/{domain,service,infra,ui}` — 50 tipos Java. O módulo da Sprint 1 permanece no pacote `smfield`, fora das quatro camadas; ver `docs/arquitetura/arquitetura-inicial.md`. |
| RNF-03 | Separação de responsabilidades | Regras de negócio separadas da interface. | Atendido | Cálculo, classificação e validações ficam em `domain`/`service`; o pacote `ui` apenas chama serviços e exibe resultados. |
| RNF-04 | Testes unitários | Principais regras com testes JUnit. | Atendido | Sprint 2: 102 métodos de teste JUnit 5 (9 parametrizados), totalizando 152 execuções. Sprint 1: 23 testes. 0 falhas. |
| RNF-05 | Testes de comportamento | Pelo menos cinco cenários Cucumber/Gherkin automatizados. | Atendido | 4 arquivos `.feature`, 17 cenários executados (11 cenários + 6 exemplos de esquema), 89 passos, todos aprovados no `mvn verify`. |
| RNF-06 | Cobertura de testes | Buscar 80% nas classes de serviço e regras de negócio, com ferramenta de medição. | Atendido (medido) | JaCoCo: `service` 99,4% e `domain` 99,0% de linhas; regra `check` no `pom.xml` falha o build abaixo de 80%. Total do projeto: 45,1% (a interface Swing não possui testes automatizados; o pacote `smfield` da Sprint 1 tem 76,9%). |
| RNF-07 | Validação de dados | Validar entradas e impedir valores inválidos. | Atendido | Campos obrigatórios, faixas, letras, NaN, infinito, datas inválidas/futuras, duplicidades e talhão/propriedade inexistentes são rejeitados com mensagens claras (telas 02, 04, 06, 07 e 08 em `docs/evidencias/telas`). |
| RNF-08 | Versionamento e documentação | Código e documentação no GitHub, com instruções no README. | Atendido localmente | README com instruções de execução, testes e cobertura; histórico Git organizado em commits. A publicação no GitHub depende do `git push` da equipe (comandos no relatório). |

## 3. Critérios de qualidade

| Critério | Situação |
|---|---|
| As funcionalidades principais estão funcionando. | Sim — roteiro manual executado na interface real (`docs/evidencias`). |
| As entradas inválidas são tratadas corretamente. | Sim — testes unitários, cenários BDD e roteiro de interface. |
| As regras de cálculo de risco possuem testes automatizados. | Sim — `RiscoServiceTest` (46 execuções) e `risco.feature`. |
| Os cenários de comportamento estão automatizados. | Sim — executados pelo Maven via JUnit Platform. |
| A estrutura respeita a separação de responsabilidades. | Sim — ver arquitetura. |
| A documentação permite que outro integrante execute o sistema. | Sim — README (terminal, NetBeans e IntelliJ). |

## 4. Observação

Os percentuais acima foram obtidos executando `mvn clean verify` e lendo o relatório `target/site/jacoco/jacoco.csv`. Se o código for alterado, os valores devem ser medidos novamente.
