# Base real do INMET — origem e atualização

Esta pasta guarda a ferramenta usada para gerar a base climática real que o SMField usa na aba **Dados reais (INMET)**.

## Origem

- **Fonte:** dados históricos das estações meteorológicas automáticas do INMET (Instituto Nacional de Meteorologia), portal de dados históricos: https://portal.inmet.gov.br/dadoshistoricos
- **Período incluído no projeto:** 01/01/2026 a 31/10/2026 (arquivo anual `2026.zip`, com 634 estações).
- **Tratamento:** script `tratar_inmet.py` (Python + pandas), recebido de um colaborador da equipe. Ele converte os registros horários (UTC) para dias no horário de Brasília e descarta dias com menos de 20 horas válidas.

## Arquivos usados pelo programa

O SMField lê apenas os dois arquivos abaixo, embutidos em `src/main/resources/inmet/`:

| Arquivo | Conteúdo |
|---|---|
| `estacoes.csv` | catálogo: código, nome, UF, região, latitude, longitude, altitude, cobertura |
| `diario_consolidado.csv` | um dia por linha e estação: temperatura média e mínima, umidade relativa média do ar, chuva total do dia |

O ZIP bruto do INMET (cerca de 340 MB descompactado) **não** é versionado.

## Como atualizar a base

```bash
pip install pandas openpyxl
python tratar_inmet.py 2026.zip --saida saida_inmet
```

Depois, copie `saida_inmet/estacoes.csv` e `saida_inmet/diario_consolidado.csv` para `src/main/resources/inmet/` e rode `mvn clean verify`.

Para alimentar o recomendador da Sprint 1 (`regioes.json` com os 12 meses), é preciso processar vários anos completos juntos, por exemplo `python tratar_inmet.py 2023.zip 2024.zip 2025.zip`. Com apenas jan–out/2026, nenhuma estação tem os 12 meses e o `regioes.json` sai vazio.

## O que é real e o que é simulado no SMField

- **Reais (INMET):** temperatura média, umidade relativa do ar e chuva do dia.
- **Simulada:** umidade do solo. As estações do INMET não medem umidade do solo; o SMField a estima por uma fórmula acadêmica (ver `InmetService.umidadeSoloSimulada`).
- **Acadêmico:** o índice de risco e suas faixas de referência.
