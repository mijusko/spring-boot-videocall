#!/bin/sh
# Pokreni Spring Boot u pozadini
java -jar /app/app.jar &

# Čekaj dok port 8080 ne postane dostupan
while ! nc -z localhost 8080; do
  sleep 1
done

# Pokreni Nginx
nginx -g "daemon off;"
