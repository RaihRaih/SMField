# Arquitetura — SMField

> Documento atualizado na Sprint 2 para refletir o software implementado. A versão inicial previa as mesmas quatro camadas; os itens marcados como "decisão da Sprint 2" mudaram em relação ao planejamento original.

## 1. Objetivo

Organizar o código do SMField para facilitar o desenvolvimento, a manutenção e os testes, mantendo as regras de negócio separadas da interface gráfica.

## 2. Camadas e responsabilidades

```
            ┌──────────────────────────────┐
            │ ui  (Java Swing)             │  telas, formulários, tabelas, cores
            └──────────────┬───────────────┘
                           │ chama
            ┌──────────────▼───────────────┐
            │ service (regras de negócio)  │  validações de cadastro, motor de risco,
            │                              │  monitoramento, alertas, histórico
            └──────┬───────────────┬───────┘
                   │ usa           │ usa
     ┌─────────────▼─────┐   ┌─────▼──────────────────┐
     │ infra (JSON)      │──▶│ domain (entidades)     │
     │ repositórios      │   │ invariantes e enums    │
     └───────────────────┘   └────────────────────────┘
```

| Pacote | Responsabilidade | Pode depender de |
|---|---|---|
| `domain` | Entidades, enums e validações invariantes (ex.: área > 0, umidade 0–100) | nada além do JDK |
| `infra` | Leitura e gravação dos arquivos JSON | `domain`, Gson |
| `service` | Regras de negócio, cálculo e consolidação de dados | `domain`, `infra` |
| `ui` | Interação com o usuário; não contém cálculo de risco nem validação de regras | `service`, `domain` (somente leitura) |

Não há dependências circulares: `domain` não conhece nenhuma outra camada e `infra` não conhece `service` nem `ui`.

## 3. Classes implementadas

### `domain` (15 tipos)

| Classe | Papel |
|---|---|
| `Propriedade` | Propriedade rural (id, nome, município). Classe original do repositório, com o pacote corrigido. |
| `Talhao` | Área agrícola vinculada a uma propriedade (nome, área em ha, cultura). |
| `IndicadorAmbiental` | Registro imutável de umidade do solo, temperatura, umidade do ar, precipitação 24 h e data/hora. |
| `NivelRisco` | Enum BAIXO / MEDIO / ALTO / NAO_CALCULADO e regra de classificação por índice. |
| `ResultadoRisco` | Índice, nível, fatores e justificativa de um cálculo. |
| `FatorRisco` | Contribuição de um indicador (valor, faixa, penalidade, peso, pontos). |
| `OcorrenciaCampo` | Ocorrência observada em campo. |
| `TipoOcorrencia` | Enum de tipos de ocorrência. |
| `RegistroHistorico` | Item do histórico: indicador + resultado + nível anterior. |
| `Alerta` / `TipoAlerta` | Alerta interno e seus tipos. |
| `RequisitoCultura` | Faixas simuladas de uma cultura (módulo demonstrativo). |
| `Identificavel` | Contrato de entidade com id. |
| `Validacao` / `ValidacaoException` | Funções de validação e exceção com mensagem para o usuário. |

### `service` (10 classes)

| Classe | Papel |
|---|---|
| `PropriedadeService` | RF-01/RF-03: cadastro, edição, exclusão (bloqueada se houver talhões) e consulta. |
| `TalhaoService` | RF-02/RF-03/RF-09: cadastro vinculado (RN-01), edição, exclusão em cascata, contagem de vínculos. |
| `IndicadorService` | RF-04/RF-11: registro com validação completa e histórico cronológico. |
| `RiscoService` | RF-05/RF-06/RF-08: motor de risco, classificação e justificativa. |
| `MonitoramentoService` | RF-07/RF-08/RF-11/RF-12: painel, resumo, detalhe, histórico e alertas. |
| `OcorrenciaService` | RF-10: ocorrências de campo. |
| `CompatibilidadeService` | Módulo demonstrativo de compatibilidade de culturas por município. |
| `DadosDemonstracaoService` | Carga idempotente dos dados SIMULADOS. |
| `ConversorEntrada` | Conversão de texto digitado em número/data com mensagens claras. |
| `ServicosSMField` | Cria e compartilha os serviços e repositórios (mesma fonte de dados para todas as telas). |

### `infra` (7 classes)

