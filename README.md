# java-curl-utility
Fluent Java utility library to make REST API calls to remote APIs

This is a Fluent Java Utility that will allow you to do a rest api calls using curl command, the motivation behind creating this library is, we had an issue to call one secured API with mTLS and we tried to use a lot of existing libraries like, Sring Rest and apache http client, but these libraries require a valid client and server certificates issued by a valid CA, in our case we had the client private key and certificate and the issue was with the server certificate it was self signed certificate therefore we were not able to communicate with mTLs.

So, By Using Curl you can accomplish it using below command

```ssh
  curl --location --request POST 'https://protected.api.mtls/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Authorization: Basic base64Encode(username:password)' \
--header 'Accept: application/json' \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=accounts openid'
- k
--key path/to/your/client-private-key.key
--cert path/to/your/client-certificate.pem
```

But in order to implement this in java you can use ``` java.lang.ProcessBuilder``` and this process builder will run the command direclty to the underlying OS.
Writing Rest API Calls using process builder in which will use curl to call the API is frustrating since you need to write everything in an imperative way.

So We created a declarative fluent API.

# How it works

After cloning the repository in your local machine.
Navigate to ```org.ers.utils.TestCurl``` and you will find all test cases.

you can run ``` mvn clean install``` to run test cases.

# Examples

1. To convert above curl command using our library you can use the below.
let's Assume we have a Pojo class which will hold the secured https://protected.api.mtls/token token details

```
 public class AccessToken {
    
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    privaste int expiresIn;
    
    ...setters & getters...
 
 }

```

```
Map<String, String> httpHeaders = Map.of("Content-Type", "application/x-www-form-urlencoded");
Map<String, String> = Map<String, String> formData = Map.of(
                "grant_type", "client_credentials",
                "scope", "accounts openid"
        );

AccessToken accessToken = Curl.executor("path/to/working/directory/")
                    .post("https://protected.api.mtls/token")
                    .mTLS("path/to/your/client-private-key.key", "path/to/your/client-certificate.pem")
                    .basicAuth("client id", "client secret")
                    .headers(httpHeaders)
                    .formDate(formData)
                    .enableLogs(true)
                    .trustSelfSignedCertificate(true)
                    .execute(AccessToken.class);
                    
System.out.println(accessToken.getAccessToken);                    
  
```

***Note: your private key and certificate must be in the workingDirectory you have mentioned above*** 
```Curl.executor("path/to/working/directory/")```

2. Call Normal Post API Protected by Bearer token

```
String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTUsInVzZXJuYW1lIjoia21pbmNoZWxsZSIsImVtYWlsIjoia21pbmNoZWxsZUBxcS5jb20iLCJmaXJzdE5hbWUiOiJKZWFubmUiLCJsYXN0TmFtZSI6IkhhbHZvcnNvbiIsImdlbmRlciI6ImZlbWFsZSIsImltYWdlIjoiaHR0cHM6Ly9yb2JvaGFzaC5vcmcvYXV0cXVpYXV0LnBuZyIsImlhdCI6MTY4NTg2OTA0NywiZXhwIjoxNjg1ODcyNjQ3fQ.8ckCvzvwXOuAGify9zSqR1xpV8SLRykw5ByijkaBZuQ"


ProductList response = Curl.executor("path/to/working/directory/")
                          .post("https://protected.api.mtls/token")
                          .bearerToken(token)
                          .headers(httpHeaders)
                          .enableLogs(true)
                          .trustSelfSignedCertificate(true)
                          .execute(ProductList.class);
```

