'use strict';

const { test, before, after } = require('node:test');
const assert = require('node:assert/strict');
const { spawn } = require('node:child_process');
const net = require('node:net');
const path = require('node:path');

let serverProcess;
let baseUrl;

async function reserveFreePort() {
    return new Promise((resolve, reject) => {
        const probe = net.createServer();
        probe.once('error', reject);
        probe.listen(0, '127.0.0.1', () => {
            const port = probe.address().port;
            probe.close(error => error ? reject(error) : resolve(port));
        });
    });
}

before(async () => {
    const port = await reserveFreePort();
    baseUrl = `http://127.0.0.1:${port}`;

    serverProcess = spawn(process.execPath, ['server.js'], {
        cwd: path.resolve(__dirname, '..'),
        env: { ...process.env, PORT: String(port), NODE_ENV: 'test' },
        stdio: ['ignore', 'pipe', 'pipe']
    });

    let stderr = '';
    serverProcess.stderr.on('data', chunk => { stderr += chunk.toString(); });

    // Allow the API server to start, without depending on a fixed timeout.
    for (let attempt = 0; attempt < 40; attempt++) {
        if (serverProcess.exitCode !== null) {
            throw new Error(`API process exited early: ${stderr}`);
        }
        try {
            const response = await fetch(`${baseUrl}/health`, {
                signal: AbortSignal.timeout(600)
            });
            if (response.ok) return;
        } catch (_) {
            // Server not listening yet.
        }
        await new Promise(resolve => setTimeout(resolve, 100));
    }
    throw new Error(`API did not start at ${baseUrl}. ${stderr}`);
});

after(() => {
    if (serverProcess && serverProcess.exitCode === null) {
        serverProcess.kill('SIGTERM');
    }
});

test('GET / returns HTTP 200', async () => {
    const response = await fetch(`${baseUrl}/`);
    assert.equal(response.status, 200);
});

test('GET / returns application version 2.0.0', async () => {
    const response = await fetch(`${baseUrl}/`);
    const json = await response.json();
    assert.equal(json.version, '2.0.0');
    assert.equal(json.env, 'test');
    assert.match(json.message, /DevOps Lab 8/);
});

test('GET /health returns status OK', async () => {
    const response = await fetch(`${baseUrl}/health`);
    assert.equal(response.status, 200);
    const json = await response.json();
    assert.equal(json.status, 'OK');
    assert.equal(typeof json.uptime, 'number');
});

test('Unknown route returns HTTP 404', async () => {
    const response = await fetch(`${baseUrl}/does-not-exist`);
    assert.equal(response.status, 404);
});
