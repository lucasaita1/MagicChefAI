# Decisões do projeto

## Por que essa stack

**Backend: Java 21 + Spring Boot 4.1.** Eu já tinha começado o MagicChefAI em Spring quando ainda estava aprendendo, então fazia sentido evoluir o mesmo projeto em vez de recomeçar. Aproveitei para trazer o ecossistema para as versões atuais: Spring Boot 4.1, Spring Security 7 e Java 21 (com virtual threads ligadas, já que a chamada à IA é I/O pesado e demorado).

**IA: Spring AI 2.0 + OpenAI (gpt-4o-mini).** Antes eu montava o JSON da OpenAI na mão com `WebClient` e lia a chave com uma biblioteca de dotenv dentro do construtor do service. O Spring AI me dá o `ChatClient`, separa mensagens de sistema e de usuário e, principalmente, faz **saída estruturada**: a IA devolve um JSON que vira o record `GeneratedRecipe`. Isso deixou a tela previsível e facilitou validar a resposta. O `gpt-4o-mini` foi escolhido pelo custo baixo e por ser rápido o suficiente para um chat.

**Banco: Postgres 18 + Flyway.** O projeto antigo usava `ddl-auto=update` junto com Flyway, ou seja, duas ferramentas brigando pelo schema. Agora o Flyway é o único dono (migrations V1 a V6, uma por mudança) e o Hibernate só valida (`ddl-auto=validate`). Mantive a V1 original intacta, porque migration já aplicada não se edita, e criei uma nova para substituir a tabela antiga.

**Segurança: Spring Security + JWT (Nimbus) com sessão revogável.** JWT puro não tem logout de verdade, porque o token continua válido até expirar. Por isso cada token tem um `jti` ligado a uma linha em `user_sessions`, e o logout revoga essa linha. Para o cenário de token roubado, usei o padrão da OWASP de *fingerprint*: o JWT só guarda o hash de um valor que fica num cookie HttpOnly, que o JavaScript não consegue ler.

**Frontend: Vue 3 + Vite + Pinia.** O Vue tem curva de aprendizado curta, componentes de arquivo único fáceis de explicar e o Vite deixa o ciclo de desenvolvimento instantâneo. Não usei biblioteca de UI: o chef robô é um SVG animado só com CSS, o que deu personalidade ao projeto sem pesar no bundle.

**Testes: JUnit 5 + Mockito + MockMvc + H2 / Vitest.** Os testes de integração sobem a aplicação inteira com H2 em modo PostgreSQL, rodando as mesmas migrations do Flyway. A IA é sempre mockada, então os testes são rápidos, gratuitos e determinísticos.

**Deploy: Railway, com cada peça na sua máquina.** O Postgres 18, a API e o front rodam como três serviços separados. Só o front (um nginx que serve o Vue e faz proxy de `/api`) tem domínio público; a API e o banco ficam acessíveis apenas pela rede privada. Para o navegador tudo é a mesma origem, o que permite usar cookie `SameSite=Strict`, e ainda dá ao gateway o papel de informar o IP real do cliente para o rate limit.

**E-mail: Resend.** Para impedir que contas falsas consumam a cota da IA, o cadastro envia um link de confirmação pelo Resend (API HTTP simples, com plano gratuito). O envio também fica atrás de uma interface (`EmailSender`), mockada nos testes.

## Maior dificuldade

A parte mais difícil foi a **proteção contra prompt injection**. Não existe uma regex mágica: alguém sempre pode escrever o ataque de outro jeito. A solução foi pensar em camadas. Primeiro saneio e bloqueio o óbvio antes de chamar a IA (o que também economiza dinheiro), depois isolo o texto do usuário com delimitadores que ele não consegue fechar, deixo a IA recusar com `offTopic` e, por último, valido a saída, incluindo um "canário" secreto no prompt de sistema que, se aparecer na resposta, faz o sistema descartá-la.

Também tive dificuldades práticas: o Spring AI 2.0 chama `getOptions()` no modelo e o meu mock devolvia `null`, quebrando o teste; um Postgres instalado via Homebrew na minha máquina ocupava a porta 5432 e "roubava" as conexões do container (resolvi expondo o Docker na 5433); e descobri durante os testes reais que a conta da OpenAI estava sem créditos. Na hora de preparar o deploy, os testes de ponta a ponta pelo gateway pegaram um bug que só apareceria em produção: o navegador manda o cabeçalho `Origin` com o domínio público, mas a API recebe a requisição pela rede privada, então o CORS do Spring a rejeitava. A solução foi declarar explicitamente o domínio do gateway como origem permitida. O código já tratava esse erro e mostrava uma mensagem amigável, sem vazar detalhes.

## O que aprendi

- Segurança é processo, não feature: autenticação, autorização por dono do recurso, rate limit, sanitização e validação de saída se complementam
- Testar com a IA mockada exige uma **porta** (`RecipeAiClient`) entre o domínio e o provedor, o que também deixa trocar de modelo bem simples
- Migrations versionadas tornam o banco reproduzível em qualquer ambiente
- CI com gate de cobertura impede que a qualidade caia sem ninguém perceber

## O que faria diferente

- Usaria **Testcontainers** com Postgres real nos testes de integração, em vez de H2
- Trocaria o rate limiter em memória por **Redis/Bucket4j**, para funcionar com várias instâncias
- Adicionaria **refresh token** com rotação, para sessões longas sem aumentar a validade do JWT
- Usaria um segundo modelo pequeno como **classificador de intenção** antes da geração, reforçando a camada de detecção
- Faria streaming da resposta da IA no chat, para a receita aparecer sendo "escrita"
