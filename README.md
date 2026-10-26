# 🚗 AutoRent - Sistema de Locação de Veículos

Sistema corporativo (Jakarta EE) para gestão de uma locadora de veículos: cadastro de clientes, agências e frota, reservas, retirada e devolução de veículos, com painel administrativo.

O sistema é dividido em **quatro projetos Eclipse** que são entregues juntos em um **EAR**:

- 🧩 **Backend EJB** com regras de negócio e persistência (JPA/Hibernate + PostgreSQL)
- 🌐 **Aplicação web (JSF)** para clientes e administradores
- 🖥️ **Client Java SE (Swing)** para funcionários da locadora, que acessa os EJBs remotamente
- 📦 **Client JAR** com as entidades, DTOs e interfaces remotas, compartilhado entre os módulos

<br>

## 🌐 Tecnologias

| Item | Versão / Detalhe |
|---|---|
| **Java (EJB, Client e Web)** | 17 (conforme facets do Eclipse) |
| **Java (Desktop Swing)** | Projeto configurado com compliance **25** no Eclipse (ajuste em *Properties → Java Compiler* se usar outro JDK) |
| **Jakarta EE** | 10 (EJB 4.0, JPA/Persistence 3.0, Faces 4.0, CDI, Servlet 6.x) |
| **Servidor de aplicação recomendado** | **WildFly 27** (runtime usado no desenvolvimento) |
| **Persistência** | Hibernate (provider JPA do WildFly) + `PostgreSQLDialect` |
| **Banco de dados** | PostgreSQL |
| **Web** | JSF (Facelets/XHTML), CSS próprio |
| **Client remoto** | Swing + JNDI via `remote+http` (`jboss-client.jar`) |
| **Senhas** | PBKDF2WithHmacSHA256, 120.000 iterações, sal aleatório de 16 bytes |

<br>

## 📁 Estrutura dos projetos

```
AutoRent/            → Módulo EJB (session beans, persistence.xml, ejb-jar.xml)
AutoRentClient/      → Utility project: entidades, enums, DTOs e interfaces @Remote (gera o AutoRentClient.jar declarado em <ejb-client-jar>)
AutoRentWeb/         → WAR: managed beans CDI, páginas XHTML, filtro de acesso
AutoRentDesktop/     → Aplicação Java SE (Swing) usada pelos funcionários
```

<br>

## 📦 Pré-requisitos

- JDK 17+ (JDK 25 para compilar o Desktop conforme a configuração atual do projeto)
- WildFly 27 (ou outro servidor compatível com Jakarta EE 10)
- PostgreSQL 12+
- Eclipse IDE for Enterprise Java (com WTP/JBoss Tools) ou outra forma de montar o EAR

<br>

## ▶️ Como rodar localmente

### 1. Banco de dados (PostgreSQL)

O `persistence.xml` usa conexão JDBC direta (`RESOURCE_LOCAL`) com estes valores fixos:

| Propriedade | Valor |
|---|---|
| URL | `jdbc:postgresql://localhost:5432/auto-rent` |
| Usuário | `postgres` |
| Senha | `postgre` |

Crie apenas o banco vazio:

```sql
-- psql -U postgres
CREATE DATABASE "auto-rent";
```

> 💡 **Não há scripts de criação de tabelas.** O Hibernate está com `hibernate.hbm2ddl.auto=update`, então as tabelas e sequences (`usuario`, `agencia`, `veiculo`, `acessorio`, `locacao` e a tabela de junção veículo–acessório) são criadas automaticamente no primeiro deploy.
> Se a sua senha do `postgres` for outra, altere `AutoRent/ejbModule/META-INF/persistence.xml`.

### 2. Driver JDBC do PostgreSQL

Como o `EntityManagerFactory` é criado manualmente (`Persistence.createEntityManagerFactory("auto-rent")`), o driver `org.postgresql.Driver` precisa estar visível para a aplicação. Use **uma** das opções:

- Colocar o `postgresql-<versão>.jar` na pasta `lib` do EAR (*Deployment Assembly*), **ou**
- Instalar o driver como módulo no WildFly.

