.PHONY: all execute-app docker clean

all: execute-app

execute-app:
	go build -o yeelight-app

docker:
	docker build . -t mistyfalcon/yeelight-controller:v1.1.0

push:
	docker push mistyfalcon/yeelight-controller:v1.1.0
clean:
	rm -f yeelight-app
