# SMField — Sistema de Monitoramento Agrícola (Sprint 1 · MVP) — versão Java

Ajuda o agricultor a descobrir **quando plantar e colher** cada cultura na sua região.
Esta é a versão em **Java** do MVP originalmente feito em Python, com as mesmas funcionalidades,
as mesmas regras de cálculo, os mesmos dados e os mesmos testes.

## Funcionalidades do MVP
| # | Funcionalidade | Comando |
|---|----------------|---------|
| 1 | **Recomendação de plantio**: nota (0-100) para cada mês, com colheita estimada | `recomendar` |
| 2 | **O que plantar agora**: ranking de culturas para um mês na região | `agora` |
| 3 | **Calendário de colheita**: data de colheita a partir da data de plantio | `colheita` |
| + | Lista as culturas e regiões disponíveis | `listar` |

## Requisitos
- **JDK 17 ou superior** (`java -version`)
- **Maven 3.8+** (`mvn -version`) — só para compilar e rodar os testes pelo Maven
- Nenhuma biblioteca externa em tempo de execução (o único artefato baixado é o JUnit 5, usado nos testes)

## Compilar, testar e empacotar
```bash
mvn clean test       # compila e roda os testes (JUnit 5)
mvn clean package    # gera target/smfield-0.1.0.jar
```

## Como usar
```bash
java -jar target/smfield-0.1.0.jar listar
java -jar target/smfield-0.1.0.jar recomendar --cultura soja --regiao "Passo Fundo" --ano 2026
java -jar target/smfield-0.1.0.jar agora --regiao "Belo Horizonte" --mes 10
java -jar target/smfield-0.1.0.jar colheita --cultura milho --plantio 2026-10-20
```
Também funciona `--opcao=valor` (ex.: `--mes=10`). Nomes ignoram acento e maiúsculas/minúsculas
(`feijao` = `Feijão`). Sem `--ano`, usa o ano atual; sem `--mes`, usa o mês atual.

> **Windows:** para exibir acentos e os ícones 🟢🟡🔴 no terminal, rode antes `chcp 65001`
> (ou use o Windows Terminal). A saída do programa já é gerada em UTF-8.

Códigos de saída: `0` sucesso · `1` erro de dados (cultura/região inexistente, data ou mês inválido) · `2` erro de uso.

## Estrutura do projeto
```
smfield-java/
├── pom.xml
├── src/main/java/smfield/
│   ├── Main.java          # CLI (cli.py)
│   ├── Recomendador.java  # motor de notas (recomendador.py)
│   ├── Calendario.java    # colheita e agrupamento de meses (calendario.py)
│   ├── DadosLoader.java   # leitura/validação dos JSON e busca (dados_loader.py)
│   ├── Json.java          # leitor JSON mínimo, sem dependências
│   └── Cultura / ClimaMes / Regiao / Janela / Sugestao  # modelos (models.py), como records
├── src/main/resources/dados/   # culturas.json e regioes.json (iguais aos do Python)
└── src/test/java/smfield/      # testes JUnit 5
```

## Testes
`mvn test` executa 23 testes: os 13 testes originais do Python portados para JUnit 5
(dados, calendário e recomendador) mais 10 extras (leitor JSON, validação dos dados e CLI).

## Como funciona a nota
Para cada mês de plantio, o ciclo da cultura é percorrido mês a mês e avalia-se:
- **Temperatura** (peso 0,4): 1 dentro da faixa ideal, cai linearmente até 0 nos limites absolutos.
- **Chuva** (peso 0,4): total do ciclo vs. necessidade da cultura, descontando seca na fase inicial.
- **Geada** (peso 0,2): só penaliza culturas sensíveis.

Combinação por **média geométrica ponderada** (lei do mínimo): um fator crítico muito ruim derruba a nota.
Classificação: ≥75 ideal · 50–74 aceitável · <50 desfavorável.

## ⚠️ Limitações conhecidas
- Os dados em `src/main/resources/dados/*.json` são **valores ilustrativos aproximados** (3 regiões, 6 culturas).
  Antes de uso real, substituir por normais climatológicas do **INMET** e parâmetros do **ZARC/Embrapa**.
- Sem irrigação, tipo de solo, cultivar ou altitude no cálculo.

## Backlog (próximas sprints)
- S2: importar séries do INMET/NASA POWER; mais culturas e regiões; localização por município/coordenada.
- S3: mapa/zoneamento com QGIS (exportar GeoJSON/GPKG) e API (Spring Boot) + interface web.
- S4: alertas (geada/chuva), histórico da safra do usuário, modo irrigado.