### 3. Montar o EAR

O JNDI usado pelo client Desktop espera estes nomes, então **respeite-os**:

| Item | Nome |
|---|---|
| Enterprise Application Project (EAR) | `AutoRent-EAR` |
| Módulo EJB | `AutoRent` |
| Módulo Web | `AutoRentWeb` (context-root `AutoRentWeb`) |

No Eclipse:

1. *File → New → Enterprise Application Project* com o nome `AutoRent-EAR` e o runtime **WildFly 27**.
2. Em **Java EE Module Dependencies / Deployment Assembly** do EAR, adicione os módulos `AutoRent` (EJB) e `AutoRentWeb` (WAR). O `AutoRentClient.jar` deve ficar no EAR (o `MANIFEST.MF` do EJB já referencia `Class-Path: AutoRentClient.jar`).
3. Adicione o driver do PostgreSQL ao EAR, se escolheu essa opção no passo anterior.

### 4. Deploy do EAR no WildFly

**Pelo Eclipse:** adicione o `AutoRent-EAR` ao servidor WildFly 27 (*Add and Remove...*) e inicie o servidor.

### 5. Acessar a aplicação web

```
http://localhost:8080/AutoRentWeb/
```

| Página | Quem acessa |
|---|---|
| `/index.xhtml`, `/login.xhtml`, `/cadastro.xhtml` | Público |
| `/home.xhtml`, `/veiculos.xhtml`, `/veiculo.xhtml` | Usuário logado |
| `/dashboard.xhtml`, `/reserva.xhtml`, `/minhasReservas.xhtml` | Cliente |
| `/admin/dashboard.xhtml` | Somente funcionário/administrador |

O acesso é controlado pelo `AcessoFilter` (redireciona para o login se não houver sessão e para `/home.xhtml` se um cliente tentar abrir `/admin/*`).

<br>

## 🔐 Credenciais padrão

O administrador é criado automaticamente na inicialização (`AdminPadraoSB`) caso ainda não exista:

| Campo | Valor |
|---|---|
| **Login (CPF)** | `00000000000` |
| **Senha** | `admin` |
| **E-mail** | `admin@autorent.com` |
| **Perfil** | `FUNCIONARIO` |

> ⚠️ O login do sistema é feito por **CPF** (pontuação é ignorada), não por nome de usuário.
> ⚠️ Troque a senha padrão fora do ambiente de testes.

**Cliente de teste:** não há cliente pré-cadastrado. Crie um em `http://localhost:8080/AutoRentWeb/cadastro.xhtml` (CPF com 11 dígitos, e-mail válido e senha com no mínimo 6 caracteres) ou pelo menu *Clientes* do Desktop.

**Dados iniciais:** além do administrador, o banco começa vazio. Antes de testar reservas, cadastre ao menos **uma agência** e **um veículo** pelo client Desktop (menus *Agências* e *Frota*).

<br>

## 🖥️ Como rodar o client Java SE (Desktop)

O Desktop é usado **apenas por funcionários** (clientes recebem "Acesso restrito a funcionários da locadora"). Ele se conecta ao servidor em `localhost:8080` via `remote+http` e localiza os EJBs com nomes do tipo:

```
ejb:AutoRent-EAR/AutoRent//VeiculoSB!br.autorent.sessionbeans.IVeiculo
ejb:AutoRent-EAR/AutoRent//UsuarioSB!br.autorent.sessionbeans.IUsuario?stateful
```

Host, porta, nome do EAR e do módulo estão em `AutoRentDesktop/src/br/autorent/desktop/ConexaoEJB.java`.

**Pré-requisito:** o EAR precisa estar implantado e o WildFly em execução.

**Pelo Eclipse:** importe `AutoRentDesktop`, confirme que `jar/AutoRentClient.jar` e `jar/jboss-client.jar` estão no *Build Path* e execute *Run As → Java Application* na classe `br.autorent.desktop.AutoRentDesktop`.

Na tela de login informe `00000000000` / `admin`. Módulos disponíveis: **Clientes**, **Agências**, **Frota** (consulta, situação e cadastro de veículos) e **Locações** (consulta, nova locação, retirada e devolução).

