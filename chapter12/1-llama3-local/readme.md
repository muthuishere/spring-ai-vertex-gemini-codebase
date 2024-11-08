

## Whats Next

Till now we have seen to use data from a GCP vertex and use it in our application , But sometimes you want things on your own.

There are opportunities to build things with ollama and Spring Ai also supports Ollama using [spring-ai-ollama-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)


But there is a problem , we want  them to install Ollama , what if there is an Option  to have everything within Java Code itself , should not rely any external dependencies.

[Andrej Karpathy](https://karpathy.ai/) Thought about it and build llama2.c , just a 900 line [code](https://github.com/karpathy/llama2.c) which can be used to build a chatbot , You just need to supply a model file and you can build chatbot without relying on any system. To learn more about how it works , you can watch [here](https://www.youtube.com/watch?v=zjkBMFhNj_g)

If you just want to make it work with java do proceed reading.

Based on llama2 , Alfonso² Peterssen built a similar version for java
https://github.com/mukel/llama3.java



## Setting up
The application requires Java 21 or above with experimental vector feature and Gradle version 8.10 or above to use llama3.java


### Setting up Gradle


In your build.gradle Update the Java Dependency to  21

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

```

And then after dependencies , create three tasks to enable the experimental vector API and the JVM to use the new vector API

```groovy

tasks.withType(JavaCompile).configureEach {
    options.compilerArgs += [
        '--enable-preview',
        '--add-modules', 'jdk.incubator.vector'
    ]
}

tasks.withType(Test).configureEach {
    jvmArgs += [
        '--enable-preview',
        '--add-modules', 'jdk.incubator.vector'
    ]
}

tasks.withType(JavaExec).configureEach {
    jvmArgs += [
        '--enable-preview',
        '--add-modules', 'jdk.incubator.vector'
    ]
}```



### Setting up Llama3.java

Create a package llama3 and add the Llama3.java file from the [Llama3.java](https://raw.githubusercontent.com/mukel/llama3.java/refs/heads/main/Llama3.java), Since its not been provided as a library , we are using the file as it is.

```java
package apps.unstructured.llama3;

// The entire Llama3.java file , remove the package name and add it here


```


For Llama3 to function, it needs a model file - which you can think of as the "brain" containing all the knowledge and patterns learned during training. This model is essentially a large collection of numbers (called weights) that help the AI make predictions. These model files can be downloaded from Hugging Face.

The model files come in Q4 or Q8 versions, which refers to quantization - a technique that compresses these numbers to save space. Think of it like image compression: Q4 compresses the numbers down to 4 bits (making the file smaller but potentially less precise), while Q8 uses 8 bits (larger file but maintains more precision). This is similar to how a heavily compressed JPEG might lose some image quality while taking up less space.


We will use a Q4 model file for our chatbot. You can download the model file from https://huggingface.co/mukel/Llama-3.2-1B-Instruct-GGUF/tree/main , Choose a Q4 Model Llama-3.2-1B-Instruct-Q4_0.gguf and press the download icon to download the model file. and save it  somewhere in you system


### Setting up Spring Ai Application config

Create a new property in application.properties file to store the path of the model file

```properties
# put your model file path
llama3.modelPath=/Users/brn/models/Llama-3.2-1B-Instruct-Q4_0.gguf
llama3.maxTokens=1000
```

## Create Custom Model based on LLama3.java
Create a class named LocalLlamaChatModel with model path set from the property file and implements ChatModel interface, to make it compatible to work with Spring Ai



```java
package apps.unstructured.llama3;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

@Service
@Slf4j
public class LocallamaChatModel implements ChatModel {


    @Value("${llama3.modelPath}")
    private String modelPath;

    @Value("${llama3.maxTokens}")
    private int maxTokens;

    @Override
    public ChatResponse call(Prompt prompt) {
    // code to get the response from the model
    }


    @Override
     public ChatOptions getDefaultOptions() {
        // we wont be using it as we are not exposing it as library
         return null;
     }

}
```


To get the answer from the model we need the following properties

```java

Llama localLlamaModel;

Sampler sampler;

```
Llama is a record which is part of the llama3.java , which helps us to load the model file and get the answer from the model file.

The Sampler is a interface which is responsible for selecting the next token in the sequence during text generation. The Sampler interface has a single method called nextToken which returns the next token in the sequence.


### Initialize & Load Model

We can load the model by initializing localLlamaModel , we should opt to pre load if its already there , if not we will fully load the model

```java
localLlamaModel = AOT.tryUsePreLoaded( Path.of(this.modelPath), maxTokens);
           if (localLlamaModel == null) {

               localLlamaModel = ModelLoader.loadModel( Path.of(this.modelPath), maxTokens, true);
           }
```

Next we will initialize Sampler with Temparaure and TopP values , which are used to generate the next token in the sequence, The seed is a  helper for token generator

```java
float temperature = 0.1f;
float topp = 0.95f;
long seed = System.nanoTime();
     sampler = selectSampler(localLlamaModel.configuration().vocabularySize, temperature,topp,seed);

   sampler = selectSampler(localLlamaModel.configuration().vocabularySize, temperature,topp,seed);

```




We want to initialize load the model file when the application starts ,

```java
@PostConstruct
 @SneakyThrows
 public void init() {



      localLlamaModel = AOT.tryUsePreLoaded( Path.of(this.modelPath), maxTokens);
         if (localLlamaModel == null) {
             // No compatible preloaded model found, fallback to fully parse and load the specified file.
             localLlamaModel = ModelLoader.loadModel( Path.of(this.modelPath), maxTokens, true);
         }

     float temperature = 0.1f;
     float topp = 0.95f;
     long seed = System.nanoTime();
          sampler = selectSampler(localLlamaModel.configuration().vocabularySize, temperature,topp,seed);





 }
```



### Implement call method

```java
@Override
    public ChatResponse call(Prompt prompt) {

    }
```

To read the question  from prompt , we can use prompt.getInstructions , which will return list of messages , since for demo purpose , we just get the first message from the list

```java
String question = prompt.getInstructions().getFirst().getContent();
```


Now we need to generate prompt tokens for the question, First create a ChatFormat instance with tokenizer and create a new state, also we need to create ChatFormat for tokenizing , and we initalize stop tokens , so we will use at the later to find the end of the text


```java
Llama.State state = localLlamaModel.createNewState();
 ChatFormat chatFormat = new ChatFormat(localLlamaModel.tokenizer());
 Set<Integer> stopTokens = chatFormat.getStopTokens();

```

The input prompt tokens is list of integer ,  First we will create an initial token to denote beginning of text by

```java
List<Integer> promptTokens = new ArrayList<>();
     promptTokens.add(chatFormat.beginOfText);

```

And then create a UserMessage from the question
```java
ChatFormat.Message message = new ChatFormat.Message(ChatFormat.Role.USER, question);

```

Now create tokens for the questions by calling chatFormat.encodeMessage

```java
 promptTokens.addAll(chatFormat.encodeMessage(message));
```

Now finally add an end of entry by passing an empty assistant message and instead of doing encodeMessage , do a encodeHeader

```java

 ChatFormat.Message emptyEndMessage = new ChatFormat.Message(ChatFormat.Role.ASSISTANT, "");

promptTokens.addAll(chatFormat.encodeHeader(emptyEndMessage));

```

The getInputTokens method will look like this

```java
private  List<Integer> getInputTokens(String question, ChatFormat chatFormat) {
    List<Integer> promptTokens = new ArrayList<>();
    promptTokens.add(chatFormat.beginOfText);
           ChatFormat.Message message = new ChatFormat.Message(ChatFormat.Role.USER, question);
    promptTokens.addAll(chatFormat.encodeMessage(message));

    ChatFormat.Message emptyEndMessage = new ChatFormat.Message(ChatFormat.Role.ASSISTANT, "");
    promptTokens.addAll(chatFormat.encodeHeader(emptyEndMessage));
    return promptTokens;
}


```

Now we need to call the generate Tokens method of the model to get the response tokens. The generate tokens  method look like this

```java

public static List<Integer> generateTokens(Llama model, State state, int startPosition, List<Integer> promptTokens, Set<Integer> stopTokens, int maxTokens, Sampler sampler, boolean echo,
                                           IntConsumer onTokenGenerated) {
                                           }
```

we have the model , we have the state , The startposition will be zero , we have prompt tokens , we have  StopTokens , we have maxTokens , We have sampler , we can set echo to true ,  if we want to print , and there is a consumer which will be invoked after each token generation. we dont care and it finally return list of response tokens


Now we can invoke it by

```java
var echo =false;
List<Integer> responseTokens = Llama.generateTokens(localLlamaModel, state, 0, promptTokens, stopTokens, maxTokens, sampler,echo, token -> {

})
```

Every response Tokens will return answer along the stop tokens , so if we see a stop token at the end , we need to remove it

```java

if (!responseTokens.isEmpty() && stopTokens.contains(responseTokens.getLast())) {
    responseTokens.removeLast();
}
```

and  now our response tokens are available , we can convert it to text


```java
 String answer = localLlamaModel.tokenizer().decode(responseTokens);
```

And finally we can return ChatResponse with the answer like this  , Just some Java verbosity or adaptability

```java

AssistantMessage assistantMessage = new AssistantMessage(answer);
Generation generation = new Generation(assistantMessage);
return new ChatResponse(List.of(generation));
```


The final Implementation would look something like this


```java

@Override
 public ChatResponse call(Prompt prompt) {

     String question = prompt.getInstructions().getFirst().getContent();


     Llama.State state = localLlamaModel.createNewState();
     ChatFormat chatFormat = new ChatFormat(localLlamaModel.tokenizer());
     Set<Integer> stopTokens = chatFormat.getStopTokens();


     List<Integer> promptTokens = getInputTokens(question, chatFormat);

     var echo =false;
     List<Integer> responseTokens = Llama.generateTokens(localLlamaModel, state, 0, promptTokens, stopTokens, maxTokens, sampler,echo, token -> {

     });


     if (!responseTokens.isEmpty() && stopTokens.contains(responseTokens.getLast())) {
         responseTokens.removeLast();
     }

     String answer = localLlamaModel.tokenizer().decode(responseTokens);

     AssistantMessage assistantMessage = new AssistantMessage(answer);
     Generation generation = new Generation(assistantMessage);

     return new ChatResponse(List.of(generation));

 }

 private  List<Integer> getInputTokens(String question, ChatFormat chatFormat) {
     List<Integer> promptTokens = new ArrayList<>();
     promptTokens.add(chatFormat.beginOfText);
            ChatFormat.Message message = new ChatFormat.Message(ChatFormat.Role.USER, question);
     promptTokens.addAll(chatFormat.encodeMessage(message));

     ChatFormat.Message emptyEndMessage = new ChatFormat.Message(ChatFormat.Role.ASSISTANT, "");
     promptTokens.addAll(chatFormat.encodeHeader(emptyEndMessage));
     return promptTokens;
 }

```
 Now we have succesfully implemented a sort of working custom model , we will see how to expose as an endpoint

 ### Expose as an endpoint

  We can expose the model as an endpoint by createing  LocallamaChatController  class and our model as a dependency


  ```java
  @RestController
  @Slf4j
  @RequiredArgsConstructor
  public class LocallamaChatController {

      private final LocallamaChatModel locallamaChatModel;
  }
  ```

  Now we will build an endpoint as we always do in spring boot

  ```java
  @PostMapping("/api/local-llama-chat")
   public ChatBotResponse askQuestion(@RequestBody ChatBotRequest chatBotRequest) {

       String question = chatBotRequest.question();
       Prompt prompt = new Prompt(question);
       ChatResponse chatResponse = locallamaChatModel.call(prompt);

       String answer = chatResponse.getResult().getOutput().getContent();
       return new ChatBotResponse(chatBotRequest.question(),  answer);
   }

  ```
