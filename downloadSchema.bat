@echo off
set /p token=< ./definitelynotatoken.txt
set endpoint=http://localhost:5454/graphql
call gradlew.bat :app:downloadApolloSchema -Pcom.apollographql.apollo.endpoint=%endpoint% -Pcom.apollographql.apollo.schema=src\main\graphql\de\datenkraken\datenkrake\schema.json "-Pcom.apollographql.apollo.headers=Authorization=Bearer %token%"
pause
