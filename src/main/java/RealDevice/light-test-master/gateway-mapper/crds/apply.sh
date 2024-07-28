#!/bin/bash

# 定义要执行的 YAML 文件路径
model_yaml="model.yaml"
instance_yaml="instance.yaml"
deployment_yaml="deployment.yaml"

# 执行 kubectl apply 命令
echo "Applying model.yaml ..."
kubectl apply -f "$model_yaml"

echo "Applying instance.yaml ..."
kubectl apply -f "$instance_yaml"

echo "Applying deployment.yaml ..."
kubectl apply -f "$deployment_yaml"

echo "All files applied successfully!"