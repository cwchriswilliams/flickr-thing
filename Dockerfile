FROM clojure:openjdk-13-tools-deps-slim-buster

COPY . /usr/src/app

WORKDIR /usr/src/app

EXPOSE 5050

CMD ["clojure", "-M:run-m", "--port", "5050", "--config", "./resources/appsettings.json"]

