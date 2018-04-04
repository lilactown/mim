#!/bin/sh

mkdir -p ~/.mim/

echo "~> Building mim..."
lein uberjar

echo "~> Copying files to ~/.mim/"
cp client.sh target/uberjar/mim.jar ~/.mim/

echo "~> Creating link to ~/.mim/client.sh at /usr/local/bin/mim"
sh -c "cd ~/.mim/ && ln client.sh /usr/local/bin/mim"

echo "~> Verifying installation..."
VERSION=$(mim --version)

if [ "$VERSION" = "mim version 1.0.0" ]; then
    echo "Success!"
    echo "Run 'mim' for usage instructions."
    exit 0
else
    echo "Installation failure."
    exit 1
fi
