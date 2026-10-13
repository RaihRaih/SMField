# Arquitetura Inicial — SMField

## 1. Objetivo

Organizar o código do SMField para facilitar o desenvolvimento, a manutenção e os testes.

## 2. Pacotes do sistema

* `domain`: entidades e conceitos do sistema.
* `service`: regras de negócio e cálculos.
* `infra`: armazenamento e acesso aos dados.
* `ui`: interface gráfica desenvolvida com Java Swing.

## 3. Classes principais

* `Propriedade`: representa uma propriedade rural.
* `Talhao`: representa uma área agrícola.
* `IndicadorAmbiental`: armazena os indicadores ambientais.
* `ResultadoRisco`: apresenta o índice, o nível e a justificativa do risco.
* `NivelRisco`: define os níveis baixo, médio e alto.
* `PropriedadeService`: gerencia operações de propriedades.
* `TalhaoService`: gerencia operações de talhões.
* `RiscoService`: calcula e classifica o risco.
* `PropriedadeRepository`: define operações de armazenamento.
* `MemoriaPropriedadeRepository`: armazena dados temporariamente em memória.
* `MainFrame`: janela principal.
* `DashboardPanel`: painel de riscos.

## 4. Tecnologias

* Java
* Maven
* Java Swing
* JUnit
* Cucumber

## 5. Decisões iniciais

O sistema terá quatro camadas separadas. As regras de negócio não dependerão da interface gráfica. O cálculo de risco será definido e testado antes de sua implementação.
