#!/bin/bash
PROTO=$1
PROTOC_VERSION="24.3"
PROTOC_GO_VERSION="1.31.0"
PROTOC_GO_GRPC_VERSION="1.2"
PROTOC_JAVA_VERSION="1.58.0"
PROTOBUF_PATH=".protobuf"

die() {
  echo "$@" >&2
  exit 1
}

helpFunction() {
    echo "Usage: $0 path/example.proto --go --out=out_path"
    printf "Arguments:"
    printf "\n --go generate golang code"
    printf "\n --java generate java code"
    printf "\n --all generate all (golang, java)"
    printf "\n --out output path"
    printf "\n --protobuf protobuf path\n"
    exit 1
}

if [ -z "$PROTO" ]; then
    helpFunction
fi

protoc_check() {
  local protoc_path="$PROTOBUF_PATH/bin/protoc"
  if [ -r "$protoc_path" ]; then
    return 0
  fi
  mkdir -p "$PROTOBUF_PATH/bin"
  if ! command -v wget > /dev/null; then
    die "wget is not available"
  fi
  echo "start downloading protoc"
  local out_file="$PROTOBUF_PATH/protoc.zip"
  local download_url="https://github.com/protocolbuffers/protobuf/releases/download/v$PROTOC_VERSION/protoc-$PROTOC_VERSION-$OS-$ARCH.zip"
  wget "$download_url" -O "$out_file" || die "unable to download protoc from $download_url"
  if ! command -v unzip > /dev/null; then
    unzip "$out_file" -d "$PROTOBUF_PATH" && rm -f "$out_file"
  fi
  unzip "$out_file" -d "$PROTOBUF_PATH" && rm -f "$out_file"
  chmod +x "$protoc_path"
}

protoc_java_check() {
  local protoc_java_path="$PROTOBUF_PATH/bin/protoc-gen-grpc-java"
  if [ -r "$protoc_java_path" ]; then
    return 0
  fi
  mkdir -p "$PROTOBUF_PATH/bin"
  if ! command -v wget > /dev/null; then
    die "wget is not available"
  fi
  echo "start downloading protoc-gen-grpc-java"
  local download_url="https://repo.maven.apache.org/maven2/io/grpc/protoc-gen-grpc-java/$PROTOC_JAVA_VERSION/protoc-gen-grpc-java-$PROTOC_JAVA_VERSION-$OS-$ARCH.exe"
  wget "$download_url" -O "$protoc_java_path" || die "unable to download protoc-gen-grpc-java from $download_url"
  chmod +x "$protoc_java_path"
}

protoc_go_check() {
  if ! command -v go > /dev/null; then
    die "go is not available"
  fi
  if [ ! -r "$PROTOBUF_PATH/bin/protoc-gen-go" ]; then
    echo "start downloading protoc-gen-go"
    GOBIN="$(pwd)/$PROTOBUF_PATH/bin" go install "google.golang.org/protobuf/cmd/protoc-gen-go@v$PROTOC_GO_VERSION"
  fi
  if [ ! -r "$PROTOBUF_PATH/bin/protoc-gen-go-grpc" ]; then
    echo "start downloading protoc-gen-go-grpc"
    GOBIN="$(pwd)/$PROTOBUF_PATH/bin" go install "google.golang.org/grpc/cmd/protoc-gen-go-grpc@v$PROTOC_GO_GRPC_VERSION"
  fi
}

OS=$(uname -o)
ARCH=$(uname -m)
case $OS in
  Darwin)
    OS="osx"
    ;;
  *Linux)
    OS="linux"
    ;;
esac
case $ARCH in
  arm64|aarch64)
    ARCH="aarch_64"
    ;;
esac

GEN_GO=false
GEN_JAVA=false
OUT_PATH="src/"
OPTS=""
for arg in "${@:2}"; do
  case "$arg" in
    --go)
      GEN_GO=true
      ;;
    --java)
      GEN_JAVA=true
      ;;
    --all)
      GEN_GO=true
      GEN_JAVA=true
      ;;
    --out=*)
      OUT_PATH="${arg#*=}/generated"
      ;;
    --protobuf=*)
      PROTOBUF_PATH="${arg#*=}"
      ;;
  esac
done

protoc_check

if [ $GEN_GO == "true" ]; then
  protoc_go_check
  go_out_path="$OUT_PATH/golang"
  mkdir -p "$go_out_path"
  OPTS="$OPTS --go_out=$go_out_path --go-grpc_out=$go_out_path"
fi
if [ $GEN_JAVA == "true" ]; then
  protoc_java_check
  java_out_path="$OUT_PATH/java"
  mkdir -p "$java_out_path"
  OPTS="$OPTS --java_out=$java_out_path --grpc-java_out=$java_out_path"
fi

echo "protoc $OPTS $PROTO"
eval PATH="$(pwd)/$PROTOBUF_PATH/bin" protoc "$OPTS" "$PROTO"