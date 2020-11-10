const Socket = require('ws');

const ws = new Socket('ws://127.0.0.1:7070/events/', '', {});

ws.on('error', (err) => {
    console.error('[!]', err);
})

ws.on('open', function () {
    console.log('[*] connected.')
    ws._socket.prependListener('data', (chunk) => {
        console.log('[D] [chunk digest] <-', chunk.toString('hex'));
    });

    ws.send('test', (err) => {
        if (err) {
            console.error('[!] error sending message', err)
        }
    });
});

ws.on('message', (data) => {
    console.log('[*] data:', data);
})