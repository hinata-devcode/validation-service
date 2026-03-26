# ==========================================
# STAGE 1: Build the React Code (Vite)
# ==========================================
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# ==========================================
# STAGE 2: Serve the files with Nginx
# ==========================================
FROM nginx:alpine

# Copy the compiled React files
COPY --from=build /app/dist/public /usr/share/nginx/html

# THE MISSING PIECE: Copy our custom Nginx routing rules!
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]