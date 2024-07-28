.PHONY: all execute-app docker clean

all: execute-app

execute-app:
	go build -o gateway-app

docker:
	docker build . -t mistyfalcon/gateway-controller:v1.0.0

push:
	docker push mistyfalcon/gateway-controller:v1.0.0
clean:
	rm -f gateway-app