| Classe | Papel |
|---|---|
| `JsonRepository<T>` | Repositório genérico em arquivo JSON, com id sequencial e gravação atômica. |
| `PropriedadeRepository`, `TalhaoRepository`, `IndicadorRepository`, `OcorrenciaRepository` | Consultas específicas de cada entidade. |
| `ArmazenamentoLocal` | Agrupa os repositórios de uma pasta de dados. |
| `PersistenciaException` | Falha de leitura/gravação com mensagem clara. |

### `ui` (13 tipos)

`AppMain`, `MainFrame`, `DashboardPanel`, `PropriedadePanel`, `TalhaoPanel`, `IndicadoresPanel`, `DetalhesRiscoPanel`, `OcorrenciasPanel`, `HistoricoAlertasPanel`, `CompatibilidadePanel`, `ResultadoRiscoView`, `Estilo` (paleta e utilitários visuais) e `Atualizavel` (contrato de recarga das telas).

## 4. Fluxo dos dados

1. O usuário preenche um formulário na `ui`.
2. A tela envia o texto digitado ao serviço (ex.: `IndicadorService.registrarTexto`). `ConversorEntrada` rejeita letras, `NaN` e infinitos; as entidades de `domain` validam faixas e obrigatoriedade.
3. O serviço grava pelo repositório de `infra`, que atualiza o arquivo JSON.
4. A tela chama `MainFrame.notificarAlteracao()`, e todas as telas recarregam seus dados a partir dos serviços (dashboard, histórico e alertas ficam sempre coerentes com o que foi salvo).
5. Para exibir risco, `MonitoramentoService` busca o último registro de cada talhão e pede o cálculo ao `RiscoService`.

```
Cadastro → Indicadores → RiscoService (índice + nível + justificativa) → MonitoramentoService → Dashboard / Consulta / Alertas
```

## 5. Estratégia de persistência

- **Formato:** um arquivo JSON por entidade na pasta `dados/` (configurável com `-Dsmfield.dados=...`): `propriedades.json`, `talhoes.json`, `indicadores.json`, `ocorrencias.json`.
- **Estrutura:** `{"proximoId": n, "itens": [...]}`. O identificador é sequencial e **nunca é reutilizado**, mesmo após exclusões.
- **Gravação segura:** cada alteração grava um arquivo temporário e o substitui atomicamente.
- **Histórico:** cada registro de indicadores é um novo item; nada é sobrescrito. O risco de cada registro é recalculado de forma determinística a partir dos valores gravados.
- **Vínculos:** talhões guardam `propriedadeId`; indicadores e ocorrências guardam `talhaoId`. Ao excluir um talhão, seus indicadores e ocorrências são removidos (após confirmação do usuário, que vê a quantidade). Uma propriedade só pode ser excluída se não tiver talhões.
- **Duplicidades:** não são aceitas propriedades com o mesmo nome e município, nem talhões com o mesmo nome na mesma propriedade; a carga de demonstração não se repete.
- **Arquivo corrompido:** o sistema mostra uma mensagem clara indicando o arquivo, em vez de falhar silenciosamente.

## 6. Principais decisões técnicas

| Decisão | Motivo |
|---|---|
| Java Swing | Previsto na arquitetura inicial; sem dependências externas e simples de demonstrar. |
| JSON com Gson em vez de banco de dados | Funciona offline, sem instalação; os arquivos podem ser abertos para conferência. (Decisão da Sprint 2: substitui o `MemoriaPropriedadeRepository` previsto, que perderia os dados ao fechar.) |
| Cálculo com aritmética inteira (`penalidade × peso%`) | Evita erros de ponto flutuante: resultado determinístico e arredondamento exato. |
| `NAO_CALCULADO` como estado separado | Atende RN-05: falta de dados nunca é exibida como risco baixo. |
| Cálculo de risco só na camada `service` | Atende RNF-03; a interface apenas exibe o `ResultadoRisco`. |
| `Clock` injetável nos serviços | Testes reproduzíveis para datas ("não futura", histórico). |
| Exclusão em cascata com confirmação | Evita registros órfãos e deixa o impacto visível antes da ação. |
| Precipitação = acumulado das últimas 24 h | Período de referência definido para tornar a regra objetiva. |
| Cucumber em português (`# language: pt`) | Cenários legíveis para a equipe e para a professora. |
