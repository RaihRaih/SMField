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

### `domain` (17 tipos)

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
| `EstacaoMeteorologica` | Estação automática do INMET (código, nome, UF, coordenadas, cobertura). |
| `LeituraClimaticaDiaria` | Resumo diário real de uma estação (temperatura, umidade do ar, chuva). |
| `Identificavel` | Contrato de entidade com id. |
| `Validacao` / `ValidacaoException` | Funções de validação e exceção com mensagem para o usuário. |

### `service` (11 classes)

| Classe | Papel |
|---|---|
| `PropriedadeService` | RF-01/RF-03: cadastro, edição, exclusão (bloqueada se houver talhões) e consulta. |
| `TalhaoService` | RF-02/RF-03/RF-09: cadastro vinculado (RN-01), edição, exclusão em cascata, contagem de vínculos. |
| `IndicadorService` | RF-04/RF-11: registro com validação completa e histórico cronológico. |
| `RiscoService` | RF-05/RF-06/RF-08: motor de risco, classificação e justificativa. |
| `MonitoramentoService` | RF-07/RF-08/RF-11/RF-12: painel, resumo, detalhe, histórico e alertas. |
| `OcorrenciaService` | RF-10: ocorrências de campo. |
| `InmetService` | RF-13: busca de estações (com sugestão pelo município, ex.: "Sorriso/MT"), umidade do solo simulada, prévia e importação do clima real do INMET. |
| `CompatibilidadeService` | Módulo demonstrativo de compatibilidade de culturas por município, usando o clima real da estação do INMET quando há. |
| `DadosDemonstracaoService` | Carga idempotente da demonstração em Sorriso/MT, Rio Verde/GO e Patrocínio/MG, com clima real do INMET. |
| `ConversorEntrada` | Conversão de texto digitado em número/data com mensagens claras. |
| `ServicosSMField` | Cria e compartilha os serviços e repositórios (mesma fonte de dados para todas as telas). |

### `infra` (8 classes)

| Classe | Papel |
|---|---|
| `JsonRepository<T>` | Repositório genérico em arquivo JSON, com id sequencial e gravação atômica. |
| `PropriedadeRepository`, `TalhaoRepository`, `IndicadorRepository`, `OcorrenciaRepository` | Consultas específicas de cada entidade. |
| `ArmazenamentoLocal` | Agrupa os repositórios de uma pasta de dados. |
| `InmetRepository` | Leitura (somente consulta) da base real do INMET embutida em `src/main/resources/inmet/`, carregada uma vez por execução. |
| `PersistenciaException` | Falha de leitura/gravação com mensagem clara. |

### `ui` (18 tipos)

- **Janela e navegação:** `AppMain`, `MainFrame` e `BarraLateral` (menu lateral com logo, grupos de seções e recolher/expandir).
- **Telas:** `DashboardPanel`, `PropriedadePanel`, `TalhaoPanel`, `IndicadoresPanel`, `DetalhesRiscoPanel`, `OcorrenciasPanel`, `HistoricoAlertasPanel`, `InmetPanel`, `CompatibilidadePanel` e `ResultadoRiscoView`.
- **Design system:**
  - `Estilo`: paleta da marca, tipografia Inter, tema FlatLaf, botões, tabelas, selos e mensagens;
  - `Cartao`: superfície com cantos arredondados e sombra;
  - `Graficos`: medidor, rosca, barras e série temporal em Java2D;
  - `Icone`: ícones vetoriais.
- `Atualizavel`: contrato de recarga das telas.

As seções continuam num `JTabbedPane`, com a faixa de abas oculta. O menu lateral apenas seleciona a seção, de modo que o menu **Seções**, os atalhos `Ctrl+1…9` e a navegação entre telas (por exemplo, Dashboard → Consulta) não mudaram. Os recursos visuais ficam em `src/main/resources/ui/`: `marca/` (logo original e ícones) e `fontes/` (Inter e licença OFL).

### Módulo da Sprint 1 — pacote `smfield`

O recomendador de plantio por região, entregue na Sprint 1, foi mantido sem alterações:

- `Main` — linha de comando (`listar`, `recomendar`, `agora`, `colheita`);
- `Recomendador` — nota de 0 a 100 por mês de plantio;
- `Calendario` — colheita estimada;
- `DadosLoader` e `Json` — leitura de `src/main/resources/dados/*.json`;
- `Cultura`, `ClimaMes`, `Regiao`, `Janela` e `Sugestao` — records do modelo;
- `smfield.domain.Propriedade` — classe da Sprint 1.

Ele é independente das quatro camadas da Sprint 2: não usa nem é usado por `domain`, `service`, `infra` ou `ui`. Também é executado por linha de comando (`java -cp target/smfield.jar smfield.Main ...`). Integrá-lo às camadas, por exemplo como um serviço consumido pela interface, é uma evolução possível.

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

- **Base do INMET:** arquivos CSV somente leitura em `src/main/resources/inmet/`, embutidos no `.jar`. Os registros importados viram indicadores comuns em `indicadores.json`, com o campo `fonte` preenchido. Registros antigos sem esse campo continuam válidos e são tratados como "Registro manual".

## 6. Principais decisões técnicas

| Decisão | Motivo |
|---|---|
| Java Swing | Previsto na arquitetura inicial; simples de demonstrar. |
| FlatLaf para a aparência (modernização da interface) | Biblioteca de aparência para Swing (Apache 2.0, sem dependências), compatível com Java 8+. Moderniza campos, botões, tabelas e barras de rolagem sem trocar de framework e sem mexer em regras de negócio. Gráficos e ícones foram desenhados em Java2D para não adicionar outras bibliotecas. |
| Navegação lateral sobre o mesmo `JTabbedPane` | Menor risco técnico: preserva menu, atalhos, roteiros de teste e a comunicação entre telas. |
| JSON com Gson em vez de banco de dados | Funciona offline, sem instalação; os arquivos podem ser abertos para conferência. (Decisão da Sprint 2: substitui o `MemoriaPropriedadeRepository` previsto, que perderia os dados ao fechar.) |
| Cálculo com aritmética inteira (`penalidade × peso%`) | Evita erros de ponto flutuante: resultado determinístico e arredondamento exato. |
| `NAO_CALCULADO` como estado separado | Atende RN-05: falta de dados nunca é exibida como risco baixo. |
| Cálculo de risco só na camada `service` | Atende RNF-03; a interface apenas exibe o `ResultadoRisco`. |
| `Clock` injetável nos serviços | Testes reproduzíveis para datas ("não futura", histórico). |
| Exclusão em cascata com confirmação | Evita registros órfãos e deixa o impacto visível antes da ação. |
| Precipitação = acumulado das últimas 24 h | Período de referência definido para tornar a regra objetiva. |
| Cucumber em português (`# language: pt`) | Cenários legíveis para a equipe e para a professora. |
| Base do INMET embutida (CSV) em vez de API online | Funciona offline e é reproduzível na apresentação; o script de tratamento fica em `ferramentas/inmet/` para atualizar a base. |
| Umidade do solo simulada por fórmula | O INMET não mede umidade do solo; a fórmula é determinística, documentada e marcada como SIMULADA em cada registro. |
| Carregamento da base do INMET em segundo plano | A janela abre sem esperar a leitura dos 128 mil dias. |
