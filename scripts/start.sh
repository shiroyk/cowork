#!/bin/bash
kubectl apply -f k8s/ingress-nginx.yaml
until kubectl get endpoints -n ingress-nginx ingress-nginx-controller-admission -o jsonpath='{.subsets[*].addresses[*].ip}' | grep -q '[0-9]'; do
  echo waiting for ingress-nginx-controller
  sleep 2
done
kubectl apply -f k8s/dashboard.yaml,k8s/metrics-server.yaml,k8s/custom-metrics.yaml
kubectl apply -f k8s/editor
