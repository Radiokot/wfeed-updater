# Warm Feed updater
Updates [Warm Feed](https://radiokot.com.ua/p/feed/) database from VK and Tumblr sources for known authors.

### VK
Posts source is the user's newsfeed (the user is defined by `VK_ACCESS_TOKEN`). Also updates author photos and names.
[Get access token](https://oauth.vk.com/authorize?client_id=4712158&scope=offline,wall,friends,photos&redirect_uri=https://oauth.vk.com/blank.html&response_type=token)

### Tumblr
Posts source is the user's dashboard (the user is defined by `TUMBLR_ACCESS_TOKEN`). Does not update author photos and names.

## Build
```bash
./gradlew shadowJar
```

## Run
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
java -Dlog4j.configuration=file:build/libs/logging.properties -jar build/libs/updater.jar
```

## Test
```bash
docker-compose -f test-db-compose.yaml up -d
```
