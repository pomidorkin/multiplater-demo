const path = require('path')
const http = require('http')
const express = require('express')
const socketio = require('socket.io')

const app = express()
const server = http.createServer(app)
const io = socketio(server)

const port = process.env.port || 3000
const publicDirectoryPath = path.join(__dirname, '../public')

app.use(express.static(publicDirectoryPath))
// app.use(express.json())

io.on('connection', (socket) => {
    console.log('New user connected')

    // Emits to a single client
    socket.emit('message', 'Welcome!')

    // Emits to all clients
    // io.emit('message', 'A new user joined')

    // Emits to all clients except for the client 'socket'
    socket.broadcast.emit('message', 'A new user joined')

    socket.on('disconnect', () => {
        io.emit('message', 'User has left the chat')
    })
})


server.listen(port, () => {
    console.log('Server is up on port ' + port)
})