#!/bin/bash

# Ensure NUM_CONSUMERS is set, defaulting to 1 if not provided
NUM_CONSUMERS=${NUM_CONSUMERS:-1}

echo "Starting $NUM_CONSUMERS consumer(s)..."

for i in $(seq 1 "$NUM_CONSUMERS"); do
  echo "Starting consumer #$i"
  java -cp /app/java-client.jar example.SimpleConsumer &
done

wait
echo "All consumers have been started."
