# language: pt
Funcionalidade: Registro de indicadores e cálculo do risco
  Como gestor agrícola
  Quero registrar indicadores ambientais e obter um índice de risco explicável
  Para identificar quais talhões precisam de atenção

  Contexto:
    Dado um sistema SMField sem dados cadastrados
    E um talhão "Talhão A" de cultura "Milho" na propriedade "Fazenda Modelo"

  Cenário: Cálculo de risco baixo
    Dado indicadores dentro das faixas ideais: solo 55, temperatura 24, umidade do ar 65 e precipitação 12
    Quando o sistema calcular o risco do talhão
    Então o índice deverá estar entre 0 e 33
    E a classificação deverá ser "BAIXO"
    E a justificativa deverá conter "não foram identificados desvios"

  Cenário: Cálculo de risco alto
    Dado indicadores críticos: solo 15, temperatura 41, umidade do ar 15 e precipitação 0
    Quando o sistema calcular o risco do talhão
    Então o índice deverá estar entre 67 e 100
    E a classificação deverá ser "ALTO"
    E a justificativa deverá conter "temperatura muito acima da faixa de referência"

  Cenário: Dados insuficientes
    Dado um talhão sem todos os indicadores registrados
    Quando solicitar a análise de risco com a temperatura ausente
    Então o sistema deverá informar que não há dados suficientes
    E a classificação deverá ser "NÃO CALCULADO"

  Cenário: Indicador inválido é rejeitado
    Dado um valor de umidade do solo igual a 150
    Quando registrar os indicadores
    Então o cadastro deverá ser rejeitado com a mensagem contendo "Umidade do solo"
    E o talhão deverá continuar sem registros de indicadores

  Esquema do Cenário: Classificação nos limites das faixas
    Quando o índice <indice> for classificado
    Então a classificação do índice deverá ser "<nivel>"

    Exemplos:
      | indice | nivel |
      | 0      | BAIXO |
      | 33     | BAIXO |
      | 34     | MEDIO |
      | 66     | MEDIO |
      | 67     | ALTO  |
      | 100    | ALTO  |
