# Warm Feed updater
Updates [Warm Feed](https://radiokot.com.ua/p/feed/) database from VK and Tumblr sources.

Build
```bash
./gradlew shadowJar
```

Run
```bash
TUMBLR_CONSUMER_KEY=*** \
TUMBLR_CONSUMER_SECRET=*** \
TUMBLR_ACCESS_TOKEN=*** \
TUMBLR_ACCESS_SECRET=*** \
VK_ACCESS_TOKEN=*** \
DB_HOST=localhost \
DB_PORT=3306 \
DB_NAME=feed \
DB_USER=*** \
DB_PASSWORD=*** \
java -Djava.util.logging.config.file=build/libs/logging.properties -jar build/libs/updater.jar

```
