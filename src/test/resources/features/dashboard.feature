# language: pt
Funcionalidade: Painel de riscos e histórico
  Como gestor agrícola
  Quero visualizar os talhões ordenados por risco
  Para priorizar as áreas mais críticas

  Contexto:
    Dado um sistema SMField sem dados cadastrados

  Cenário: Dashboard com talhões de riscos diferentes
    Dado os seguintes talhões e indicadores na propriedade "Fazenda Modelo":
      | talhao       | cultura | solo | temperatura | umidadeAr | precipitacao |
      | Talhão Baixo | Soja    | 55   | 24          | 65        | 12           |
      | Talhão Médio | Milho   | 30   | 33          | 30        | 2            |
      | Talhão Alto  | Café    | 15   | 41          | 15        | 0            |
    E um talhão "Talhão Novo" sem indicadores
    Quando abrir o dashboard
    Então os talhões deverão aparecer nesta ordem com estas classificações:
      | talhao       | classificacao |
      | Talhão Alto  | ALTO          |
      | Talhão Médio | MEDIO         |
      | Talhão Baixo | BAIXO         |
      | Talhão Novo  | NAO_CALCULADO |
    E deverá existir um alerta de "RISCO_ALTO" para o talhão "Talhão Alto"
    E deverá existir um alerta de "SEM_INDICADORES" para o talhão "Talhão Novo"

  Cenário: Histórico preserva medições anteriores e dados persistem após reabrir
    Dado um talhão "Talhão A" de cultura "Milho" na propriedade "Fazenda Modelo"
    E indicadores dentro das faixas ideais: solo 55, temperatura 24, umidade do ar 65 e precipitação 12
    E indicadores críticos: solo 15, temperatura 41, umidade do ar 15 e precipitação 0
    Quando o sistema for fechado e aberto novamente
    Então o histórico do talhão deverá ter 2 registros
    E o último registro deverá indicar mudança de "BAIXO" para "ALTO"
