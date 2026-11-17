# Requisitos Funcionais — SMField

## 1. Objetivo

Este documento apresenta os requisitos funcionais do SMField, um sistema de monitoramento e análise de riscos agrícolas, e o **status real de implementação** ao final da Sprint 2.

## 2. Requisitos funcionais do MVP

| ID | Requisito | Descrição | Status | Classe responsável | Tela | Testes |
|---|---|---|---|---|---|---|
| RF-01 | Cadastrar propriedade | Cadastrar propriedade rural com identificador único, nome e município (obrigatórios, sem aceitar texto vazio ou só com espaços; sem duplicar nome + município). | Implementado | `Propriedade`, `PropriedadeService`, `PropriedadeRepository` | Propriedades | `PropriedadeTest`, `PropriedadeServiceTest`, `cadastro.feature` |
| RF-02 | Cadastrar talhão | Cadastrar talhão vinculado a uma propriedade existente, com nome, área (> 0 ha) e cultura obrigatórios. | Implementado | `Talhao`, `TalhaoService`, `TalhaoRepository` | Talhões | `TalhaoTest`, `TalhaoServiceTest`, `cadastro.feature` |
| RF-03 | Listar propriedades e talhões | Visualizar propriedades, selecionar uma e consultar nome, área e cultura de seus talhões; listas atualizadas após cadastros, alterações e exclusões; mensagens quando não há registros. | Implementado | `PropriedadeService`, `TalhaoService` | Propriedades, Talhões | `PropriedadeServiceTest`, `TalhaoServiceTest` |
| RF-04 | Registrar indicadores ambientais | Registrar umidade do solo (%), temperatura (°C), umidade do ar (%), precipitação acumulada nas últimas 24 h (mm) e data/hora para um talhão. | Implementado | `IndicadorAmbiental`, `IndicadorService`, `ConversorEntrada`, `IndicadorRepository` | Indicadores ambientais | `IndicadorAmbientalTest`, `IndicadorServiceTest`, `ConversorEntradaTest`, `risco.feature` |
| RF-05 | Calcular índice de risco | Índice de 0 a 100, determinístico, documentado e explicável (pesos e faixas na seção 4). | Implementado | `RiscoService` | Indicadores, Consulta de risco, Dashboard | `RiscoServiceTest`, `risco.feature` |
| RF-06 | Classificar nível de risco | BAIXO (0–33), MÉDIO (34–66), ALTO (67–100); NÃO CALCULADO quando faltam dados. | Implementado | `NivelRisco`, `RiscoService` | Todas as telas de risco | `NivelRiscoTest`, `risco.feature` (esquema com 0, 33, 34, 66, 67 e 100) |
| RF-07 | Exibir painel de riscos | Propriedade, talhão, cultura, último índice, classificação colorida e data da última atualização, ordenados do maior para o menor risco. | Implementado | `MonitoramentoService` | Dashboard | `MonitoramentoServiceTest`, `dashboard.feature` |
| RF-08 | Consultar dados e justificativa | Indicadores, índice, classificação, data e justificativa baseada nos indicadores efetivamente fora da faixa. | Implementado | `MonitoramentoService`, `RiscoService` | Consulta de risco | `RiscoServiceTest`, `MonitoramentoServiceTest`, `risco.feature` |

## 3. Requisitos complementares (derivados da visão do produto)

O documento `visao-do-produto.md` inclui no escopo do MVP itens que não constavam da tabela original. Eles foram tratados como requisitos complementares:

| ID | Requisito | Descrição | Status | Classe responsável | Tela | Testes |
|---|---|---|---|---|---|---|
| RF-09 | Editar e excluir talhões | Alterar nome, área e cultura; excluir com confirmação. Estratégia para registros vinculados: a tela informa quantos indicadores e ocorrências serão removidos e, se confirmado, a exclusão é feita em cascata. | Implementado | `TalhaoService` | Talhões | `TalhaoServiceTest` |
| RF-10 | Registrar ocorrências de campo | Tipo (praga, doença, falta de irrigação, excesso de água, outra), descrição, data (não futura) e talhão; exibidas na consulta do talhão. Não alteram o índice numérico. | Implementado | `OcorrenciaCampo`, `OcorrenciaService` | Ocorrências, Consulta de risco | `OcorrenciaCampoTest`, `OcorrenciaServiceTest` |
| RF-11 | Histórico de indicadores e riscos | Cada novo registro preserva os anteriores; tabela cronológica com índice, classificação e variação. | Implementado | `IndicadorService`, `MonitoramentoService`, `RegistroHistorico` | Histórico e alertas | `IndicadorServiceTest`, `MonitoramentoServiceTest`, `JsonRepositoryTest`, `dashboard.feature` |
| RF-12 | Alertas internos | Risco alto, mudança de classificação no último registro e talhão sem indicadores, exibidos no próprio sistema. | Implementado | `MonitoramentoService`, `Alerta` | Histórico e alertas | `MonitoramentoServiceTest`, `dashboard.feature` |