> Se o `AutoRentClient.jar` for alterado, gere-o novamente (exportação do projeto `AutoRentClient`) e copie para `AutoRentDesktop/jar/` e `AutoRentWeb/jar/`.

<br>

## 📊 Regras de negócio

1. **Perfis:** `CLIENTE` e `FUNCIONARIO`. O administrador padrão é funcionário.
2. **Cadastro de cliente:** nome até 120 caracteres, CPF com 11 dígitos, e-mail válido, senha mínima de 6 caracteres; CPF e e-mail são únicos.
3. **Status da locação:** `RESERVADA` → `ATIVA` → `FINALIZADA`, ou `RESERVADA` → `CANCELADA`. Cada operação exige o status anterior correto.
4. **Status do veículo:** `DISPONIVEL`, `LOCADO`, `MANUTENCAO`. A retirada marca o veículo como `LOCADO`; a devolução o marca como `DISPONIVEL`.
5. **Devolução em outra agência:** ao devolver, o veículo passa a pertencer à agência de devolução.
6. **Período:** a devolução deve ser posterior à retirada e a retirada não pode estar no passado.
7. **Conflito de reservas:** não é permitido reservar um veículo que já tenha locação `RESERVADA` ou `ATIVA` sobreposta ao período.
8. **Valor total:** número de diárias × valor da diária do veículo.
9. **Atraso:** locação `ATIVA` com data prevista de devolução anterior a hoje é exibida como atrasada.
10. **Senhas:** armazenadas com PBKDF2 + sal. Senhas legadas em texto puro são convertidas para hash no primeiro login válido.

<br>

## 🔎 Consultas JPQL implementadas

Todas usam `EntityManager.createQuery(...)` com parâmetros nomeados. As colunas "Como testar" indicam a tela que dispara a consulta; para as demais, use o teste Java SE descrito na seção seguinte.

### Acessórios (`AcessorioSB`)

| # | Método | JPQL | Como testar |
|---|---|---|---|
| 1 | `buscarPorDescricao(descricao)` | `select a from Acessorio a where lower(a.descricao) like lower(:descricao) order by a.descricao` | Java SE |
| 2 | `buscarPorVeiculo(veiculoId)` | `select a from Acessorio a join a.veiculos v where v.id = :id order by a.descricao` | Web: detalhes do veículo (`/veiculos.xhtml` → *Ver detalhes*) |

### Agências (`AgenciaSB`)

| # | Método | JPQL | Como testar |
|---|---|---|---|
| 3 | `buscarPorCidade(cidade)` | `select a from Agencia a where lower(a.cidade) = lower(:cidade) order by a.nome` | Java SE |
| 4 | `buscarPorNome(nome)` | `select a from Agencia a where lower(a.nome) like lower(:nome) order by a.nome` | Java SE |
| 5 | `buscarVeiculosDisponiveis(categoria)` | `select distinct a from Agencia a join a.veiculos v where v.status = :status and v.categoria = :categoria order by a.nome` | Java SE |
| 6 | `pesquisarAgencias(texto)` (sem filtro) | `select a from Agencia a order by a.nome` | Desktop: *Agências* (campo de busca vazio) |
| 7 | `pesquisarAgencias(texto)` (com filtro) | `select a from Agencia a where lower(a.nome) like :termo or lower(a.cidade) like :termo order by a.nome` | Desktop: *Agências* (digitar nome ou cidade) |
| 8 | `existe(nome, cidade)` (privado) | `select count(a) from Agencia a where lower(a.nome) = lower(:nome) and lower(a.cidade) = lower(:cidade)` | Desktop: cadastrar agência repetida |

### Clientes (`ClienteSB`)

