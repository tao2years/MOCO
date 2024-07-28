#!/bin/bash

# 执行 kubectl delete 命令
echo "Deleting deployment.yaml ..."
kubectl delete -f deployment.yaml

echo "Deleting model.yaml ..."
kubectl delete -f model.yaml

echo "Deleting instance.yaml ..."
kubectl delete -f instance.yaml

echo "All files deleted successfully!"