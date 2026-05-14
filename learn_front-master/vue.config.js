const fs = require('fs');
const path = require('path');

module.exports = {
	transpileDependencies: true,
	publicPath:'/',
	outputDir: 'dist',
  	assetsDir: 'assets',
	devServer: {
		host: '127.0.0.1',
		port: 3001,
		open: true,
		client: {
			overlay: false,
		},
		proxy: {
			'/studyRoom': {
				target: 'http://127.0.0.1:8080',
				changeOrigin: true,
			},
		},
		setupMiddlewares: (middlewares, devServer) => {
			if (!devServer) return middlewares;
			
			devServer.app.use('/physics', (req, res, next) => {
				const url = req.url.split('?')[0];
				const publicPath = path.join(__dirname, 'public');
				const unityPath = path.join('unity-labs', 'physics');
				
				console.log(`[Physics Middleware] Request: ${url}`);
				
				const brFileMap = {
					'.wasm': '.wasm.br',
					'.data': '.data.br',
					'.framework.js': '.framework.js.br'
				};
				
				let targetFile = url;
				let isBrFile = false;
				
				for (const [ext, brExt] of Object.entries(brFileMap)) {
					if (url.endsWith(ext)) {
						const brPath = url.replace(ext, brExt);
						const fullPath = path.join(publicPath, unityPath, brPath);
						console.log(`[Physics Middleware] Checking: ${fullPath}`);
						if (fs.existsSync(fullPath)) {
							targetFile = brPath;
							isBrFile = true;
							console.log(`[Physics Middleware] Found! Serving: ${brPath}`);
							break;
						}
					}
				}
				
				if (isBrFile) {
					res.setHeader('Content-Encoding', 'br');
				}
				
				if (targetFile.endsWith('.wasm') || targetFile.endsWith('.wasm.br')) {
					res.setHeader('Content-Type', 'application/wasm');
				} else if (targetFile.endsWith('.data') || targetFile.endsWith('.data.br')) {
					res.setHeader('Content-Type', 'application/octet-stream');
				} else if (targetFile.endsWith('.js') || targetFile.endsWith('.js.br')) {
					res.setHeader('Content-Type', 'application/javascript');
				}
				
				const fullPath = path.join(publicPath, unityPath, targetFile);
				if (fs.existsSync(fullPath)) {
					console.log(`[Physics Middleware] Sending: ${fullPath}`);
					return res.sendFile(fullPath);
				}
				
				console.log(`[Physics Middleware] File not found: ${fullPath}`);
				next();
			});
			
			return middlewares;
		},
	},
	chainWebpack: config => {
		config.plugin('html')
			.tap(args => {
				args[0].title = "智学伴";
				return args;
			})
	},
}
