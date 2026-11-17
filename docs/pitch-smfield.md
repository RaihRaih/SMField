# Pitch — SMField (Sprint 2)

**Duração sugerida:** 4 a 5 minutos (cerca de 3 min de slides + 1 a 2 min de demonstração)
**Material de apoio:** capturas de tela em `docs/evidencias/telas/`

> Use apenas o que está implementado e comprovado. Números citados abaixo vêm da execução real de `mvn clean verify`.

---

## Slide 1 — SMField

**Texto do slide**
- SMField — Monitoramento Inteligente e Análise de Riscos Agrícolas
- Centraliza talhões e indicadores e mostra quais áreas precisam de atenção primeiro
- Squad Ânima HUB — Gestão e Qualidade de Software

**Elemento visual:** captura `16-dashboard-com-dados-demo.png` (cartões vermelho/laranja/verde/cinza).

**Fala sugerida (≈25 s)**
"Boa tarde! Nós somos a equipe do SMField. Nosso sistema reúne as informações dos talhões de uma propriedade e transforma umidade, temperatura e chuva em um índice de risco de 0 a 100, com uma explicação clara do porquê. A ideia é simples: abrir o sistema e saber qual área visitar primeiro."

---

## Slide 2 — O problema

**Texto do slide**
- Muitos talhões, muitos indicadores
- Dados espalhados em anotações e planilhas
- Difícil perceber cedo quais áreas estão em condição desfavorável

**Elemento visual:** ícones de planilha, caderno e mapa de talhões, com um ponto de interrogação.

**Fala sugerida (≈25 s)**
"Uma propriedade pode ter vários talhões, cada um com sua cultura e suas condições. Quando umidade do solo, temperatura, umidade do ar e chuva ficam em anotações separadas, é difícil comparar as áreas e perceber a tempo onde o risco está aumentando."

---

## Slide 3 — A solução

**Texto do slide**
- Cadastro de propriedades e talhões
- Registro de indicadores ambientais com validação
- Índice de risco 0–100, classificação por cores e justificativa
- Histórico, alertas e ocorrências de campo

**Elemento visual:** captura `13-consulta-justificativa-registro-2.png`.

**Fala sugerida (≈30 s)**
"O SMField centraliza tudo: propriedades, talhões, indicadores e ocorrências. A cada registro, o sistema calcula o risco, classifica como baixo, médio ou alto e escreve a justificativa — por exemplo, 'umidade do solo muito abaixo da faixa de referência'. E guarda o histórico, para acompanharmos a evolução."

---

## Slide 4 — Como funciona

**Texto do slide**
- Cadastro → Indicadores → Cálculo → Classificação → Dashboard
- Pesos: solo 35% · temperatura 30% · umidade do ar 15% · chuva (24 h) 20%
- Cada indicador: 0, 50 ou 100 pontos conforme a faixa
- Baixo 0–33 · Médio 34–66 · Alto 67–100 · Sem dados = Não calculado

**Elemento visual:** fluxograma com cinco caixas e a fórmula abaixo. Opcional: uma faixa "Sprint 1: quando plantar na região → Sprint 2: como está cada talhão".

**Fala sugerida (≈40 s)**
"O cálculo é determinístico e explicável. Cada indicador ganha 0, 50 ou 100 pontos conforme está dentro, perto ou longe da faixa de referência, e entra na média com seu peso. Por exemplo: solo a 22%, temperatura de 36 graus, umidade do ar 35% e nenhuma chuva somam 68 pontos — risco alto. E se faltar um indicador, o sistema não inventa: mostra 'não calculado'. Importante: as faixas são hipóteses acadêmicas para demonstrar a lógica, não recomendações agronômicas."

---

## Slide 5 — Demonstração

**Texto do slide**
- Cadastro → registro → risco → justificativa → dashboard → histórico
- Dados preservados ao fechar e abrir

**Elemento visual:** a própria aplicação (ou, em caso de imprevisto, as capturas 03, 05, 09, 13, 11 e 14 em sequência).

**Fala sugerida:** narrar o roteiro abaixo.

---

## Slide 6 — Qualidade do software

