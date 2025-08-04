![MagicChefAI Logo](https://private-us-east-1.manuscdn.com/sessionFile/UT5WhW6EtX7BSNYOCLsZe5/sandbox/0vis3tKf7mLfKGOZdRN2xX-images_1754274335189_na1fn_L2hvbWUvdWJ1bnR1L01hZ2ljQ2hlZkFJL2RvY3MvbWFnaWNjaGVmX2xvZ29fcmVmaW5lZA.png?Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cHM6Ly9wcml2YXRlLXVzLWVhc3QtMS5tYW51c2Nkbi5jb20vc2Vzc2lvbkZpbGUvVVQ1V2hXNkV0WDdCU05ZT0NMc1plNS9zYW5kYm94LzB2aXMzdEtmN21MZktHT1pkUk4yeFgtaW1hZ2VzXzE3NTQyNzQzMzUxODlfbmExZm5fTDJodmJXVXZkV0oxYm5SMUwwMWhaMmxqUTJobFprRkpMMlJ2WTNNdmJXRm5hV05qYUdWbVgyeHZaMjlmY21WbWFXNWxaQS5wbmciLCJDb25kaXRpb24iOnsiRGF0ZUxlc3NUaGFuIjp7IkFXUzpFcG9jaFRpbWUiOjE3OTg3NjE2MDB9fX1dfQ__&Key-Pair-Id=K2HSFNDJXOU9YS&Signature=bZnl-U0Ok2lXZ0LvTDBfnuXpG2CeF1qYvdng-yo1SOWBXSE41r3sDWru5O8xYefQJ99yT1egdapzbNALi11hwLmcPX24osaCQZO1eO6WdcWu2juJjQWh3UIE8LMWCYGYTxk8OGZlXslVVi~mEFp-ia~SqC5QL6pnIHtdxCnJ0NOCmrzvkxr1meTFYmqluZjPOmTNxPgYKh75mA-TMlZ98R9G4cER5d0GsR1YfxAvF4naLzfSkDwwYMrtSizVSIeohDh1yjMneuMVXTHUlATY~8NaarvKcS9zMHAJqfwdPVBrB-t5ZpfAtbkG18YiHl3NTg7WD9QNDUyzae0-7OSMGw__)

# 🧙‍♂️ MagicChefAI

Um chef de cozinha IA que recebe seus ingredientes disponíveis ou favoritos, monta uma receita e te ensina a fazer!

## Descrição do projeto

MagicChefAI é uma aplicação moderna que combina **Spring Boot + WebFlux + PostgreSQL** com o poder da **OpenAI (GPT-4o)** para gerar receitas inteligentes com base nos ingredientes disponíveis do usuário.

> "Você informa o que tem na geladeira, e a IA sugere uma receita criativa pra você!" 🍲🤖

## ⚙️ Tecnologias Utilizadas

*   **Spring Boot:** Framework para desenvolvimento de aplicações Java robustas e escaláveis.
*   **Java 17:** Linguagem de programação utilizada para o backend.
*   **Spring WebFlux:** Para construir aplicações reativas e não bloqueantes.
*   **Spring Data JPA:** Para persistência de dados com bancos de dados relacionais.
*   **PostgreSQL:** Banco de dados relacional para armazenamento de informações.
*   **Flyway:** Ferramenta de migração de banco de dados para gerenciar o esquema do banco de dados.
*   **Lombok:** Biblioteca para reduzir o código boilerplate em Java.
*   **Dotenv-java:** Para carregar variáveis de ambiente de um arquivo `.env`.
*   **Docker Compose:** Para orquestração de contêineres, facilitando a configuração do ambiente de desenvolvimento.
*   **OpenAI API (GPT-4o)** – integração para geração de receitas

## 📐 Estrutura do Projeto

```bash
src/
├── config/           # Configurações de WebClient
├── controller/       # Endpoints REST (CRUD + geração de receita)
├── enums/            # Enum para categorias dos alimentos
├── model/            # Entidade FoodItem
├── repository/       # Interface JPA/Reactive para o FoodItem
├── service/          # Lógica de negócio e integração com a OpenAI
```

---

## 💾 Banco de Dados

O projeto utiliza PostgreSQL com versão Dockerizada.

### 🔁 Flyway Migration

O Flyway é usado para criar a tabela `food_item` automaticamente. O script de inicialização está em:

```
src/main/resources/db/migration/V1__create_food_item_table.sql
```

---

## 📦 Como Rodar Localmente

Para configurar e rodar o MagicChefAI em sua máquina local, siga os passos abaixo:

### Pré-requisitos

Certifique-se de ter o seguinte software instalado:

*   **Java Development Kit (JDK) 17** ou superior
*   **Docker** e **Docker Compose**
*   **Maven**

### Configuração

1.  **Clone o repositório:**

    ```bash
    git clone https://github.com/lucasaita1/MagicChefAI.git
    cd MagicChefAI
    ```

2.  **Crie um arquivo `.env`:**

    Na raiz do projeto, crie um arquivo chamado `.env` e adicione as seguintes variáveis de ambiente:

    ```
    OPENAI_API_KEY=sua_chave_api_openai
    DB_URL=jdbc:postgresql://localhost:5432/magicchefdb
    DB_USERNAME=seu_username
    DB_PASSWORD=sua_senha 

    *Substitua `sua_chave_api_openai` pela sua chave de API da OpenAI.*

3.  **Inicie os serviços com Docker Compose:**

    ```bash
    docker-compose up -d
    ```

    Isso irá iniciar o banco de dados PostgreSQL e o serviço Flyway.

4.  **Execute a aplicação Spring Boot:**

    ```bash
    mvn spring-boot:run
    ```

    A aplicação estará disponível em `http://localhost:8080`.

## 🚀 Endpoints Disponíveis

| Método | Rota                   | Descrição                              |
|--------|------------------------|----------------------------------------|
| GET    | `/food/listar`         | Lista todos os ingredientes            |
| GET    | `/food/listar/{id}`    | Busca ingrediente por ID               |
| POST   | `/food/criar`          | Cria novo ingrediente                  |
| PUT    | `/food/atualizar/{id}` | Atualiza ingrediente                   |
| DELETE | `/food/{id}`           | Remove ingrediente                     |
| GET    | `/generate`            | Gera uma receita com os ingredientes   |

---

# 🧠 Integração com a OpenAI

A IA é chamada via WebClient com o modelo **gpt-4o**. Ela utiliza os ingredientes cadastrados no banco de dados para criar receitas personalizadas.

Exemplo de prompt gerado:

```text
Baseado nos itens cadastrados no banco de dados, faça uma receita gostosa. (Pode usar mais itens e dicas para compor a receita)
• Arroz (GRAO) - Quantidade: 2, Categoria: GRAO
• Tomate (FRUTA) - Quantidade: 3, Categoria: FRUTA
...
```

---

# 📌 Funcionalidades Futuras (Backlog)

- 🔐 Autenticação de usuários com Spring Security
- 📝 Cadastro de receitas favoritas
- ⏰ Verificador de validade automática
- ☁️ Deploy em cloud (Render, Railway ou AWS)
- ✅ Testes unitários e de integração
- 📱 Interface web responsiva com React ou Thymeleaf

---

## 👨‍💻 Autor

**Lucas Prates Aita**

- 📧 lucasaita4000@gmail.com
- 💼 [LinkedIn](https://www.linkedin.com/in/lucas-aita)
- 💻 [GitHub](https://github.com/lucasaita1)
- 📸 [Instagram](https://instagram.com/lucasp.aita)

---

## 📝 Licença

Este projeto está sob a licença **MIT** – sinta-se livre para usar, estudar e contribuir!

----