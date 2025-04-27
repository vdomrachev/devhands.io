#!/bin/bash

/local/wrk -t4 -c256 -R10000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R10000.txt
sleep 10

/local/wrk -t4 -c256 -R20000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R20000.txt
sleep 10

/local/wrk -t4 -c256 -R30000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R30000.txt
sleep 10

/local/wrk -t4 -c256 -R40000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R40000.txt
sleep 10

/local/wrk -t4 -c256 -R50000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R50000.txt
sleep 10

/local/wrk -t4 -c256 -R60000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R60000.txt
sleep 10

/local/wrk -t4 -c256 -R70000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R70000.txt
sleep 10

/local/wrk -t4 -c256 -R80000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R80000.txt
sleep 10

/local/wrk -t4 -c256 -R100000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R100000.txt
sleep 10

/local/wrk -t4 -c256 -R120000 -d30s -L http://localhost:8080/api/v1/books/random/cached/rows/1000/  > ~/results/rest_t4_c256_R120000.txt
sleep 10
