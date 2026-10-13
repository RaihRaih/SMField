# SMField — Visão do Produto
## Participantes: Raissa, Tayná, João, Arthur Henrique, Brenner.
## 1. Identificação do projeto

**Nome:** SMField — Monitoramento Inteligente e Análise de Riscos Agrícolas

**Categoria:** Sistema de monitoramento agrícola e apoio à tomada de decisão.

**Público-alvo:** Produtores rurais, gestores agrícolas e profissionais responsáveis pelo acompanhamento de lavouras.

## 2. Problema identificado

O acompanhamento das condições de uma lavoura envolve a análise de diferentes indicadores ambientais e ocorrências de campo, como umidade do solo, temperatura, umidade do ar, precipitação, pragas e doenças.

Quando essas informações estão dispersas ou são analisadas manualmente, torna-se mais difícil identificar quais áreas necessitam de atenção prioritária. Isso pode atrasar a identificação de condições desfavoráveis e dificultar a tomada de decisões.

O SMField busca solucionar esse problema centralizando as informações dos talhões e transformando os dados registrados em indicadores de risco compreensíveis.

## 3. Visão da solução

O SMField será uma plataforma que permitirá cadastrar propriedades rurais, organizar suas áreas em talhões, registrar indicadores ambientais e ocorrências de campo e calcular um índice de risco para cada talhão.

O sistema apresentará os resultados por meio de classificações de risco, um painel consolidado da propriedade, explicações sobre os fatores que influenciaram os resultados, alertas e histórico de acompanhamento.

Na primeira versão, os dados poderão ser registrados manualmente ou simulados, sem depender de sensores físicos ou serviços externos para demonstrar as funcionalidades principais.

## 4. Objetivo geral

Desenvolver um sistema capaz de centralizar dados agrícolas e apoiar a identificação e a priorização de talhões que necessitam de maior atenção, por meio de uma análise de risco automatizada, organizada e explicável.

## 5. Objetivos específicos

* Permitir o cadastro e o gerenciamento de propriedades rurais e talhões.
* Registrar indicadores ambientais e ocorrências identificadas no campo.
* Calcular um índice de risco entre 0 e 100, conforme regras de negócio documentadas.
* Classificar os talhões em níveis de risco claramente definidos.
* Apresentar os fatores que contribuíram para cada classificação.
* Disponibilizar alertas e histórico para acompanhar alterações nas condições dos talhões.
* Facilitar a comparação dos níveis de risco entre diferentes áreas da propriedade.

## 6. Público-alvo

O sistema será direcionado principalmente a:

* Produtores rurais que precisam acompanhar as condições de suas lavouras.
* Gestores agrícolas responsáveis por diferentes áreas de produção.
* Profissionais que auxiliam no monitoramento e na avaliação das condições de campo.

## 7. Escopo da primeira versão (MVP)

A primeira versão do SMField contemplará:

1. Cadastro, edição, consulta e exclusão de talhões.
2. Cadastro e consulta de propriedades rurais.
3. Registro de indicadores ambientais.
4. Cálculo e classificação do índice de risco.
5. Consulta individual dos indicadores e riscos dos talhões.
6. Painel consolidado para comparação e priorização de áreas.
7. Registro de ocorrências de campo.
8. Explicação dos fatores que influenciaram o risco.
9. Histórico e alertas de alterações relevantes.

A implementação será incremental, priorizando primeiro o cadastro, o registro dos indicadores e o cálculo do risco.

## 8. Fora do escopo inicial

Não serão obrigatórios na primeira versão:

* Integração com sensores IoT físicos.
* Conexão com estações meteorológicas reais.
* Processamento de imagens de satélite ou drones.
* Modelos de inteligência artificial para previsão de risco.
* Envio de notificações por WhatsApp ou e-mail.

Esses recursos poderão ser considerados em versões futuras.

## 9. Benefícios esperados

* Centralização das informações agrícolas.
* Maior agilidade na identificação de condições que merecem atenção.
* Priorização dos talhões de acordo com o nível de risco.
* Melhor compreensão dos fatores que influenciam os resultados.
* Acompanhamento da evolução das condições agrícolas ao longo do tempo.
* Apoio à tomada de decisão orientada por dados.

## 10. Critérios de sucesso

O projeto será considerado bem-sucedido quando:

* Os usuários conseguirem cadastrar e consultar propriedades e talhões.
* O sistema registrar indicadores válidos e rejeitar entradas inadequadas.
* O cálculo de risco produzir resultados consistentes com as regras definidas.
* A classificação apresentar os fatores que contribuíram para o resultado.
* Os usuários conseguirem identificar os talhões prioritários no painel.
* As funcionalidades principais possuírem testes e documentação.
* O sistema puder ser executado e demonstrado no ambiente definido pela equipe.

## 11. Tecnologias previstas

* **Linguagem:** Java.
* **Ambiente de desenvolvimento:** NetBeans.
* **Build e dependências:** Maven.
* **Interface inicial:** Java Swing, caso confirmada pela equipe.
* **Testes unitários:** JUnit 5.
* **Testes BDD:** Cucumber.
* **Versionamento e gestão:** Git e GitHub.

## 12. Evolução futura

O SMField poderá evoluir para integrar fontes meteorológicas, sensores IoT, imagens de satélite, drones e modelos preditivos, ampliando sua capacidade de monitoramento e análise.

Essas evoluções dependerão da viabilidade técnica, da disponibilidade de dados e das prioridades definidas para versões posteriores.
