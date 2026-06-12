# ClassRoomPB — Sistema de Controle Acadêmico

> Projeto desenvolvido para a disciplina **Engenharia de Software 2**, aplicando o método **Scrum** na prática.

**Equipe:**
- Eric Natan
- Jose Eudson
- Rui Fernando

---

## 📋 Sobre o Sistema

O **ClassRoomPB** é um sistema de controle acadêmico simplificado, executado via terminal (CLI). Ele gerencia usuários com quatro perfis distintos, cada um com uma visão diferente do sistema:

| Perfil | Prefixo de Matrícula | Funcionalidades |
|---|---|---|
| **Aluno** | `A0001` | Consultar disciplinas, solicitar matrícula, ver frequência e notas |
| **Professor** | `P0001` | Visualizar turmas, registrar frequência, lançar notas |
| **Coordenador** | `C0001` | Cadastrar disciplinas, ofertar turmas, aprovar/cancelar matrículas |
| **Administrador** | `AD0001` | Gerenciar usuários (CRUD completo) e cadastrar cursos |

Os dados são persistidos localmente em arquivos JSON (`usuarios.json` e `cursos.json`), gerados automaticamente na primeira execução.

---

## 🗂️ Estrutura do Projeto

```
ClassRoomPB-ES2/
├── src/
│   ├── main/java/com/classroompb/
│   │   ├── model/        # Entidades: Usuario, Aluno, Professor, Coordenador, Administrador, Curso
│   │   ├── repository/   # Persistência de dados (leitura/escrita no JSON)
│   │   ├── service/      # Regras de negócio e validações
│   │   ├── ui/           # Interface com o usuário (menus, entrada de dados)
│   │   └── util/         # Utilitários: JsonUtil, MatriculaGenerator
│   └── test/java/com/classroompb/
│       ├── repository/   # Testes de integração (persistência real em disco)
│       ├── service/      # Testes unitários com Mockito
│       └── util/         # Testes do gerador de matrículas
├── target/               # Arquivos compilados e relatórios (gerado pelo Maven)
├── pom.xml               # Configurações e dependências do projeto
├── usuarios.json         # Banco de dados local (gerado automaticamente)
└── README.md
```

---

## ✅ Pré-requisitos

Antes de rodar o projeto, certifique-se de ter instalado:

- **Java 8** ou superior
  ```bash
  java -version
  ```
- **Maven 3.6** ou superior
  ```bash
  mvn -version
  ```

Se não tiver o Maven, você pode baixar em: https://maven.apache.org/download.cgi  
E o Java em: https://adoptium.net

---

## 🚀 Como Rodar o Sistema

### 1. Clone ou extraia o projeto

Se estiver usando Git:
```bash
git clone <url-do-repositorio>
cd ClassRoomPB-ES2
```

Ou apenas extraia o `.zip` e entre na pasta.

### 2. Compile e gere o JAR

```bash
mvn clean package -DskipTests
```

Isso gera o arquivo `target/classroompb-1.0-SNAPSHOT.jar` com todas as dependências empacotadas.

### 3. Execute o sistema

```bash
java -jar target/classroompb-1.0-SNAPSHOT.jar
```

O sistema abrirá no terminal com um menu interativo. Use as **setas do teclado** (ou `W`/`S`) para navegar e `ENTER` para confirmar.

> **Primeira execução:** O arquivo `usuarios.json` será criado automaticamente na pasta raiz do projeto quando o primeiro usuário for cadastrado.

---

## 🧪 Como Rodar os Testes

### Rodar todos os testes

```bash
mvn test
```

### Rodar os testes e ver o resumo no terminal

```bash
mvn test -Dsurefire.useFile=false
```

### Rodar apenas um arquivo de teste específico

```bash
mvn test -Dtest=UsuarioServiceTest
mvn test -Dtest=LoginServiceTest
mvn test -Dtest=UsuarioRepositoryTest
mvn test -Dtest=LoginPersistenceTest
mvn test -Dtest=MatriculaGeneratorTest
```

---

## 📊 Relatório de Cobertura (JaCoCo)

O projeto usa o **JaCoCo** para medir a cobertura de testes. Após rodar `mvn test`, o relatório é gerado automaticamente em HTML.

### Gerar o relatório

```bash
mvn test
```

