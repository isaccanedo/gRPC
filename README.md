## gRPC

# 1. Introdução
gRPC é uma estrutura RPC de código aberto de alto desempenho desenvolvida inicialmente pelo Google. Ele ajuda a eliminar o código clichê e ajuda a conectar serviços poliglotas em e entre data centers.

# 2. Visão geral
A estrutura é baseada em um modelo cliente-servidor de chamadas de procedimento remoto. Um aplicativo cliente pode chamar métodos diretamente em um aplicativo de servidor como se fosse um objeto local.

Este artigo usará as seguintes etapas para criar um aplicativo cliente-servidor típico usando gRPC:

- Definir um serviço em um arquivo .proto;
- Gerar código de servidor e cliente usando o compilador de buffer de protocolo;
- Criar a aplicação do servidor, implementando as interfaces de serviço geradas e gerando o servidor gRPC;
- Crie o aplicativo cliente, fazendo chamadas RPC usando stubs gerados.

Vamos definir um HelloService simples que retorna saudações em troca do nome e do sobrenome.

# 3. Dependências Maven
Vamos adicionar dependências grpc-netty, grpc-protobuf e grpc-stub:

```
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-netty</artifactId>
    <version>1.16.1</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-protobuf</artifactId>
    <version>1.16.1</version>
</dependency>
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-stub</artifactId>
    <version>1.16.1</version>
</dependency>
```

# 4. Definindo o serviço
Começamos definindo um serviço, especificando métodos que podem ser chamados remotamente junto com seus parâmetros e tipos de retorno.

Isso é feito no arquivo .proto usando os buffers de protocolo. Eles também são usados para descrever a estrutura das mensagens de carga útil.

### 4.1. Configurações Básicas
Vamos criar um arquivo HelloService.proto para nosso exemplo HelloService. Começamos adicionando alguns detalhes básicos de configuração:

```
syntax = "proto3";
option java_multiple_files = true;
package org.isaccanedo.grpc;
```

A primeira linha informa ao compilador qual sintaxe é usada neste arquivo. Por padrão, o compilador gera todo o código Java em um único arquivo Java. A segunda linha substitui essa configuração e tudo será gerado em arquivos individuais.

Finalmente, especificamos o pacote que queremos usar para nossas classes Java geradas.

### 4.2. Definindo a Estrutura da Mensagem
A seguir, definimos a mensagem:

```
message HelloRequest {
    string firstName = 1;
    string lastName = 2;
}
```

Isso define a carga útil da solicitação. Aqui, cada atributo que entra na mensagem é definido junto com seu tipo.

Um número exclusivo precisa ser atribuído a cada atributo, chamado de tag. Esta tag é usada pelo buffer de protocolo para representar o atributo em vez de usar o nome do atributo.

Portanto, ao contrário do JSON, em que passaríamos o nome do atributo firstName todas as vezes, o buffer de protocolo usaria o número 1 para representar o firstName. A definição da carga útil da resposta é semelhante à solicitação.

Observe que podemos usar a mesma tag em vários tipos de mensagem:

```
message HelloResponse {
    string greeting = 1;
}
```

4.3. Definindo o Contrato de Serviço
Finalmente, vamos definir o contrato de serviço. Para o nosso HelloService, definimos uma operação 
hello():

```
service HelloService {
    rpc hello(HelloRequest) returns (HelloResponse);
}
```

A operação hello () aceita uma solicitação unária e retorna uma resposta unária. O gRPC também oferece suporte a streaming prefixando a palavra-chave stream à solicitação e resposta.

# 5. Gerando o Código
Agora, passamos o arquivo HelloService.proto para o protoc do compilador do buffer de protocolo para gerar os arquivos Java. Existem várias maneiras de acionar isso.

5.1. Usando o compilador de buffer de protocolo
Primeiro, precisamos do compilador de buffer de protocolo. Podemos escolher entre muitos binários pré-compilados disponíveis aqui.

Além disso, precisamos obter o plugin gRPC Java Codegen.

Finalmente, podemos usar o seguinte comando para gerar o código:

```
protoc --plugin=protoc-gen-grpc-java=$PATH_TO_PLUGIN -I=$SRC_DIR 
  --java_out=$DST_DIR --grpc-java_out=$DST_DIR $SRC_DIR/HelloService.proto
```

### 5.2. Usando o plugin Maven
Como desenvolvedor, você deseja que a geração de código seja totalmente integrada ao seu sistema de construção. gRPC fornece um protobuf-maven-plugin para o sistema de compilação Maven:

