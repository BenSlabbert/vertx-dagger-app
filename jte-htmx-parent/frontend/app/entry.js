import { server as app } from './index.js';

function shutdownGracefully(signal) {
	console.log('Server doing graceful shutdown signal: ', signal);
	app.server.close();
}

process.on('SIGINT', () => shutdownGracefully('SIGINT'));
process.on('SIGTERM', () => shutdownGracefully('SIGTERM'));