| # | Método | JPQL | Como testar |
|---|---|---|---|
| 9 | `buscarPorCpf(cpf)` | `select u from Usuario u where u.cpf = :cpf and u.tipo = :tipo` | Web: `/cadastro.xhtml` com CPF já cadastrado |
| 10 | `buscarPorEmail(email)` | `select u from Usuario u where lower(u.email) = lower(:email) and u.tipo = :tipo` | Web: `/cadastro.xhtml` com e-mail já cadastrado |
| 11 | `buscarPorNome(nome)` | `select u from Usuario u where lower(u.nome) like lower(:nome) and u.tipo = :tipo order by u.nome` | Java SE |
| 12 | `buscarTodos()` | `select u from Usuario u where u.tipo = :tipo order by u.nome` | Java SE |
| 13 | `pesquisarClientes(texto)` | `select u from Usuario u where u.tipo = :tipo and (lower(u.nome) like :termo or lower(u.email) like :termo [or u.cpf like :cpf]) order by u.nome` *(trecho do CPF só entra se o texto tiver dígitos)* | Desktop: *Clientes* e *Nova locação* (busca de cliente) |
| 14 | `existe(campo, valor)` (privado) | `select count(u) from Usuario u where u.cpf = :valor` ou `... where lower(u.email) = :valor` | Desktop/Web: cadastrar CPF ou e-mail duplicado |

### Frota e locações para o Desktop (`ConsultaFrotaSB`)

| # | Método | JPQL | Como testar |
|---|---|---|---|
| 15 | `listarAgencias()` | `select a from Agencia a order by a.nome` | Desktop: *Frota* (filtro de agência) |
| 16 | `consultarFrota(agenciaId, status)` | `select v from Veiculo v join fetch v.agencia a where 1 = 1 [and a.id = :agenciaId] [and v.status = :status] order by a.nome, v.marca, v.modelo` | Desktop: *Frota* e *Nova locação* |
| 17 | `consultarFrota(...)` (devoluções previstas) | `select l.veiculo.id, l.dataPrevDevolucao from Locacao l where l.status = :status` *(status `ATIVA`)* | Desktop: *Frota* (coluna de devolução prevista) |
| 18 | `consultarLocacoes(status, agenciaId)` | `select l from Locacao l join fetch l.cliente join fetch l.veiculo join fetch l.agenciaRetirada ar join fetch l.agenciaDevolucao ad where 1 = 1 [and l.status = :status] [and (ar.id = :agenciaId or ad.id = :agenciaId)] order by l.dataPrevDevolucao, l.id` | Desktop: *Locações*, *Retirada* e *Devolução* |

### Locações (`LocacaoSB`)

| # | Método | JPQL | Como testar |
|---|---|---|---|
| 19 | `buscarPorCliente(clienteId)` | `select l from Locacao l where l.cliente.id = :id order by l.dataRetirada desc` | Java SE |
| 20 | `buscarPorClienteCompleto(clienteId)` | `select l from Locacao l join fetch l.veiculo join fetch l.agenciaRetirada join fetch l.agenciaDevolucao where l.cliente.id = :clienteId order by l.dataRetirada desc` | Web (cliente): `/dashboard.xhtml` e `/minhasReservas.xhtml` |
| 21 | `buscarPorVeiculo(veiculoId)` | `select l from Locacao l where l.veiculo.id = :id order by l.dataRetirada desc` | Java SE |
| 22 | `buscarPorAgencia(agenciaId)` | `select l from Locacao l where l.agenciaRetirada.id = :id or l.agenciaDevolucao.id = :id order by l.dataRetirada desc` | Java SE |
| 23 | `buscarPorStatus(status)` | `select l from Locacao l where l.status = :status order by l.dataRetirada desc` | Java SE |
| 24 | `buscarAtivasPorCliente(clienteId)` | `select l from Locacao l join fetch l.cliente c join fetch l.veiculo join fetch l.agenciaRetirada join fetch l.agenciaDevolucao where c.id = :clienteId and l.status = :status order by l.dataRetirada desc` | Java SE |
| 25 | `buscarPorCategoriaEStatus(categoria, status)` | `select l from Locacao l join fetch l.veiculo v join fetch l.cliente join fetch l.agenciaRetirada join fetch l.agenciaDevolucao where v.categoria = :categoria and l.status = :status order by l.dataRetirada desc` | Web (admin): `/admin/dashboard.xhtml` (reservas e locações ativas) |
| 26 | Carregamento de uma locação por id (retirada/devolução com DTO) | `select l from Locacao l join fetch l.cliente join fetch l.veiculo join fetch l.agenciaRetirada join fetch l.agenciaDevolucao where l.id = :id` | Desktop: registrar retirada ou devolução |

