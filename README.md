# rest-repository

### Features
- Rest API call as Repository

### Usages
#### Maven Dependency
```xml
<dependency>
	<groupId>com.samuraiiway</groupId>
	<artifactId>rest-repository</artifactId>
	<version>0.0.1</version>
</dependency>
```

#### Interface of Repository
```java
public interface MyRepository {

    @HttpRequest(uri = "/test/${id}", method = "GET")
    Response<TestResponse> findOne(Integer id);

    @HttpRequest(uri = "/test/name/${name}/status/${status}", method = "POST")
    Response<List<TestResponse>> findByNameAndStatus(String name, String status, @RequestBody TestRequest request);
}
```
- `@HttpRequest` is Method annotation to define uri and method of HTTP request
- `@RequestBody` is Parameter annotation to define request body of HTTP request (Only support `application/json` for now)
- `Response<List<TestResponse>>` is an example of return object which is using `ParameterizedTypeReference` for message converter and also support Java Generic Type
- `${name}` or `${status}` in uri is an example of path variable which is replacing by primitive data of method parameters (match by variable name)

#### RestRepositoryAdvisor (Interface)
```java
public interface RestRepositoryAdvisor {
    HttpHeaders getHeader();

    String getHostName();

    RestTemplate getRestTemplate();

    default void handlePostSuccess(ResponseEntity responseEntity) {}

    default void handleHttpException(HttpStatusCodeException ex) {
        ex.printStackTrace();
        throw new RestRepositoryException(ex.getStatusCode(), ex.getClass().getName(), new String(ex.getResponseBodyAsByteArray()));
    }

    default void handleDefaultException(Throwable ex) {
        ex.printStackTrace();
        throw new RestRepositoryException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getClass().getName(), ex.getMessage());
    }
}
```
- `RestRepositoryAdvisor` is an advisor for a Repository to make a REST API call
- `HttpHeaders getHeader()` is to define custom http headers
- `String getHotName()` is to define a hostname of HTTP request for a Repository
- `RestTemplate getRestTemplate()` is to define custom bean of RestTemplate for HTTP request
- `default void handlePostSuccess(ResponseEntity responseEntity)` is to define logic after HTTP request success
- `default void handleDefaultException(Throwable ex)` is default http exception handler which is only call when `HttpStatusCodeException` was thrown
- `default void handleDefaultException(Throwable ex)` is default exception handler for any Throwable

#### RestRepositoryAdvisor (Example of Implementation)
```java
@Component
public class MyRepositoryAdvisor implements RestRepositoryAdvisor {

    @Value("${hostname}")
    private String hostName;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public HttpHeaders getHeader() {
        HttpHeaders headers = new HttpHeaders();
        return headers;
    }

    @Override
    public String getHostName() {
        return this.hostName;
    }

    @Override
    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }
}
```
- This is a simple `@Component` bean
- So that can be made use of Spring IoC Container at full featured
- Recommend to declare as `@Component` or `@Service`

#### Configuration Bean
```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}

@Bean
public MyRepository myRepository(MyRepositoryAdvisor myRepositoryAdvisor) {
    return RestRepositoryFactory.createProxy(MyRepository.class, myRepositoryAdvisor);
}
```
- `RepositoryRegister.createProxy(Interface.class, RestRepositoryAdvisor advisor)` is to create proxy bean of Repository Interface with RestRepositoryAdvisor as advisor

#### Auto Configuration (Alternative of Configuration Bean)
```java
@EnableRestRepository
@SpringBootApplication
public class SpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootApplication.class, args);
	}
}
```
- `@EnableRestRepository` is to scan @RestRepository annotation and create a proxy bean of that interface

```java
@RestRepository(advisor = MyRepositoryAdvisor.class)
public interface MyRepository {
	...
}
```
- `@RestRepository` is to create a proxy bean of this annotated interface
- `advisor = <T extends RestRepositoryAdvisor>` is to specific `RestRepositoryAdvisor` corresponding to this annotated interface

#### Example of Repository Usages
```java
@Slf4j
@RestController
@RequestMapping("/my")
public class MyController {

    @Autowired
    private MyRepository myRepository;

    @GetMapping("/{id}")
    public ResponseEntity get(@PathVariable Integer id) {
        Response<TestResponse> response = myRepository.findOne(id);

        log.info("Response: [{}]", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{name}/{status}")
    public ResponseEntity post(@PathVariable String name, @PathVariable String status) {
        TestRequest testRequest = new TestRequest();
        testRequest.setIds(Stream.of(1, 2, 3).collect(Collectors.toSet()));

        Response<List<TestResponse>> response = myRepository.findByNameAndStatus(name, status, testRequest);

        log.info("Response: [{}]", response);
        return ResponseEntity.ok(response);
    }

}
```
- This is a really simple example of using Repository as Rest API call
- `@Autowired` Interface that already create proxy bean at Configuration @Bean
- Call it as method signature defined on Interface

#### Example of Raw Http Request
- Assume that `application.properties` has `hostname=http://localhost:8080`
- Assume `name` value is `samuraiiway`
- Assume `status` value is `single`
- Assume `testRequest` value is `{"message": "Hello World", "timestamp": 1612787138000}`
- By calling this `myRepository.findByNameAndStatus(name, status, testRequest)` will make HTTP request as
```text
POST /test/name/samuraiiway/status/single HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Content-Length: 54

{"message": "Hello World", "timestamp": 1612787138000}
```
