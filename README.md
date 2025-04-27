# Getting Started

### Generate book data 

 sql
insert into 
    book(id, author , isbn, name)
select
    i as id,
    substr(md5(random()::text), 1, 25) as author,   
    substr(md5(random()::text), 1, 25) as isbn,
    substr(md5(random()::text), 1, 25) as name
from 
    generate_series(1, 1_000) as i

### Run
java -jar app.jar --spring.profiles.active=local

### wrk2
wrk2 -t2 -c100 -d30s -R2000 http://localhost:8080/api/v1/books/hello
wrk2 -t2 -c100 -d30s -R2000 http://localhost:8080/api/v1/books/random/rows/1000/
wrk2 -t2 -c100 -d30s -R2000 http://localhost:8080/api/v1/books/random/cached/rows/1000/

#### rest postgres
slava@slava-IdeaPad-5-Pro-14ACN6:~$ wrk2 -t2 -c100 -d30s -R50000 http://localhost:8080/api/v1/books/random/rows/1000/
Running 30s test @ http://localhost:8080/api/v1/books/random/rows/1000/
  2 threads and 100 connections
  Thread calibration: mean lat.: 3370.136ms, rate sampling interval: 12337ms
  Thread calibration: mean lat.: 3358.227ms, rate sampling interval: 12337ms
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    13.56s     3.87s   20.64s    57.91%
    Req/Sec     8.00k    20.00     8.02k    50.00%
  479960 requests in 30.00s, 110.77MB read
Requests/sec:  15998.56
Transfer/sec:      3.69MB

#### rest postgres with redis cache (all data loaded)
slava@slava-IdeaPad-5-Pro-14ACN6:~$ wrk2 -t2 -c100 -d30s -R50000 http://localhost:8080/api/v1/books/random/cached/rows/1000/
Running 30s test @ http://localhost:8080/api/v1/books/random/cached/rows/1000/
  2 threads and 100 connections
  Thread calibration: mean lat.: 1248.343ms, rate sampling interval: 4632ms
  Thread calibration: mean lat.: 1257.539ms, rate sampling interval: 4579ms
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.21s     1.51s    8.03s    57.47%
    Req/Sec    18.43k   147.89    18.64k    62.50%
  1103971 requests in 30.00s, 254.89MB read
  Non-2xx or 3xx responses: 550
Requests/sec:  36797.37
Transfer/sec:      8.50MB

#### rest-reactive
slava@slava-IdeaPad-5-Pro-14ACN6:~$ wrk2 -t2 -c100 -d30s -R50000 http://localhost:8080/api/v1/books/random/1000
Running 30s test @ http://localhost:8080/api/v1/books/random/1000
  2 threads and 100 connections
  Thread calibration: mean lat.: 2810.552ms, rate sampling interval: 10395ms
  Thread calibration: mean lat.: 2810.136ms, rate sampling interval: 10403ms
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    11.53s     3.33s   17.35s    57.64%
    Req/Sec    10.42k     0.00    10.42k     0.00%
  627079 requests in 30.00s, 112.92MB read
  Non-2xx or 3xx responses: 287
Requests/sec:  20902.49
Transfer/sec:      3.76MB

### Rest specs
http://localhost:8080/swagger-ui/index.html

 json
