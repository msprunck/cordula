# Cordula [![Build Status](https://travis-ci.org/msprunck/cordula.svg?branch=master)](https://travis-ci.org/msprunck/cordula)

HTTP request adapter
<pre>
+-------------+
|  Webhook    |
+-------------+
   |      ^
   v      |
   HTTP POST
   |      ^
   v      |
+-------------+
|  Cordula    |
+-------------+
   |      ^
   v      |
   HTTP GET
   |      ^
   v      |
+-------------+
| Webservice  |
+-------------+
</pre>
## Usage

### Run the application locally

`lein run`

### Run the tests

`lein test`

### Packaging and running as standalone jar

```
lein do clean, uberjar
java -jar target/server.jar
```

## License

Copyright ©  2016 Matthieu Sprunck

Eclipse Public License v1.0
