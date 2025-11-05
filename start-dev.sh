#!/bin/bash

# Start backend in a new terminal
gnome-terminal -- bash -c "cd backend && ./mvnw spring-boot:run; exec bash"

# Start frontend in a new terminal
gnome-terminal -- bash -c "cd frontend && npm run dev; exec bash"