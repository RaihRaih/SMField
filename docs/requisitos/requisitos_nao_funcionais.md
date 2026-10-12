# Requisitos Não Funcionais — SMField

## 1. Objetivo

Este documento define os requisitos de qualidade, organização e desenvolvimento do SMField.

## 2. Requisitos não funcionais

| ID     | Requisito                      | Descrição                                                                                                                                          |
| ------ | ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| RNF-01 | Tecnologia                     | O sistema deverá ser desenvolvido em Java utilizando Maven para gerenciamento do projeto.                                                          |
| RNF-02 | Organização do código          | O projeto deverá ser organizado nas camadas `domain`, `service`, `infra` e `ui`.                                                                   |
| RNF-03 | Separação de responsabilidades | As regras de negócio deverão ser separadas da interface gráfica.                                                                                   |
| RNF-04 | Testes unitários               | As principais regras de negócio deverão possuir testes automatizados utilizando JUnit.                                                             |
| RNF-05 | Testes de comportamento        | O projeto deverá possuir pelo menos cinco cenários automatizados utilizando Cucumber e Gherkin.                                                    |
| RNF-06 | Cobertura de testes            | O projeto deverá buscar uma cobertura mínima de 80% nas classes de serviço e regras de negócio, utilizando uma ferramenta de medição de cobertura. |
| RNF-07 | Validação de dados             | O sistema deverá validar as entradas e impedir o processamento de valores inválidos.                                                               |
| RNF-08 | Versionamento e documentação   | O código-fonte e a documentação deverão ser mantidos no GitHub, com instruções de execução no arquivo README.md.                                   |

## 3. Critérios de qualidade

O sistema será considerado adequado quando:

* As funcionalidades principais estiverem funcionando.
* As entradas inválidas forem tratadas corretamente.
* As regras de cálculo de risco possuírem testes automatizados.
* Os cenários de comportamento definidos estiverem automatizados.
* A estrutura do projeto respeitar a separação de responsabilidades.
* A documentação permitir que outro integrante da equipe execute o sistema.

## 4. Observação

A cobertura de testes deverá ser verificada por uma ferramenta apropriada. A meta de 80% representa um critério de qualidade do projeto, não um resultado já alcançado.