```
<build>
  <extensions>
    <extension>
      <groupId>kr.motd.maven</groupId>
      <artifactId>os-maven-plugin</artifactId>
      <version>1.6.1</version>
    </extension>
  </extensions>
  <plugins>
    <plugin>
      <groupId>org.xolstice.maven.plugins</groupId>
      <artifactId>protobuf-maven-plugin</artifactId>
      <version>0.6.1</version>
      <configuration>
        <protocArtifact>
          com.google.protobuf:protoc:3.3.0:exe:${os.detected.classifier}
        </protocArtifact>
        <pluginId>grpc-java</pluginId>
        <pluginArtifact>
          io.grpc:protoc-gen-grpc-java:1.4.0:exe:${os.detected.classifier}
        </pluginArtifact>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>compile</goal>
            <goal>compile-custom</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

A extensão/plugin os-maven-plugin gera várias propriedades úteis de projeto dependentes da plataforma, como $ {os.detected.classifier}

# 6. Criação do servidor
Independentemente de qual método você usa para geração de código, os seguintes arquivos de chave serão gerados:

HelloRequest.java - contém a definição do tipo HelloRequest
HelloResponse.java - contém a definição do tipo HelleResponse
HelloServiceImplBase.java - contém a classe abstrata HelloServiceImplBase que fornece uma implementação de todas as operações que definimos na interface de serviço
### 6.1. Substituindo a classe de base de serviço
A implementação padrão da classe abstrata HelloServiceImplBase é lançar a exceção de tempo de execução io.grpc.StatusRuntimeException dizendo que o método não foi implementado.

Devemos estender essa classe e substituir o método hello() mencionado em nossa definição de serviço:

```
public class HelloServiceImpl extends HelloServiceImplBase {

    @Override
    public void hello(
      HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

        String greeting = new StringBuilder()
          .append("Hello, ")
          .append(request.getFirstName())
          .append(" ")
          .append(request.getLastName())
          .toString();

        HelloResponse response = HelloResponse.newBuilder()
          .setGreeting(greeting)
          .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

Se compararmos a assinatura de hello () com a que escrevemos no arquivo HellService.proto, notaremos que ela não retorna HelloResponse. Em vez disso, leva o segundo argumento como StreamObserver ```<HelloResponse>```, que é um observador de resposta, uma chamada de retorno para o servidor chamar com sua resposta.

Desta forma, o cliente tem a opção de fazer uma chamada bloqueadora ou não bloqueadora.

gRPC usa construtores para criar objetos. Usamos HelloResponse.newBuilder() e definimos o texto de saudação para construir um objeto HelloResponse. Definimos esse objeto para o método onNext() do responseObserver para enviá-lo ao cliente.

Finalmente, precisamos chamar onCompleted() para especificar que terminamos de lidar com o RPC, caso contrário, a conexão será interrompida e o cliente apenas aguardará a chegada de mais informações.

### 6.2. Executando o Grpc Server
Em seguida, precisamos iniciar o servidor gRPC para ouvir as solicitações de entrada:

```
public class GrpcServer {
    public static void main(String[] args) {
        Server server = ServerBuilder
          .forPort(8080)
          .addService(new HelloServiceImpl()).build();

        server.start();
        server.awaitTermination();
    }
}
```

Aqui, novamente usamos o construtor para criar um servidor gRPC na porta 8080 e adicionar o serviço HelloServiceImpl que definimos. start() iniciaria o servidor. Em nosso exemplo, chamaremos awaitTermination() para manter o servidor em execução em primeiro plano, bloqueando o prompt.

# 7. Criação do cliente
gRPC fornece uma construção de canal que abstrai os detalhes subjacentes, como conexão, pool de conexão, balanceamento de carga, etc.

Vamos criar um canal usando ManagedChannelBuilder. Aqui, especificamos o endereço do servidor e a porta.

Usaremos texto simples sem criptografia:

```
public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
          .usePlaintext()
          .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub 
          = HelloServiceGrpc.newBlockingStub(channel);

        HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder()
          .setFirstName("isaccanedo")
          .setLastName("gRPC")
          .build());

        channel.shutdown();
    }
}
```

Em seguida, precisamos criar um stub que usaremos para fazer a chamada remota real para hello (). O stub é a principal forma de os clientes interagirem com o servidor. Ao usar stubs gerados automaticamente, a classe stub terá construtores para agrupar o canal.

Aqui, estamos usando um stub de bloqueio / síncrono para que a chamada RPC aguarde a resposta do servidor e retorne uma resposta ou gere uma exceção. Existem dois outros tipos de stubs fornecidos pelo gRPC, que facilitam chamadas sem bloqueio / assíncronas.

Finalmente, é hora de fazer a chamada RPC hello (). Aqui passamos o HelloRequest. Podemos usar os setters gerados automaticamente para definir os atributos firstName e lastName do objeto HelloRequest.

Recebemos o objeto HelloResponse retornado do servidor.

# 8. Conclusão
Neste tutorial, vimos como poderíamos usar o gRPC para facilitar o desenvolvimento da comunicação entre dois serviços, focando na definição do serviço e deixando o gRPC lidar com todo o código clichê.