### Módulo demonstrativo (alinhamento ao pré-projeto)

| ID | Requisito | Status | Observação |
|---|---|---|---|
| RD-01 | Compatibilidade de culturas por município | Parcial (demonstrativo) | Compara as médias dos indicadores registrados nos talhões do município com uma base **SIMULADA** de faixas por cultura e informa quando há dados insuficientes (mínimo de 3 registros). Não considera pH, textura nem fertilidade do solo e não é recomendação agronômica. Classe: `CompatibilidadeService`; tela: Compatibilidade (demo); teste: `CompatibilidadeServiceTest`. |

## 4. Regras de negócio

| ID | Regra | Onde é garantida | Testes |
|---|---|---|---|
| RN-01 | Todo talhão deve estar vinculado a uma propriedade cadastrada. | `Talhao` (id > 0) e `TalhaoService.cadastrar` (propriedade existe) | `TalhaoTest`, `TalhaoServiceTest`, `cadastro.feature` |
| RN-02 | O índice de risco deve estar entre 0 e 100. | `RiscoService` (aritmética inteira e limite) e `NivelRisco.classificar` | `RiscoServiceTest` (14.256 combinações de valores), `NivelRiscoTest` |
| RN-03 | Baixo (0–33), médio (34–66), alto (67–100). | `NivelRisco.classificar` | `NivelRiscoTest`, `risco.feature` |
| RN-04 | Valores inválidos não são aceitos nos indicadores. | `ConversorEntrada` (letras, NaN, infinito) e `IndicadorAmbiental` (faixas) | `IndicadorAmbientalTest`, `IndicadorServiceTest`, `ConversorEntradaTest`, `risco.feature` |
| RN-05 | Sem dados suficientes, o risco não é calculado e o sistema informa claramente. | `IndicadorService.registrar` (exige os quatro) e `RiscoService` (`NAO_CALCULADO`) | `RiscoServiceTest`, `IndicadorServiceTest`, `risco.feature` |
| RN-06 | Toda classificação calculada apresenta justificativa baseada nos fatores que contribuíram. | `RiscoService.justificar` | `RiscoServiceTest`, `risco.feature` |

### Modelo de cálculo (RF-05)

| Indicador | Peso | Penalidade 0 | Penalidade 50 | Penalidade 100 |
|---|---|---|---|---|
| Umidade do solo | 35% | 40–70% | 25–<40% ou >70–85% | <25% ou >85% |
| Temperatura | 30% | 18–30 °C | 10–<18 °C ou >30–38 °C | <10 °C ou >38 °C |
| Umidade do ar | 15% | 40–80% | 20–<40% ou >80–90% | <20% ou >90% |
| Precipitação (últimas 24 h) | 20% | 5–30 mm | 0–<5 mm ou >30–100 mm | >100 mm |

`risco = solo × 0,35 + temperatura × 0,30 + umidade do ar × 0,15 + precipitação × 0,20`, arredondado para o inteiro mais próximo (0,5 para cima).

Faixas aceitas no registro: umidades 0–100%; temperatura −20 a 60 °C (faixa operacional); precipitação 0 a 500 mm.

## 5. Fora do escopo do MVP

* Integração com sensores e dispositivos IoT.
* Consulta automática a serviços meteorológicos.
* Inteligência artificial para previsão de riscos.
* Envio de alertas por e-mail ou WhatsApp.
* Exportação de relatórios.
* Gráficos históricos avançados e análise de tendências (o histórico é apresentado em tabela cronológica).

## 6. Observação

Os limites de classificação e a fórmula de cálculo do risco são regras acadêmicas, definidas e testadas para demonstrar a lógica do software. **Não representam recomendação agronômica validada** para todas as culturas.