**Texto do slide**
- Arquitetura em 4 camadas: domain · service · infra · ui
- 171 testes automatizados aprovados (JUnit 5 das duas sprints + 15 cenários Cucumber em português)
- Cobertura medida com JaCoCo: 99,2% em service e 98,9% em domain
- Build único: `mvn clean verify`

**Elemento visual:** diagrama das camadas + print do relatório JaCoCo (`target/site/jacoco/index.html`) + trecho de um `.feature`.

**Fala sugerida (≈35 s)**
"Por trás da interface, separamos o sistema em quatro camadas: a interface não faz cálculo nenhum, só chama os serviços. Isso permitiu testar as regras de verdade: são 171 testes automatizados, todos passando, somando as duas sprints, incluindo 15 cenários de comportamento escritos em português com Cucumber. A cobertura das regras de negócio passa de 98%, e o próprio build falha se ela cair abaixo de 80%. A interface não tem testes automatizados; validamos o roteiro completo nela com capturas de tela."

---

## Slide 7 — Benefícios e conclusão

**Texto do slide**
- Prioriza as áreas críticas em segundos
- Explica o porquê de cada classificação
- Mantém histórico e alertas
- Próximos passos: dados meteorológicos, sensores e faixas por cultura validadas

**Elemento visual:** captura `17-historico-alertas-demo.png`.

**Fala sugerida (≈30 s)**
"Com o SMField, o gestor abre o painel e já vê, em vermelho, onde agir primeiro — e sabe por quê. Nesta versão os dados são manuais ou simulados; como evolução, queremos integrar dados meteorológicos e sensores e construir faixas por cultura com apoio agronômico. Obrigado!"

---

## Roteiro de demonstração do software (≈1 min 30 s)

**Preparação (antes da apresentação)**
1. Rodar `mvn clean verify` uma vez para gerar `target/smfield.jar`.
2. Começar com a pasta `dados/` vazia (ou usar `java -Dsmfield.dados=demo-apresentacao -jar target/smfield.jar`).
3. Deixar a janela maximizada.

**Passo a passo**

| # | Ação | O que dizer |
|---|---|---|
| 1 | Abrir o SMField (`java -jar target/smfield.jar`). | "Esta é a tela inicial, ainda sem dados." |
| 2 | Aba **2. Propriedades**: nome "Fazenda Modelo", município "Contagem", **Salvar propriedade**. | "Cadastro com validação: nome e município são obrigatórios." |
| 3 | Aba **3. Talhões**: propriedade "Fazenda Modelo", "Talhão A", área 12, cultura Milho, **Salvar talhão**. | "Todo talhão fica vinculado a uma propriedade." |
| 4 | Aba **4. Indicadores**: solo 55, temperatura 24, ar 65, precipitação 12, **Salvar e calcular risco**. | "Condições ideais: índice 0, risco baixo." |
| 5 | (Opcional) Digitar 150 na umidade do solo e salvar. | "Valores inválidos são rejeitados com mensagem clara." |
| 6 | Registrar a segunda medição: solo 22, temperatura 36, ar 35, precipitação 0. | "Agora o índice é 68: risco alto." |
| 7 | Aba **Consulta de risco**. | Ler a justificativa e mostrar a tabela de pontos por indicador. |
| 8 | Aba **6. Histórico e alertas**. | "As duas medições estão preservadas, e o sistema avisou a mudança de baixo para alto." |
| 9 | Fechar e abrir novamente o programa; ir ao **Dashboard**. | "Os dados continuam lá: ficam gravados em arquivos locais." |
| 10 | Menu **Arquivo > Carregar dados de demonstração** e voltar ao Dashboard. | "Com dados simulados vemos vários talhões ordenados do maior para o menor risco." |
| 11 (opcional) | No terminal: `java -cp target/smfield.jar smfield.Main agora --regiao "Belo Horizonte" --mes 10` | "E este é o recomendador da Sprint 1: ele sugere o que plantar em cada região e mês. Continua funcionando no mesmo projeto." |

**Valores fictícios:** os números usados para "Contagem" e os dados [SIMULADO] servem apenas para demonstração e não representam condições reais.

**Plano B:** se o computador da apresentação não abrir o programa, usar as capturas de `docs/evidencias/telas/` na ordem 01 → 03 → 05 → 09 → 12 → 13 → 14 → 15 → 16.
