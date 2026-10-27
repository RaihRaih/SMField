# SMField
Projeto de Gestão e Qualidade de Software - Squad Ânima HUB | Alunos: Arthur, Brener, João, Raissa e Tayná | Professora: Rafaela


## Estrutura do repositório

```
SMField/
├── docs/
│   ├── requisitos/          # Sprint 1: visão do produto e requisitos (funcionais e não funcionais)
│   ├── arquitetura/         # Sprint 1: arquitetura inicial
│   └── sprint-1-cli-java.md # Sprint 1: documentação do MVP em Java (CLI)
├── src/main/java/smfield/
│   ├── domain/Propriedade.java   # Sprint 1: entidade Propriedade
│   └── ...                       # Sprint 1: CLI, recomendador, calendário, leitor de dados
├── src/main/resources/dados/     # Sprint 1: culturas.json e regioes.json
├── src/test/java/smfield/        # Sprint 1: testes JUnit 5
└── pom.xml                       # Maven (Java 17)
```

## Como compilar e executar (Sprint 1)

```bash
mvn clean test
mvn clean package
java -jar target/smfield-0.1.0.jar listar
```

Veja [docs/sprint-1-cli-java.md](docs/sprint-1-cli-java.md) para os comandos e detalhes.
