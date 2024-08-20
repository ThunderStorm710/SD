# Googol: Motor de Pesquisa de Páginas Web

## Resumo

Este projeto tem como objetivo criar um motor de pesquisa de páginas Web com funcionalidades semelhantes aos serviços Google.com, Bing.com e DuckDuckGo.com. Inclui indexação automática (Web crawler) e busca (search engine). O sistema deve armazenar informações relevantes sobre as páginas, como URL, título, citação de texto, e outras. Os utilizadores podem sugerir URLs para serem indexados e o sistema deve indexar recursivamente todas as ligações encontradas em cada página.

## Metas do Projeto

### Meta 1 (24 de março de 2023)

1. **Objetivos do Projeto**
    - Programar um sistema de pesquisa de páginas Web com arquitetura cliente-servidor.
    - Usar sockets Multicast para comunicação entre servidores.
    - Seguir um modelo multithread para desenhar os servidores.
    - Criar uma camada de acesso aos dados usando Java RMI.
    - Garantir a disponibilidade da aplicação através de redundância com failover.

2. **Funcionalidades a Desenvolver**
    - **Indexar novo URL:** Permitir ao utilizador introduzir manualmente um URL para ser indexado.
    - **Indexação recursiva:** O sistema deve indexar todos os URLs encontrados nas páginas previamente visitadas.
    - **Pesquisar páginas:** O utilizador pode pesquisar páginas com termos específicos, e o sistema retorna os resultados relevantes.
    - **Ordenar por relevância:** Resultados de pesquisa devem ser ordenados por importância, com base em links de outras páginas.
    - **Consultar ligações:** O utilizador pode consultar todas as páginas que fazem ligação para uma página específica.
    - **Administração em tempo real:** Exibir informações sobre o sistema em tempo real, como Downloaders e Barrels ativos.
    - **Particionamento do índice:** Dividir o índice em partes (A–M e N–Z) para otimizar a pesquisa.

3. **Arquitetura**
    - A aplicação é composta por Downloaders, Index Storage Barrels, RMI Search Module e RMI Client.
    - **Downloaders:** Responsáveis por obter as páginas Web em paralelo, analisá-las e atualizar o índice.
    - **Index Storage Barrels:** Servidores centrais que armazenam os dados, replicados para garantir alta disponibilidade.
    - **RMI Search Module:** Componente que comunica com os Storage Barrels para realizar buscas.
    - **RMI Client:** Interface do utilizador para acessar as funcionalidades do Googol.

4. **Protocolo UDP Multicast**
    - Um protocolo de comunicação Multicast deve ser implementado para enviar e receber dados entre os servidores.

5. **Requisitos Não-Funcionais**
    - Garantir tratamento adequado de exceções e implementação de failover para alta disponibilidade.

6. **Relatório**
    - Incluir detalhes sobre a arquitetura, funcionamento do Multicast, RMI, e distribuição de tarefas.

### Meta 2 (17 de maio de 2023)

1. **Objetivos do Projeto**
    - Desenvolver uma interface Web para a aplicação Googol.
    - Integrar a interface Web com a aplicação da Meta 1.
    - Aplicar tecnologias de programação para a Web, como Spring Boot.
    - Seguir a arquitetura MVC para desenvolvimento Web.
    - Usar WebSockets para comunicação assíncrona com os clientes.
    - Integrar a aplicação com serviços REST externos.

2. **Interface Web**
    - **Indexar novo URL:** Permitir a indexação manual de URLs via interface Web.
    - **Indexação recursiva:** O indexador automático deve visitar URLs encontrados em páginas previamente visitadas.
    - **Pesquisar páginas:** O motor de busca deve retornar resultados para termos de pesquisa inseridos pelo usuário.
    - **Resultados ordenados por importância:** Exibir resultados de pesquisa ordenados por relevância.
    - **Consultar ligações:** Exibir todas as ligações conhecidas que apontam para uma página específica.
    - **Administração em tempo real:** Exibir informações do sistema em tempo real via interface Web.
    - **Particionamento do índice:** Implementar a divisão do índice em partes (A–M e N–Z) e realizar o merge dos resultados de pesquisas.

3. **Notificações em Tempo Real**
    - Usar WebSockets para atualizar as páginas da aplicação instantaneamente.

4. **Integração com Serviço REST**
    - Integrar o Googol com a API do Hacker News para indexar URLs das "top stories" e das "stories" de um usuário específico.

5. **Relatório**
    - Descrever a arquitetura de software, integração do Spring Boot com RMI, uso de WebSockets, e integração com o serviço REST.

## Entrega do Projeto

O projeto deve ser entregue num arquivo ZIP contendo:
- Um ficheiro `README.md` com instruções para instalar e executar o projeto.
- Relatório em formato Javadoc/PDF/HTML detalhando a arquitetura e as funcionalidades implementadas.
- Código-fonte completo, incluindo arquivos JAR necessários (`downloader.jar`, `barrel.jar`, `search.jar`, `client.jar`).
- O projeto deve ser entregue na plataforma Inforestudante até a data limite.

## Futuras Expansões

Na fase seguinte do projeto, será desenvolvida uma interface Web e será feita a integração da aplicação com uma API REST externa.
