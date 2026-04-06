# STJabah Spring Backend Documentation
## Prerequisets
Just install docker and docker compose for running application. 
for modifying the code you will need java JDK and an IDE.

## Setup
Start docker compose by running this command
```bash
docker compose up
```
or use `--build` flag to force a rebuild of the backend image. to 
stop the services run this command. 
```bash
docker compose down
```
or add `-v` flag to delete the volumes and start clean.

In order to use the pgadmin dashboard go to `localhost` in your
browser. email is `stjabah@customsolutions.com` and password is `secret`
After logging in, add a new server and configure it with the following:
  - **name**: stjabah-dashboard
  - **host**: postgis
  - **username**: admin
  - **password**: secret

Credentials are hardcoded for development only. To change them, update docker-compose.yml.
Now you can use the dashboard to track the application database.

## Running Services
| Service    | URL                                    |
|------------|----------------------------------------|
| Backend API | http://localhost:8080                 |
| Swagger UI  | http://localhost:8080/swagger-ui.html |
| pgAdmin     | http://localhost                      |

## Incident Status
Incident has 6 status each represent a different state as follows:
1. `CREATED` → CR operator creates incident
2. `DISPATCHED` → CR operator dispatches to units
3. `ACKNOWLEDGED` → at least one unit responds via the ERT app
4. `RESPONDING` → at least one unit marks arrival on site via the ERT app
5. `RESOLVED` → all assigned units mark resolved via the ERT app
6. `CLOSED` → CR operator explicitly closes