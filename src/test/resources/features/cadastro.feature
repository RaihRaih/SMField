# language: pt
Funcionalidade: Cadastro de propriedades e talhões
  Como gestor agrícola
  Quero cadastrar propriedades e seus talhões
  Para organizar as áreas que serão monitoradas

  Contexto:
    Dado um sistema SMField sem dados cadastrados

  Cenário: Cadastro de propriedade válida
    Dado que o usuário informa o nome "Fazenda Modelo" e o município "Contagem"
    Quando cadastrar a propriedade
    Então a propriedade "Fazenda Modelo" deverá ser armazenada
    E a propriedade "Fazenda Modelo" deverá aparecer na listagem

  Cenário: Cadastro de talhão vinculado a uma propriedade existente
    Dado uma propriedade existente chamada "Fazenda Modelo" no município "Contagem"
    Quando cadastrar o talhão "Talhão A" com área 12 hectares e cultura "Milho"
    Então o talhão "Talhão A" deverá estar vinculado à propriedade "Fazenda Modelo"

  Cenário: Talhão sem propriedade cadastrada é rejeitado
    Quando tentar cadastrar o talhão "Talhão X" em uma propriedade inexistente
    Então o cadastro deverá ser rejeitado com a mensagem contendo "propriedade"
    E nenhum talhão deverá estar cadastrado
