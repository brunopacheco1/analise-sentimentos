# analise-sentimentos

Este projeto tem por finalidade implementar técnicas de aprendizado de máquina supervisionado, para análise de sentimento em mídias sociais, sobre o Escritório Central de Arrecadação e Distribuição (Ecad).

Este projeto se divide em dois módulos:
- Sentiments, API RESTFul e Interface Web para criação de dataset de treino e correção de sentimentalização, além de apresentação dos resultados.
- Sentimentalizer, Aplicações Spark para indexação e sentimentalização dos status (tweets, facebook posts, etc) captados.

Pilha de tecnologia:
- Bootsrap 3.0
- Java EE 7.0 (JAX-RS e EJB)
- Google Cloud Datastore
- Apache Kafka 0.10.2.1
- Scala 2.11
- Apache Spark 2.1.0

Servidor de Aplicação:
- Wildfly 10
- Google Cloud Compute Engine - 1 vCPU, 1.7 GB de RAM e 10 GB de HDD

Serviço de Streaming:
- Apache Kafka 0.10.2.1
- Google Cloud Compute Engine - 1 vCPU, 1.7 GB de RAM e 10 GB de HDD

Indexação e Sentimentalização de Status:
- Apache Spark 2.1.0
- Google Cloud Dataproc - 3 Nós de 1vCPU, 3.75 GB de RAM e 10 GB de SSD
