# Lab 8 - Multi-stage Docker image (adapted from teacher sample)
# Node 22 meets Lab 8 requirement (Node >= 18) and avoids the EOL Node 18 base.
FROM node:22-alpine AS dependencies
WORKDIR /app
COPY app/package.json ./package.json
RUN npm install --omit=dev --no-audit --no-fund

FROM node:22-alpine AS production
WORKDIR /app
ENV NODE_ENV=production
COPY --from=dependencies --chown=node:node /app/node_modules ./node_modules
COPY --chown=node:node app/package.json app/server.js ./
USER node
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1:3000/health || exit 1
CMD ["node", "server.js"]
