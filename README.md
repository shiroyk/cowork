# Cowork
**Cowork** is an online real-time collaborative editing system, internal use of [Yjs CRDT](https://github.com/yjs/yjs) algorithm.

## Architecture
```mermaid
graph TB
classDef pod fill:#326ce5,stroke:#fff,stroke-width:4px,color:#fff;
client([client]) --> ingress
subgraph ingress
    ingress-pod(ingress-pod):::pod
end
subgraph cowork
    subgraph Deployment
        subgraph auth-service
            auth-pod(user-pod):::pod
        end
        subgraph user-service
            user-pod(user-pod):::pod
        end
        subgraph doc-service
            doc-pod(doc-pod):::pod
        end
        subgraph collab-service
            collab-pod(collab-pod):::pod
        end
        subgraph frontend-service
            frontend-pod(frontend-pod):::pod
        end
    end
    subgraph StatefulSet
        subgraph redis-service
            redis-pod(redis-pod):::pod
        end
        subgraph nats-service
            nats-pod(nats-pod):::pod
        end
        subgraph user-db-service
            user-db-pod(user-db-pod):::pod
        end
        subgraph doc-db-service
            doc-db-pod(doc-db-pod):::pod
        end
    end
    user-service --> user-db-service
    auth-service --> user-service
    collab-service --> user-service
    doc-service --> doc-db-service
    collab-service --> doc-service
    doc-service --> nats-service
    auth-service --> redis-service
    collab-service --> redis-service
    collab-service --> nats-service
end
ingress -- api.cowork.local/auth --> auth-service
ingress -- api.cowork.local/users --> user-service
ingress -- api.cowork.local/docs --> doc-service
ingress -- api.cowork.local/collab --> collab-service
ingress -- app.cowork.local --> frontend-service
```
## Usage
Install dependencies `docker`, [docker-buildx](https://github.com/docker/buildx), [minikube](https://minikube.sigs.k8s.io/docs/), `kubectl`, `make` and start the `minikube`
- build the image
```shell
chmod +x ./scripts/*
make
```
- deployment
```shell
kubectl apply -f ./k8s
```
- update hosts, need permission to modify the hosts file.
```shell
sudo ./scripts/host.sh
```
- waiting all pods start, execute to view the pods status
```shell
kubectl get pods -n cowork
```
open the [app.cowork.local](http://app.cowork.local)

## TODO
- [ ] usage
- [ ] document
- [ ] cowork-frontend
- [ ] cowork-group

## Reference
- [Yjs](https://github.com/yjs/yjs)
- [Yjs Internals](https://github.com/yjs/yjs/blob/main/INTERNALS.md)
- [Yjs: A Framework for Near Real-Time P2P Shared Editing on Arbitrary Data Types](http://dbis.rwth-aachen.de/~derntl/papers/preprints/icwe2015-preprint.pdf)
- [Real Differences between OT and CRDT for Co-Editors](https://arxiv.org/ftp/arxiv/papers/1810/1810.02137.pdf)