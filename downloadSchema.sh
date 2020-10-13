token=$(<./definitelynotatoken.txt)
endpoint=http://localhost:8000/graphql
./gradlew :app:downloadApolloSchema -Pcom.apollographql.apollo.endpoint="$endpoint" -Pcom.apollographql.apollo.schema=./src/main/graphql/de/datenkraken/datenkrake/schema.json "-Pcom.apollographql.apollo.headers=Authorization=Bearer $token"
