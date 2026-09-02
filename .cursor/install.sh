#!/usr/bin/env bash
# Idempotent bootstrap for the Angry Data Scanner Cloud Agent environment.
# Installs the toolchain the Gradle build requires and warms the dependency cache.
set -euo pipefail

TEMURIN_HOME="/usr/lib/jvm/temurin-21-jdk-amd64"

# The Gradle toolchain requires the Eclipse Temurin (Adoptium) JDK 21 (vendor = ADOPTIUM).
# The base image ships a generic OpenJDK, so install Temurin when it is missing.
if [ ! -x "${TEMURIN_HOME}/bin/javac" ]; then
  echo "Installing Eclipse Temurin (Adoptium) JDK 21..."
  sudo mkdir -p /etc/apt/keyrings
  wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public \
    | sudo gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
  # shellcheck disable=SC1091
  . /etc/os-release
  echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb ${VERSION_CODENAME} main" \
    | sudo tee /etc/apt/sources.list.d/adoptium.list >/dev/null
  sudo apt-get update
  sudo apt-get install -y temurin-21-jdk
fi

# Native/graphics libraries required by Compose Desktop (Skiko) and headless UI tests.
# These are present on the default base image; install defensively so the setup is
# self-contained if the base image ever changes. apt is a no-op when already satisfied.
sudo apt-get update
sudo apt-get install -y --no-install-recommends \
  xvfb x11-utils \
  libgl1 libgl1-mesa-dri libglx-mesa0 mesa-libgallium \
  libx11-6 libxext6 libxrender1 libxtst6 libxi6 libxrandr2 libxinerama1 \
  libfreetype6 libfontconfig1 fontconfig fonts-dejavu-core

# Warm the Gradle wrapper distribution and dependency cache, and compile main + test
# sources so the first agent build is fast. Gradle launches with the base JDK and
# resolves the Temurin toolchain automatically for compilation.
./gradlew --no-daemon compileTestKotlinDesktop