### Veículos (`VeiculoSB`)

| # | Método | JPQL | Como testar |
|---|---|---|---|
| 27 | `buscarPorAgencia(agenciaId)` | `select v from Veiculo v where v.agencia.id = :valor order by v.modelo` | Java SE |
| 28 | `buscarPorStatus(status)` | `select v from Veiculo v where v.status = :valor order by v.modelo` | Java SE |
| 29 | `buscarPorCategoria(categoria)` | `select v from Veiculo v where v.categoria = :valor order by v.modelo` | Java SE |
| 30 | `buscarPorPlaca(placa)` | `select v from Veiculo v where upper(v.placa) = upper(:placa)` | Java SE |
| 31 | `buscarDisponiveis(agenciaId, categoria)` | `select v from Veiculo v where v.status = :status [and v.agencia.id = :agenciaId] [and v.categoria = :categoria] order by v.modelo` | Java SE |
| 32 | `buscarDisponiveisPorAgencia(agenciaId)` | `select v from Veiculo v join fetch v.agencia a where a.id = :agenciaId and v.status = :status order by v.modelo` | Web: `/veiculos.xhtml` e `/reserva.xhtml` (veículos da agência) |
| 33 | `buscarAlugados()` | `select v from Veiculo v join fetch v.agencia where v.status = :status order by v.modelo` | Web: `/admin/dashboard.xhtml` (veículos alugados) |

### Dashboard (`StatusFrotaSB`)

| # | Método | JPQL | Como testar |
|---|---|---|---|
| 34 | `atualizarCache()` (total) | `select count(v) from Veiculo v` | Web (admin): `/admin/dashboard.xhtml` |
| 35 | `atualizarCache()` (por status) | `select count(v) from Veiculo v where v.status = :status` *(DISPONIVEL, LOCADO, MANUTENCAO)* | Web (admin): `/admin/dashboard.xhtml` |
| 36 | `atualizarCache()` (reservas) | `select count(l) from Locacao l where l.status = :status` *(RESERVADA)* | Web (admin): `/admin/dashboard.xhtml` |
| 37 | `atualizarCache()` (disponíveis por agência) | `select a.id, count(v) from Agencia a left join a.veiculos v on v.status = :status group by a.id` | Web (admin): tabela de agências do dashboard |

### Operação de locação, autenticação e infraestrutura

| # | Onde | JPQL | Como testar |
|---|---|---|---|
| 38 | `OperacaoLocacaoSB.confirmarLocacao()` | `select count(l) from Locacao l where l.veiculo.id = :veiculoId and l.status in :status and l.dataRetirada < :devolucao and l.dataPrevDevolucao > :retirada` | Web: `/reserva.xhtml` — reserve o mesmo veículo em períodos sobrepostos; a 2ª tentativa deve ser recusada |
| 39 | `UsuarioSB.login` / `OperacaoLocacaoSB.login` | `select u from Usuario u where u.cpf = :cpf` | Web ou Desktop: tela de login |
| 40 | `AdminPadraoSB` (startup) | `select u from Usuario u where u.cpf = :cpf or u.email = :email` | Subir o servidor e conferir o `server.log` e a tabela `usuario` |
| 41 | `CrudBean.buscarTodos()` (genérica) | `select e from <Entidade> e` | Java SE (`buscarTodos()` dos beans que estendem `CrudBean`) |

<br>

## ⚠️ Observações

- O módulo EJB usa `RESOURCE_LOCAL` com credenciais JDBC no `persistence.xml` (não há *datasource* configurado no servidor).
- A pasta `jar/` do `AutoRentWeb` e do `AutoRentDesktop` contém o `AutoRentClient.jar` e o `jboss-client.jar` (cliente EJB do WildFly, ~20 MB).
- Se o servidor rodar em outro host ou porta, altere as constantes `HOST` e `PORTA` em `ConexaoEJB.java`.
