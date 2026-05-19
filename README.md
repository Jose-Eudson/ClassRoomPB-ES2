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