### Abrir o relatório

Abra no navegador o arquivo:
```
target/site/jacoco/index.html
```

O relatório mostra a cobertura por pacote, classe e método, incluindo quais linhas foram ou não executadas pelos testes.

---

## 🏗️ Arquitetura em Camadas

O projeto segue a arquitetura em camadas padrão:

**Model** → representa os dados (entidades do sistema)  
**Repository** → persiste e recupera os dados do `usuarios.json`  
**Service** → aplica as regras de negócio e validações  
**UI** → gerencia os menus e a interação com o usuário no terminal  
**Util** → ferramentas de suporte (serialização JSON, geração de matrículas)

---

## 🎨 Guia de Estilo e Qualidade de Código

O projeto utiliza uma cadeia completa de ferramentas de qualidade integradas ao Maven. Elas rodam automaticamente no build — você não precisa configurar nada manualmente.

### Ferramentas utilizadas

| Ferramenta | Papel | Executa em |
|---|---|---|
| **EditorConfig** | Formatação básica no editor (charset, indentação, EOL) | Ao salvar (IDE) |
| **formatter-maven-plugin** | Auto-formatação Java uniforme | `mvn compile` |
| **Checkstyle** | Convenções de nomenclatura e imports | `mvn validate` |
| **PMD + CPD** | Boas práticas e detecção de código duplicado | `mvn verify` |
| **SpotBugs** | Bugs potenciais no bytecode | `mvn verify` |
| **JaCoCo** | Cobertura de testes (mínimo **80%**) | `mvn test` |

### Convenções de nomenclatura (Checkstyle)

| Elemento | Convenção | Exemplo |
|---|---|---|
| Classes e interfaces | `PascalCase` | `UsuarioService` |
| Métodos | `camelCase` | `buscarPorEmail()` |
| Variáveis e parâmetros | `camelCase` | `nomeAluno` |
| Constantes | `UPPER_SNAKE_CASE` | `MAX_VAGAS` |
| Pacotes | `minúsculas` | `com.classroompb.service` |

Regras adicionais: sem `import *` no código de produção, chaves obrigatórias em `if/else/for/while`, proibido `==` para comparar Strings, complexidade ciclomática máxima de 25 por método.

**Exceção para testes:** arquivos `*Test.java` permitem `import static.*` de Assertions e nomes de método com `_` (padrão BDD: `deveFazerX_quandoY`).

### Comandos úteis de qualidade

```bash
# Rodar tudo (formatter + checkstyle + testes + jacoco)
mvn test

# Rodar tudo incluindo PMD e SpotBugs (recomendado antes de commit)
mvn verify

# Formatar código manualmente
mvn formatter:format

# Ver relatório de cobertura (abrir no navegador após mvn test)
# target/site/jacoco/index.html

# Ver relatório de bugs do SpotBugs com interface visual
mvn spotbugs:spotbugs spotbugs:gui

# Ver relatórios PMD/CPD
mvn pmd:pmd pmd:cpd
# target/site/pmd.html e target/site/cpd.html
```

### Arquivos de configuração de estilo

| Arquivo | Descrição |
|---|---|
| `.editorconfig` | Configurações básicas de formatação para os editores |
| `checkstyle.xml` | Regras customizadas do Checkstyle (nomenclatura, imports, complexidade) |
| `style/spotbugs-exclude.xml` | Exclusões de falsos positivos do SpotBugs para classes de UI |

> Para mais detalhes sobre cada ferramenta, consulte o documento **`guia-de-estilo-classroompb.pdf`** na pasta `releases/`.

---

## 🔧 Dependências Principais

| Dependência | Versão | Uso |
|---|---|---|
| Jackson Databind | 2.15.2 | Serialização/deserialização JSON |
| Jackson JSR310 | 2.15.2 | Suporte a tipos de data Java 8 |
| JUnit Jupiter | 5.10.0 | Framework de testes |
| Mockito | 4.11.0 | Mocks para testes unitários (compatível com Java 8+) |
| Byte Buddy | 1.17.8 | Suporte de instrumentação para JDKs mais novos (ex.: 22/23) |
| JaCoCo | 0.8.10 | Cobertura de testes |
| Maven Shade Plugin | 3.2.4 | Empacotamento do JAR com dependências |