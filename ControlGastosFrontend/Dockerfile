FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --legacy-peer-deps
COPY . .
ARG API_BACKEND_URL=http://localhost:8080
RUN sed -i "s|API_BACKEND_URL_PLACEHOLDER|${API_BACKEND_URL}|g" src/environments/environment.prod.ts
RUN npm run build -- --configuration production

FROM nginx:alpine
COPY --from=build /app/dist/ControlGastosFrontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
