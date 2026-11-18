# language: pt
Funcionalidade: Importação de clima real do INMET
  Como gestor agrícola
  Quero usar dados reais das estações do INMET nos meus talhões
  Para acompanhar o risco com temperatura, umidade do ar e chuva medidas de verdade

  Contexto:
    Dado um sistema SMField sem dados cadastrados
    E um talhão "Talhão A" de cultura "Milho" na propriedade "Fazenda Modelo"

  Cenário: Importar uma semana de dados reais da estação de Belo Horizonte
    Quando importar os dados da estação "A521" de "01/10/2026" a "07/10/2026" para o talhão
    Então o talhão deverá ter 7 registros de indicadores vindos do INMET
    E a temperatura registrada em "01/10/2026" deverá ser 27,54
    E a umidade do solo dos registros deverá estar identificada como simulada
    E todos os registros importados deverão ter risco calculado

  Cenário: Importar o mesmo período de novo não duplica registros
    Dado que os dados da estação "A521" de "01/10/2026" a "07/10/2026" já foram importados para o talhão
    Quando importar os dados da estação "A521" de "01/10/2026" a "07/10/2026" para o talhão
    Então nenhum novo registro deverá ser criado e 7 dias deverão ser informados como já importados
