# medical-information-system-backend

create .env with params

    ADMIN_EMAIL= medical system admin
    ADMIN_PASSWORD= medical system admin password
    API_KEY= cloudinary api key
    API_SECRET= cloudinary secret
    APP_PASS= smtp (gmail) app password
    APP_USERNAME= smtp (gmail) username
    CLOUD_NAME= cloudinary cloud name
    DB_NAME= desired postgre db name
    DB_PASSWORD= desired postgre db password
    DB_USERNAME= desired postgre db username
    SECRET_SHA= jwt secret hash


create a jar file 

    ./mvnw clean package -DskipTests

start the container

    docker-compose up

shutdown the container

    docker-compose down