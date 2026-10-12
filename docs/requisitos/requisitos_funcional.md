# Requisitos Funcionais — SMField

## 1. Objetivo

Este documento apresenta os requisitos funcionais do SMField, um sistema de monitoramento e análise de riscos agrícolas. O objetivo é definir as funcionalidades essenciais da primeira versão do sistema (MVP).

## 2. Requisitos funcionais

| ID    | Requisito                        | Descrição                                                                                                         |
| ----- | -------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| RF-01 | Cadastrar propriedade            | O sistema deve permitir cadastrar uma propriedade rural com nome e município.                                     |
| RF-02 | Cadastrar talhão                 | O sistema deve permitir cadastrar talhões vinculados a uma propriedade, informando nome, área e cultura.          |
| RF-03 | Listar propriedades e talhões    | O sistema deve permitir visualizar as propriedades e os talhões cadastrados.                                      |
| RF-04 | Registrar indicadores ambientais | O sistema deve permitir registrar umidade do solo, temperatura, umidade do ar e precipitação para um talhão.      |
| RF-05 | Calcular índice de risco         | O sistema deve calcular um índice de risco agrícola entre 0 e 100 com base nos indicadores registrados.           |
| RF-06 | Classificar nível de risco       | O sistema deve classificar o risco como baixo, médio ou alto.                                                     |
| RF-07 | Exibir painel de riscos          | O sistema deve apresentar os talhões cadastrados e seus respectivos níveis de risco.                              |
| RF-08 | Consultar dados e justificativa  | O sistema deve permitir consultar os indicadores registrados e os fatores considerados na classificação do risco. |

## 3. Regras de negócio

* **RN-01:** Todo talhão deve estar vinculado a uma propriedade cadastrada.
* **RN-02:** O índice de risco deve estar entre 0 e 100.
* **RN-03:** O risco será classificado como baixo (0 a 33), médio (34 a 66) ou alto (67 a 100).
* **RN-04:** O sistema não deve aceitar valores inválidos para os indicadores ambientais.
* **RN-05:** Quando não houver dados suficientes para o cálculo, o sistema deverá informar que o risco não pode ser calculado.
* **RN-06:** A classificação deverá apresentar uma justificativa baseada nos indicadores utilizados.

## 4. Fora do escopo do MVP

As seguintes funcionalidades poderão ser implementadas em versões futuras:

* Integração com sensores e dispositivos IoT.
* Consulta automática a serviços meteorológicos.
* Inteligência artificial para previsão de riscos.
* Envio de alertas por e-mail ou WhatsApp.
* Exportação de relatórios.
* Gráficos históricos e análise de tendências.

## 5. Observação

Os limites de classificação e a fórmula de cálculo do risco são regras iniciais para fins acadêmicos. A fórmula deverá ser definida e testada antes da implementação, sem ser apresentada como uma recomendação agronômica validada.