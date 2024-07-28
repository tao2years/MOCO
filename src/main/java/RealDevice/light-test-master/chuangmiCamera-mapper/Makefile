.PHONY: all execute-app docker clean

all: execute-app

execute-app:
	go build -o chuangmicamera-app

docker:
	docker build . -t mistyfalcon/chuangmicamera-controller:v1.0.0

push:
	docker push mistyfalcon/chuangmicamera-controller:v1.0.0
clean:
	rm -f chuangmicamera-app
