// https://stackoverflow.com/a/70394675

import winston from 'winston';
import { resolve } from 'path';
import { fileURLToPath } from 'url';

const { format, transports, createLogger } = winston;
const { combine, timestamp, printf } = format;

export default (meta_url: string) => {
	const root = resolve('./');
	const file = fileURLToPath(new URL(meta_url));
	const file_path = file.replace(root, '');

	const customFormat = printf(({ level, message, timestamp, stack }) => {
		return `${timestamp} [${level}] ${file_path}: ${stack || message}`;
	});

	return createLogger({
		level: 'info',
		format: combine(
			timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
			format.splat(),
			format.errors({ stack: true }),
			customFormat
		),
		defaultMeta: { service: 'user-service' },
		transports: [new transports.Console({ format: combine(format.colorize(), customFormat) })]
	});
};
